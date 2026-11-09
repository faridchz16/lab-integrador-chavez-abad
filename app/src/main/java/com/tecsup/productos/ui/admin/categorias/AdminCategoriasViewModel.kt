package com.tecsup.productos.ui.admin.categorias

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
import com.tecsup.productos.domain.inventario.CategoriaEliminada
import com.tecsup.productos.domain.inventario.ErrorInventario
import com.tecsup.productos.domain.inventario.ErrorNombreCategoria
import com.tecsup.productos.domain.inventario.ValidadorCategoria
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import com.tecsup.productos.ui.admin.AvisoInventario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EdicionCategoria(
    val id: Long?,
    val nombre: String,
    val error: ErrorNombreCategoria? = null,
    val mostrarError: Boolean = false,
    val guardando: Boolean = false
) {
    val esNueva: Boolean get() = id == null
    val errorVisible: ErrorNombreCategoria? get() = error?.takeIf { mostrarError }
}

class AdminCategoriasViewModel(
    categoriaRepository: CategoriaRepository,
    private val inventario: InventarioRepository,
    private val textos: ProveedorTextos
) : ViewModel() {

    val estado: StateFlow<UiState<List<Categoria>>> = categoriaRepository.categorias
        .map<List<Categoria>, UiState<List<Categoria>>> { lista -> UiState.Exito(lista) }
        .catch { error -> emit(UiState.Error(error.aMensajeUsuario(textos))) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = UiState.Cargando
        )

    private val _edicion = MutableStateFlow<EdicionCategoria?>(null)
    val edicion: StateFlow<EdicionCategoria?> = _edicion.asStateFlow()

    private val _avisos = Channel<AvisoInventario>(Channel.BUFFERED)
    val avisos: Flow<AvisoInventario> = _avisos.receiveAsFlow()

    private var ultimaEliminada: CategoriaEliminada? = null

    fun abrirNueva() {
        _edicion.value = EdicionCategoria(id = null, nombre = "").validada()
    }

    fun abrirEdicion(categoria: Categoria) {
        _edicion.value = EdicionCategoria(id = categoria.id, nombre = categoria.nombre).validada()
    }

    fun cambiarNombre(nombre: String) {
        _edicion.update { actual ->
            actual?.copy(nombre = nombre.take(ValidadorCategoria.LARGO_MAXIMO + 1), mostrarError = true)
                ?.validada()
        }
    }

    fun cerrarEdicion() {
        if (_edicion.value?.guardando == true) return
        _edicion.value = null
    }

    fun guardar() {
        val actual = _edicion.value?.validada() ?: return
        if (actual.guardando) return
        if (actual.error != null) {
            _edicion.value = actual.copy(mostrarError = true)
            return
        }
        viewModelScope.launch {
            _edicion.value = actual.copy(guardando = true)
            val nombre = actual.nombre.trim()
            try {
                if (actual.id == null) {
                    inventario.crearCategoria(nombre)
                    _avisos.send(AvisoInventario(textos.texto(R.string.categorias_admin_creada, nombre)))
                } else {
                    inventario.renombrarCategoria(actual.id, nombre)
                    _avisos.send(AvisoInventario(textos.texto(R.string.categorias_admin_renombrada, nombre)))
                }
                _edicion.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorInventario.NombreDuplicado) {
                _edicion.value = actual.copy(
                    error = ErrorNombreCategoria.DUPLICADO,
                    mostrarError = true,
                    guardando = false
                )
            } catch (e: Exception) {
                _edicion.value = actual.copy(guardando = false)
                _avisos.send(AvisoInventario(e.aMensajeUsuario(textos)))
            }
        }
    }

    fun eliminar(categoria: Categoria) {
        viewModelScope.launch {
            try {
                ultimaEliminada = inventario.eliminarCategoria(categoria.id)
                _avisos.send(
                    AvisoInventario(
                        texto = textos.texto(R.string.categorias_admin_eliminada, categoria.nombre),
                        conDeshacer = true
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _avisos.send(AvisoInventario(e.aMensajeUsuario(textos)))
            }
        }
    }

    fun deshacer() {
        val categoria = ultimaEliminada ?: return
        ultimaEliminada = null
        viewModelScope.launch {
            try {
                inventario.restaurarCategoria(categoria)
                _avisos.send(
                    AvisoInventario(textos.texto(R.string.categorias_admin_restaurada, categoria.nombre))
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _avisos.send(AvisoInventario(e.aMensajeUsuario(textos)))
            }
        }
    }

    private fun EdicionCategoria.validada(): EdicionCategoria {
        val existentes = (estado.value as? UiState.Exito)?.datos.orEmpty()
        return copy(error = ValidadorCategoria.validar(nombre, existentes, id))
    }

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AdminCategoriasViewModel(
                    categoriaRepository = container.categoriaRepository,
                    inventario = container.inventarioRepository,
                    textos = container.textos
                )
            }
        }
    }
}
