package com.tecsup.productos.ui

import retrofit2.HttpException
import java.io.IOException

fun Throwable.aMensajeUsuario(): String = when (this) {
    is IOException -> "No hay conexión a internet"
    is HttpException -> if (code() == 404) {
        "No se encontró la información solicitada"
    } else {
        "El servidor respondió con error ${code()}"
    }
    else -> "Ocurrió un error inesperado"
}
