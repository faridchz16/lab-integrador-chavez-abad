package com.tecsup.productos.ui.admin.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.R
import com.tecsup.productos.data.inventario.InventarioRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.catalogo.normalizada
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import com.tecsup.productos.ui.admin.AvisoInventario
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminProductosViewModel(
    repo: ItemRepository,
    private val inventario: InventarioRepository,
    private val textos: ProveedorTextos
) : ViewModel() {

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta.asStateFlow()

    val estado: StateFlow<UiState<List<Item>>> = combine(repo.items, _consulta) { items, consulta ->
        val resultado: UiState<List<Item>> = UiState.Exito(items.filtrar(consulta))
        resultado
    }
        .catch { error -> emit(UiState.Error(error.aMensajeUsuario(textos))) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = UiState.Cargando
        )

    private val _avisos = Channel<AvisoInventario>(Channel.BUFFERED)
    val avisos: Flow<AvisoInventario> = _avisos.receiveAsFlow()

    private var ultimoEliminado: Int? = null

    fun cambiarConsulta(texto: String) {
        _consulta.value = texto
    }

    fun eliminar(item: Item) = ejecutar {
        inventario.eliminarProducto(item.id)
        ultimoEliminado = item.id
        _avisos.send(
            AvisoInventario(
                texto = textos.texto(R.string.inventario_producto_eliminado, item.titulo),
                conDeshacer = true
            )
        )
    }

    fun deshacer() {
        val id = ultimoEliminado ?: return
        ultimoEliminado = null
        ejecutar {
            inventario.restaurarProducto(id)
            _avisos.send(AvisoInventario(textos.texto(R.string.inventario_producto_restaurado)))
        }
    }

    fun procesarResultado(resultado: ResultadoEdicion) {
        val aviso = when (resultado.tipo) {
            TipoResultadoEdicion.CREADO -> AvisoInventario(
                textos.texto(R.string.inventario_producto_creado, resultado.titulo)
            )
            TipoResultadoEdicion.ACTUALIZADO -> AvisoInventario(
                textos.texto(R.string.inventario_producto_actualizado, resultado.titulo)
            )
            TipoResultadoEdicion.ELIMINADO -> {
                ultimoEliminado = resultado.id
                AvisoInventario(
                    texto = textos.texto(R.string.inventario_producto_eliminado, resultado.titulo),
                    conDeshacer = true
                )
            }
        }
        _avisos.trySend(aviso)
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _avisos.send(AvisoInventario(e.aMensajeUsuario(textos)))
            }
        }
    }

    private fun List<Item>.filtrar(consulta: String): List<Item> {
        val termino = consulta.normalizada()
        if (termino.isEmpty()) return this
        return filter { item ->
            listOfNotNull(item.titulo, item.marca, item.categoria)
                .any { it.normalizada().contains(termino) }
        }
    }

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AdminProductosViewModel(
                    repo = container.itemRepository,
                    inventario = container.inventarioRepository,
                    textos = container.textos
                )
            }
        }
    }
}
