package com.tecsup.productos.ui.alertas

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
import com.tecsup.productos.domain.inventario.AlertaStock
import com.tecsup.productos.domain.inventario.NivelAlerta
import com.tecsup.productos.domain.inventario.ValidadorProducto
import com.tecsup.productos.domain.inventario.alertas
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

data class EstadoAlertas(
    val alertas: List<AlertaStock>,
    val conteos: Map<NivelAlerta, Int>,
    val total: Int
)

class AlertasViewModel(
    repo: ItemRepository,
    private val inventario: InventarioRepository,
    private val textos: ProveedorTextos
) : ViewModel() {

    private val _filtro = MutableStateFlow<NivelAlerta?>(null)
    val filtro: StateFlow<NivelAlerta?> = _filtro.asStateFlow()

    val estado: StateFlow<UiState<EstadoAlertas>> = combine(repo.items, _filtro) { items, filtro ->
        val todas = items.alertas()
        val resultado: UiState<EstadoAlertas> = UiState.Exito(
            EstadoAlertas(
                alertas = if (filtro == null) todas else todas.filter { it.nivel == filtro },
                conteos = todas.groupingBy { it.nivel }.eachCount(),
                total = todas.size
            )
        )
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

    private var deshacerPendiente: (suspend () -> String)? = null

    fun seleccionarFiltro(nivel: NivelAlerta?) {
        _filtro.value = if (_filtro.value == nivel) null else nivel
    }

    fun reponer(alerta: AlertaStock, unidades: Int) {
        if (unidades <= 0) return
        val producto = alerta.producto
        val anterior = alerta.stock
        val nuevo = (anterior + unidades).coerceAtMost(ValidadorProducto.CANTIDAD_MAXIMA)
        ejecutar {
            inventario.actualizarStock(producto.id, nuevo)
            deshacerPendiente = {
                inventario.actualizarStock(producto.id, anterior)
                textos.texto(R.string.alertas_reposicion_deshecha, producto.titulo)
            }
            _avisos.send(
                AvisoInventario(
                    texto = textos.plural(
                        R.plurals.alertas_repuesto,
                        nuevo - anterior,
                        nuevo - anterior,
                        producto.titulo
                    ),
                    conDeshacer = true
                )
            )
        }
    }

    fun deshacer() {
        val accion = deshacerPendiente ?: return
        deshacerPendiente = null
        ejecutar { _avisos.send(AvisoInventario(accion())) }
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
                deshacerPendiente = {
                    inventario.restaurarProducto(resultado.id)
                    textos.texto(R.string.inventario_producto_restaurado)
                }
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

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AlertasViewModel(
                    repo = container.itemRepository,
                    inventario = container.inventarioRepository,
                    textos = container.textos
                )
            }
        }
    }
}
