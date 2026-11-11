package com.tecsup.productos.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.R
import com.tecsup.productos.data.categorias.CategoriaRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InicioViewModel(
    private val repo: ItemRepository,
    categoriaRepository: CategoriaRepository,
    private val notificador: Notificador,
    private val textos: ProveedorTextos
) : ViewModel() {

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)

    val estado: StateFlow<UiState<SeccionesInicio>> = combine(
        repo.items,
        categoriaRepository.categorias,
        _sincronizando,
        _error
    ) { items, categorias, sincronizando, error ->
        val resultado: UiState<SeccionesInicio> = when {
            items.isNotEmpty() -> UiState.Exito(construirInicio(items, categorias))
            error != null -> UiState.Error(error)
            sincronizando -> UiState.Cargando
            else -> UiState.Exito(construirInicio(emptyList(), emptyList()))
        }
        resultado
    }
        .catch { e -> emit(UiState.Error(e.aMensajeUsuario(textos))) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = UiState.Cargando
        )

    init {
        viewModelScope.launch {
            if (repo.cantidadGuardada() == 0) sincronizar()
        }
    }

    fun sincronizar() {
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            _error.value = null
            repo.refrescar()
                .onSuccess { cantidad ->
                    notificador.mostrar(
                        textos.texto(R.string.notificacion_titulo),
                        textos.plural(R.plurals.notificacion_guardados, cantidad, cantidad)
                    )
                }
                .onFailure { e ->
                    if (repo.cantidadGuardada() == 0) _error.value = e.aMensajeUsuario(textos)
                }
            _sincronizando.value = false
        }
    }

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                InicioViewModel(
                    repo = container.itemRepository,
                    categoriaRepository = container.categoriaRepository,
                    notificador = container.notificador,
                    textos = container.textos
                )
            }
        }
    }
}
