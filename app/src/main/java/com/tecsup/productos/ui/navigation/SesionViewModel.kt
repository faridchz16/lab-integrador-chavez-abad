package com.tecsup.productos.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.data.auth.AuthRepository
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SesionViewModel(
    private val auth: AuthRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Cargando)
    val estado: StateFlow<UiState<Sesion?>> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            _estado.value = UiState.Exito(auth.sesionActual())
        }
    }

    fun establecer(sesion: Sesion) {
        _estado.value = UiState.Exito(sesion)
    }

    fun cerrarSesion() {
        auth.cerrarSesion()
        _estado.value = UiState.Exito(null)
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { SesionViewModel(container.authRepository) }
        }
    }
}
