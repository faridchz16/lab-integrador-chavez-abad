package com.tecsup.productos.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.R
import com.tecsup.productos.data.categorias.CategoriaRepository
import com.tecsup.productos.data.pedidos.PedidoRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ResultadoAdmin {
    data class Sincronizado(val cantidad: Int) : ResultadoAdmin
    data object CacheLimpiada : ResultadoAdmin
    data class Error(val mensaje: String) : ResultadoAdmin
}

class AdminViewModel(
    private val repo: ItemRepository,
    pedidoRepository: PedidoRepository,
    categoriaRepository: CategoriaRepository,
    private val notificador: Notificador,
    private val textos: ProveedorTextos,
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    val estado: StateFlow<UiState<PanelAdmin>> = combine(
        repo.items,
        pedidoRepository.pedidos,
        categoriaRepository.categorias
    ) { items, pedidos, categorias ->
        val panel: UiState<PanelAdmin> =
            UiState.Exito(construirPanel(items, pedidos, categorias.size, reloj()))
        panel
    }
        .catch { error -> emit(UiState.Error(error.aMensajeUsuario(textos))) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = UiState.Cargando
        )

    private val _trabajando = MutableStateFlow(false)
    val trabajando: StateFlow<Boolean> = _trabajando.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoAdmin?>(null)
    val resultado: StateFlow<ResultadoAdmin?> = _resultado.asStateFlow()

    fun sincronizar() = ejecutar {
        repo.refrescar().fold(
            onSuccess = { cantidad ->
                notificador.mostrar(
                    textos.texto(R.string.notificacion_titulo),
                    textos.plural(R.plurals.notificacion_guardados, cantidad, cantidad)
                )
                ResultadoAdmin.Sincronizado(cantidad)
            },
            onFailure = { error -> ResultadoAdmin.Error(error.aMensajeUsuario(textos)) }
        )
    }

    fun limpiarCache() = ejecutar {
        repo.limpiarCache()
        ResultadoAdmin.CacheLimpiada
    }

    fun resultadoMostrado() {
        _resultado.value = null
    }

    private fun ejecutar(accion: suspend () -> ResultadoAdmin) {
        if (_trabajando.value) return
        viewModelScope.launch {
            _trabajando.value = true
            _resultado.value = try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ResultadoAdmin.Error(e.aMensajeUsuario(textos))
            } finally {
                _trabajando.value = false
            }
        }
    }

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AdminViewModel(
                    repo = container.itemRepository,
                    pedidoRepository = container.pedidoRepository,
                    categoriaRepository = container.categoriaRepository,
                    notificador = container.notificador,
                    textos = container.textos
                )
            }
        }
    }
}
