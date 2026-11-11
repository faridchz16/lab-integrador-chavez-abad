package com.tecsup.productos.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.R
import com.tecsup.productos.data.categorias.CategoriaRepository
import com.tecsup.productos.data.preferencias.PreferenciasRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.catalogo.FiltroCatalogo
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.TipoProducto
import com.tecsup.productos.domain.model.precioFinal
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor

class ListaViewModel(
    private val repo: ItemRepository,
    categoriaRepository: CategoriaRepository,
    private val preferencias: PreferenciasRepository,
    private val notificador: Notificador,
    private val textos: ProveedorTextos
) : ViewModel() {

    private val _estadoBase = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)

    private val _filtro = MutableStateFlow(FiltroCatalogo())
    val filtro: StateFlow<FiltroCatalogo> = _filtro.asStateFlow()

    val estado: StateFlow<UiState<List<Item>>> =
        combine(_estadoBase, _filtro) { base, filtro ->
            if (base is UiState.Exito) UiState.Exito(filtro.aplicarA(base.datos)) else base
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Cargando)

    val rangoPrecios: StateFlow<ClosedFloatingPointRange<Double>?> = _estadoBase
        .map { base ->
            val precios = (base as? UiState.Exito)?.datos?.map { it.precioFinal() }.orEmpty()
            if (precios.isEmpty()) null else floor(precios.min())..ceil(precios.max())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.categorias
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS), emptyList())

    val historial: StateFlow<List<String>> = preferencias.historialBusqueda
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS), emptyList())

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    private var refrescoTerminado = false

    init {
        viewModelScope.launch {
            repo.items.collect { lista ->
                if (lista.isNotEmpty() || refrescoTerminado) {
                    _estadoBase.value = UiState.Exito(lista)
                }
            }
        }
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            if (_estadoBase.value is UiState.Exito) {
                _refrescando.value = true
            }
            try {
                if (_estadoBase.value is UiState.Error) _estadoBase.value = UiState.Cargando
                val resultado = repo.refrescar()
                refrescoTerminado = true
                resultado
                    .onSuccess { n ->
                        if (_estadoBase.value is UiState.Cargando) {
                            _estadoBase.value = UiState.Exito(emptyList())
                        }
                        _aviso.value = null
                        notificador.mostrar(
                            textos.texto(R.string.notificacion_titulo),
                            textos.plural(R.plurals.notificacion_guardados, n, n)
                        )
                    }
                    .onFailure { e ->
                        if (repo.cantidadGuardada() == 0) {
                            _estadoBase.value = UiState.Error(e.aMensajeUsuario(textos))
                        } else {
                            if (_estadoBase.value !is UiState.Exito) {
                                _estadoBase.value = UiState.Exito(repo.items.first())
                            }
                            _aviso.value = textos.texto(R.string.lista_aviso_sin_conexion)
                        }
                    }
            } finally {
                _refrescando.value = false
            }
        }
    }

    fun alternarFavorito(id: Int) {
        viewModelScope.launch { repo.alternarFavorito(id) }
    }

    fun cambiarConsulta(consulta: String) {
        _filtro.update { it.copy(consulta = consulta) }
    }

    fun confirmarBusqueda(consulta: String) {
        cambiarConsulta(consulta)
        viewModelScope.launch { preferencias.agregarBusqueda(consulta) }
    }

    fun limpiarConsulta() = cambiarConsulta("")

    fun eliminarDelHistorial(consulta: String) {
        viewModelScope.launch { preferencias.eliminarBusqueda(consulta) }
    }

    fun limpiarHistorial() {
        viewModelScope.launch { preferencias.limpiarHistorial() }
    }

    fun seleccionarCategoria(id: Long?) {
        _filtro.update { actual ->
            val nuevas = if (id == null || actual.categorias == setOf(id)) emptySet() else setOf(id)
            actual.copy(categorias = nuevas)
        }
    }

    fun mostrarCategoria(id: Long) {
        _filtro.update { it.copy(categorias = setOf(id), tipos = emptySet()) }
    }

    fun mostrarTipo(tipo: TipoProducto) {
        _filtro.update { it.copy(categorias = emptySet(), tipos = setOf(tipo)) }
    }

    fun aplicarFiltros(nuevo: FiltroCatalogo) {
        _filtro.update { actual -> nuevo.copy(consulta = actual.consulta) }
    }

    fun limpiarFiltros() {
        _filtro.update { it.sinFiltros() }
    }

    fun limpiarTodo() {
        _filtro.value = FiltroCatalogo()
    }

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ListaViewModel(
                    repo = container.itemRepository,
                    categoriaRepository = container.categoriaRepository,
                    preferencias = container.preferenciasRepository,
                    notificador = container.notificador,
                    textos = container.textos
                )
            }
        }
    }
}
