package com.tecsup.productos.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.EstadoStock
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResumenAdmin(
    val totalGuardado: Int,
    val totalFavoritos: Int,
    val stockBajo: Int,
    val agotados: Int
)

sealed interface ResultadoAdmin {
    data class Sincronizado(val cantidad: Int) : ResultadoAdmin
    data object CacheLimpiada : ResultadoAdmin
    data class Error(val mensaje: String) : ResultadoAdmin
}

class AdminViewModel(
    private val repo: ItemRepository,
    private val notificador: Notificador
) : ViewModel() {

    val estado: StateFlow<UiState<ResumenAdmin>> = repo.items
        .map<List<Item>, UiState<ResumenAdmin>> { lista -> UiState.Exito(lista.aResumen()) }
        .catch { error -> emit(UiState.Error(error.aMensajeUsuario())) }
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
                notificador.mostrar(TITULO_NOTIFICACION, "Se guardaron $cantidad elementos")
                ResultadoAdmin.Sincronizado(cantidad)
            },
            onFailure = { error -> ResultadoAdmin.Error(error.aMensajeUsuario()) }
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
                ResultadoAdmin.Error(e.aMensajeUsuario())
            } finally {
                _trabajando.value = false
            }
        }
    }

    private fun List<Item>.aResumen() = ResumenAdmin(
        totalGuardado = size,
        totalFavoritos = count { it.esFavorito },
        stockBajo = count { it.estadoStock() == EstadoStock.STOCK_BAJO },
        agotados = count { it.estadoStock() == EstadoStock.AGOTADO }
    )

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L
        private const val TITULO_NOTIFICACION = "Datos actualizados"

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AdminViewModel(container.itemRepository, container.notificador) }
        }
    }
}
