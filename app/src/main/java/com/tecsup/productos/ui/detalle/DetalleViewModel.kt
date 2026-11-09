package com.tecsup.productos.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val repo: ItemRepository,
    private val textos: ProveedorTextos,
    private val id: Int
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<Item>>(UiState.Cargando)
    val estado: StateFlow<UiState<Item>> = _estado.asStateFlow()

    init {
        cargar()
        viewModelScope.launch {
            repo.items.collect { lista ->
                val esFavorito = lista.any { it.id == id && it.esFavorito }
                _estado.update { actual ->
                    if (actual is UiState.Exito) {
                        UiState.Exito(actual.datos.copy(esFavorito = esFavorito))
                    } else {
                        actual
                    }
                }
            }
        }
    }

    fun cargar() {
        viewModelScope.launch {
            val enCache = repo.items.first().firstOrNull { it.id == id }
            _estado.value = if (enCache != null) UiState.Exito(enCache) else UiState.Cargando
            repo.obtenerDetalle(id)
                .onSuccess { item -> _estado.value = UiState.Exito(item) }
                .onFailure { error ->
                    if (enCache == null) _estado.value = UiState.Error(error.aMensajeUsuario(textos))
                }
        }
    }

    fun alternarFavorito() {
        viewModelScope.launch { repo.alternarFavorito(id) }
    }

    companion object {
        fun factory(container: AppContainer, id: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { DetalleViewModel(container.itemRepository, container.textos, id) }
            }
    }
}
