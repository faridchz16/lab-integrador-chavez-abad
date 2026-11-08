package com.tecsup.productos.ui.lista

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ListaViewModel(
    private val repo: ItemRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = _estado.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private var refrescoTerminado = false

    init {
        viewModelScope.launch {
            repo.items.collect { lista ->
                if (lista.isNotEmpty() || refrescoTerminado) {
                    _estado.value = UiState.Exito(lista)
                }
            }
        }
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            if (_estado.value is UiState.Error) _estado.value = UiState.Cargando
            val resultado = repo.refrescar()
            refrescoTerminado = true
            resultado
                .onSuccess {
                    if (_estado.value is UiState.Cargando) _estado.value = UiState.Exito(emptyList())
                    _aviso.value = null
                }
                .onFailure { e ->
                    if (repo.cantidadGuardada() == 0) {
                        // Sin internet y sin nada guardado -> error con Reintentar (PA-09)
                        _estado.value = UiState.Error(e.aMensajeUsuario())
                    } else {
                        // Sin internet pero con datos guardados -> lista + aviso (PA-08)
                        if (_estado.value !is UiState.Exito) {
                            _estado.value = UiState.Exito(repo.items.first())
                        }
                        _aviso.value = "Sin conexión: mostrando datos guardados"
                    }
                }
        }
    }

    fun alternarFavorito(id: Int) {
        viewModelScope.launch { repo.alternarFavorito(id) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ListaViewModel(container.itemRepository) }
        }
    }
}