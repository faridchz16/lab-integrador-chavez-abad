package com.tecsup.productos.ui

import com.tecsup.productos.R
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.domain.model.ErrorAutenticacion
import retrofit2.HttpException
import java.io.IOException

fun Throwable.aMensajeUsuario(textos: ProveedorTextos): String = when (this) {
    is ErrorAutenticacion.CredencialesInvalidas -> textos.texto(R.string.error_credenciales)
    is ErrorAutenticacion.DemasiadosIntentos -> textos.texto(R.string.error_demasiados_intentos)
    is ErrorAutenticacion.SinConexion -> textos.texto(R.string.error_sin_conexion)
    is ErrorAutenticacion.Desconocido -> textos.texto(R.string.error_inicio_sesion)
    is IOException -> textos.texto(R.string.error_sin_conexion)
    is HttpException -> if (code() == CODIGO_NO_ENCONTRADO) {
        textos.texto(R.string.error_no_encontrado)
    } else {
        textos.texto(R.string.error_servidor, code())
    }
    else -> textos.texto(R.string.error_inesperado)
}

private const val CODIGO_NO_ENCONTRADO = 404
