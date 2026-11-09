package com.tecsup.productos.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.inventario.cantidadPorReponer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InsigniasViewModel(
    repo: ItemRepository
) : ViewModel() {

    val favoritos: StateFlow<Int> = repo.favoritos
        .map { lista -> lista.size }
        .catch { emit(0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = 0
        )

    val porReponer: StateFlow<Int> = repo.items
        .map { lista -> lista.cantidadPorReponer() }
        .catch { emit(0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = 0
        )

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { InsigniasViewModel(container.itemRepository) }
        }
    }
}
