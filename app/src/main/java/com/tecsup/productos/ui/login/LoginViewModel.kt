package com.tecsup.productos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.data.auth.AuthRepository
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.domain.validacion.ErrorCampo
import com.tecsup.productos.domain.validacion.ValidadorLogin
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.aMensajeUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioLogin(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: ErrorCampo? = null,
    val errorClave: ErrorCampo? = null
)

class LoginViewModel(
    private val auth: AuthRepository
) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioLogin())
    val formulario: StateFlow<FormularioLogin> = _formulario.asStateFlow()

    private val _estado = MutableStateFlow<UiState<Sesion>?>(null)
    val estado: StateFlow<UiState<Sesion>?> = _estado.asStateFlow()

    fun cambiarCorreo(correo: String) {
        _formulario.update { it.copy(correo = correo, errorCorreo = null) }
        limpiarError()
    }

    fun cambiarClave(clave: String) {
        _formulario.update { it.copy(clave = clave, errorClave = null) }
        limpiarError()
    }

    fun iniciarSesion() {
        if (_estado.value is UiState.Cargando) return
        val actual = _formulario.value
        val errorCorreo = ValidadorLogin.validarCorreo(actual.correo)
        val errorClave = ValidadorLogin.validarClave(actual.clave)
        if (errorCorreo != null || errorClave != null) {
            _formulario.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave) }
            return
        }
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = auth.iniciarSesion(actual.correo.trim(), actual.clave).fold(
                onSuccess = { sesion -> UiState.Exito(sesion) },
                onFailure = { error -> UiState.Error(error.aMensajeUsuario()) }
            )
        }
    }

    private fun limpiarError() {
        if (_estado.value is UiState.Error) _estado.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(container.authRepository) }
        }
    }
}
