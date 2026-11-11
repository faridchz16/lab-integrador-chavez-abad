package com.tecsup.productos.ui.admin.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.R
import com.tecsup.productos.data.categorias.CategoriaRepository
import com.tecsup.productos.data.inventario.InventarioRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.domain.inventario.CampoProducto
import com.tecsup.productos.domain.inventario.ErrorCampo
import com.tecsup.productos.domain.inventario.FormularioProducto
import com.tecsup.productos.domain.inventario.ValidadorProducto
import com.tecsup.productos.domain.inventario.aFormulario
import com.tecsup.productos.domain.inventario.aProducto
import com.tecsup.productos.domain.inventario.con
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.TipoProducto
import com.tecsup.productos.ui.aMensajeUsuario
import com.tecsup.productos.ui.admin.ResultadoEdicion
import com.tecsup.productos.ui.admin.TipoResultadoEdicion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoEdicionProducto(
    val esNuevo: Boolean,
    val cargando: Boolean = !esNuevo,
    val noEncontrado: Boolean = false,
    val formulario: FormularioProducto = FormularioProducto(),
    val original: FormularioProducto = FormularioProducto(),
    val base: Item? = null,
    val tocados: Set<CampoProducto> = emptySet(),
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false
) {
    val errores: Map<CampoProducto, ErrorCampo> = ValidadorProducto.validar(formulario)

    val erroresVisibles: Map<CampoProducto, ErrorCampo>
        get() = if (intentoGuardar) errores else errores.filterKeys { it in tocados }

    val hayCambios: Boolean get() = formulario != original

    val puedeGuardar: Boolean get() = !guardando && !cargando && (esNuevo || hayCambios)

    val vistaPrevia: Item get() = formulario.aProducto(base, base?.id ?: 0)
}

class EditarProductoViewModel(
    private val idProducto: Int,
    private val inventario: InventarioRepository,
    categoriaRepository: CategoriaRepository,
    private val textos: ProveedorTextos
) : ViewModel() {

    private val _estado = MutableStateFlow(
        EstadoEdicionProducto(esNuevo = idProducto == NUEVO_PRODUCTO)
    )
    val estado: StateFlow<EstadoEdicionProducto> = _estado.asStateFlow()

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.categorias
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = emptyList()
        )

    private val _resultado = MutableStateFlow<ResultadoEdicion?>(null)
    val resultado: StateFlow<ResultadoEdicion?> = _resultado.asStateFlow()

    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    init {
        if (idProducto != NUEVO_PRODUCTO) cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val producto = try {
                inventario.obtenerProducto(idProducto)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            _estado.update { actual ->
                if (producto == null) {
                    actual.copy(cargando = false, noEncontrado = true)
                } else {
                    val formulario = producto.aFormulario()
                    actual.copy(
                        cargando = false,
                        formulario = formulario,
                        original = formulario,
                        base = producto
                    )
                }
            }
        }
    }

    fun cambiarCampo(campo: CampoProducto, valor: String) {
        _estado.update { actual ->
            actual.copy(
                formulario = actual.formulario.con(campo, valor),
                tocados = actual.tocados + campo
            )
        }
    }

    fun seleccionarCategoria(categoriaId: Long?) {
        _estado.update { actual ->
            actual.copy(formulario = actual.formulario.copy(categoriaId = categoriaId))
        }
    }

    fun seleccionarTipo(tipo: TipoProducto) {
        _estado.update { actual ->
            actual.copy(formulario = actual.formulario.copy(tipo = tipo))
        }
    }

    fun guardar() {
        val actual = _estado.value
        if (!actual.puedeGuardar) return
        if (actual.errores.isNotEmpty()) {
            _estado.update { it.copy(intentoGuardar = true) }
            _avisos.trySend(textos.texto(R.string.producto_form_revisar))
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            try {
                val producto = actual.formulario.aProducto(actual.base, idProducto)
                val id = inventario.guardarProducto(producto, nuevo = actual.esNuevo)
                _estado.update { it.copy(original = it.formulario) }
                _resultado.value = ResultadoEdicion(
                    tipo = if (actual.esNuevo) {
                        TipoResultadoEdicion.CREADO
                    } else {
                        TipoResultadoEdicion.ACTUALIZADO
                    },
                    id = id,
                    titulo = producto.titulo
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _avisos.send(e.aMensajeUsuario(textos))
            } finally {
                _estado.update { it.copy(guardando = false) }
            }
        }
    }

    fun eliminar() {
        val actual = _estado.value
        if (actual.esNuevo || actual.guardando) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            try {
                inventario.eliminarProducto(idProducto)
                _estado.update { it.copy(original = it.formulario) }
                _resultado.value = ResultadoEdicion(
                    tipo = TipoResultadoEdicion.ELIMINADO,
                    id = idProducto,
                    titulo = actual.base?.titulo ?: actual.formulario.titulo.trim()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _avisos.send(e.aMensajeUsuario(textos))
            } finally {
                _estado.update { it.copy(guardando = false) }
            }
        }
    }

    fun resultadoProcesado() {
        _resultado.value = null
    }

    companion object {
        const val NUEVO_PRODUCTO = 0
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer, idProducto: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    EditarProductoViewModel(
                        idProducto = idProducto,
                        inventario = container.inventarioRepository,
                        categoriaRepository = container.categoriaRepository,
                        textos = container.textos
                    )
                }
            }
    }
}
