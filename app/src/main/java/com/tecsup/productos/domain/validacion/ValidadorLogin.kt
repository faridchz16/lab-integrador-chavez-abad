package com.tecsup.productos.domain.validacion

enum class ErrorCampo { VACIO, FORMATO_INVALIDO, MUY_CORTA }

object ValidadorLogin {

    const val LONGITUD_MINIMA_CLAVE = 6

    private val PATRON_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarCorreo(correo: String): ErrorCampo? = when {
        correo.isBlank() -> ErrorCampo.VACIO
        !PATRON_CORREO.matches(correo.trim()) -> ErrorCampo.FORMATO_INVALIDO
        else -> null
    }

    fun validarClave(clave: String): ErrorCampo? = when {
        clave.isBlank() -> ErrorCampo.VACIO
        clave.length < LONGITUD_MINIMA_CLAVE -> ErrorCampo.MUY_CORTA
        else -> null
    }
}
