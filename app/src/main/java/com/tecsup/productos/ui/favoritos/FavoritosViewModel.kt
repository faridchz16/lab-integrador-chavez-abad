package com.tecsup.productos.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritosViewModel(
    private val repo: ItemRepository
) : ViewModel() {

    private val recarga = MutableStateFlow(0)

    val estado: StateFlow<UiState<List<Item>>> = recarga
        .flatMapLatest {
            repo.favoritos
                .map<List<Item>, UiState<List<Item>>> { lista -> UiState.Exito(lista) }
                .onStart { emit(UiState.Cargando) }
                .catch { error -> emit(UiState.Error(error.aMensajeUsuario())) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = UiState.Cargando
        )

    fun recargar() {
        recarga.value++
    }

    fun alternarFavorito(id: Int) {
        viewModelScope.launch { repo.alternarFavorito(id) }
    }

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { FavoritosViewModel(container.itemRepository) }
        }
    }
}
