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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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
                    _estado.value = UiState.Error(mensajeDe(e))
                }
        }
    }

    fun alternarFavorito(id: Int) {
        viewModelScope.launch { repo.alternarFavorito(id) }
    }

    private fun mensajeDe(e: Throwable): String = when (e) {
        is IOException -> "No hay conexión a internet"
        is HttpException -> "El servidor respondió con error ${e.code()}"
        else -> "Ocurrió un error inesperado"
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ListaViewModel(container.itemRepository) }
        }
    }
}