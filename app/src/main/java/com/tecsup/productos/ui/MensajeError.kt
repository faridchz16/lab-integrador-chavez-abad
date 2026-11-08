package com.tecsup.productos.ui

import com.tecsup.productos.domain.model.ErrorAutenticacion
import retrofit2.HttpException
import java.io.IOException

fun Throwable.aMensajeUsuario(): String = when (this) {
    is ErrorAutenticacion.CredencialesInvalidas -> "Correo o contraseña incorrectos"
    is ErrorAutenticacion.DemasiadosIntentos ->
        "Demasiados intentos. Espera unos minutos e inténtalo de nuevo"
    is ErrorAutenticacion.SinConexion -> "No hay conexión a internet"
    is ErrorAutenticacion.Desconocido -> "No se pudo iniciar sesión. Inténtalo de nuevo"
    is IOException -> "No hay conexión a internet"
    is HttpException -> if (code() == 404) {
        "No se encontró la información solicitada"
    } else {
        "El servidor respondió con error ${code()}"
    }
    else -> "Ocurrió un error inesperado"
}
