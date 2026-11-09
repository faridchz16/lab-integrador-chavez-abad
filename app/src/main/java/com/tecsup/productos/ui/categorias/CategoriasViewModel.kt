package com.tecsup.productos.ui.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.data.categorias.CategoriaRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.domain.model.Categoria
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

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriasViewModel(
    private val repo: CategoriaRepository,
    private val textos: ProveedorTextos
) : ViewModel() {

    private val recarga = MutableStateFlow(0)

    val estado: StateFlow<UiState<List<Categoria>>> = recarga
        .flatMapLatest {
            repo.categorias
                .map<List<Categoria>, UiState<List<Categoria>>> { lista -> UiState.Exito(lista) }
                .onStart { emit(UiState.Cargando) }
                .catch { error -> emit(UiState.Error(error.aMensajeUsuario(textos))) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_MS),
            initialValue = UiState.Cargando
        )

    fun recargar() {
        recarga.value++
    }

    companion object {
        private const val TIEMPO_ESPERA_MS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoriasViewModel(container.categoriaRepository, container.textos) }
        }
    }
}
