package com.tecsup.productos.domain.model

sealed class ErrorAutenticacion(causa: Throwable? = null) : Exception(causa) {
    class CredencialesInvalidas : ErrorAutenticacion()
    class DemasiadosIntentos : ErrorAutenticacion()
    class SinConexion(causa: Throwable? = null) : ErrorAutenticacion(causa)
    class Desconocido(causa: Throwable? = null) : ErrorAutenticacion(causa)
}
