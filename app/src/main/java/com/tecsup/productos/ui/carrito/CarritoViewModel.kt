package com.tecsup.productos.ui.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.R
import com.tecsup.productos.data.carrito.CarritoRepository
import com.tecsup.productos.data.pedidos.PedidoRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.domain.carrito.Carrito
import com.tecsup.productos.domain.carrito.LineaCarrito
import com.tecsup.productos.domain.carrito.Pedido
import com.tecsup.productos.domain.carrito.ResultadoCarrito
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AvisoCarrito(
    val texto: String,
    val mostrarAccion: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class CarritoViewModel(
    private val carrito: CarritoRepository,
    private val pedidos: PedidoRepository,
    private val textos: ProveedorTextos
) : ViewModel() {

    private val recarga = MutableStateFlow(0)

    val estado: StateFlow<UiState<Carrito>> = recarga
        .flatMapLatest {
            carrito.lineas
                .map<List<LineaCarrito>, UiState<Carrito>> { lineas -> UiState.Exito(Carrito(lineas)) }
                .onStart { emit(UiState.Cargando) }
                .catch { error -> emit(UiState.Error(error.aMensajeUsuario(textos))) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Cargando)

    val cantidadArticulos: StateFlow<Int> = carrito.cantidadArticulos
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _procesando = MutableStateFlow(false)
    val procesando: StateFlow<Boolean> = _procesando.asStateFlow()

    private val _pedidoConfirmado = MutableStateFlow<Pedido?>(null)
    val pedidoConfirmado: StateFlow<Pedido?> = _pedidoConfirmado.asStateFlow()

    private val _avisos = Channel<AvisoCarrito>(Channel.BUFFERED)
    val avisos: Flow<AvisoCarrito> = _avisos.receiveAsFlow()

    fun recargar() {
        recarga.value++
    }

    fun agregar(item: Item, cantidad: Int = 1) {
        viewModelScope.launch { publicar(carrito.agregar(item.id, cantidad)) }
    }

    fun cambiarCantidad(productoId: Int, cantidad: Int) {
        viewModelScope.launch { publicar(carrito.cambiarCantidad(productoId, cantidad)) }
    }

    fun quitar(productoId: Int) {
        viewModelScope.launch { carrito.quitar(productoId) }
    }

    fun vaciar() {
        viewModelScope.launch { carrito.vaciar() }
    }

    fun confirmarPedido(correoUsuario: String) {
        if (_procesando.value) return
        viewModelScope.launch {
            _procesando.value = true
            pedidos.confirmarPedido(correoUsuario)
                .onSuccess { pedido -> _pedidoConfirmado.value = pedido }
                .onFailure { error ->
                    _avisos.send(AvisoCarrito(error.aMensajeUsuario(textos), mostrarAccion = false))
                }
            _procesando.value = false
        }
    }

    fun pedidoMostrado() {
        _pedidoConfirmado.value = null
    }

    private suspend fun publicar(resultado: ResultadoCarrito) {
        val aviso = when (resultado) {
            is ResultadoCarrito.Agregado -> AvisoCarrito(
                textos.texto(R.string.carrito_agregado, resultado.titulo),
                mostrarAccion = true
            )
            is ResultadoCarrito.LimiteStock -> AvisoCarrito(
                textos.plural(
                    R.plurals.carrito_limite_stock,
                    resultado.maximo,
                    resultado.maximo,
                    resultado.titulo
                ),
                mostrarAccion = true
            )
            is ResultadoCarrito.Agotado -> AvisoCarrito(
                textos.texto(R.string.carrito_agotado, resultado.titulo),
                mostrarAccion = false
            )
            ResultadoCarrito.NoDisponible -> AvisoCarrito(
                textos.texto(R.string.carrito_no_disponible),
                mostrarAccion = false
            )
            ResultadoCarrito.Actualizado -> return
        }
        _avisos.send(aviso)
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CarritoViewModel(
                    carrito = container.carritoRepository,
                    pedidos = container.pedidoRepository,
                    textos = container.textos
                )
            }
        }
    }
}
