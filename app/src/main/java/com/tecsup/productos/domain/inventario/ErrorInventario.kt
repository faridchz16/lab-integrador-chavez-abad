package com.tecsup.productos.domain.inventario

sealed class ErrorInventario : Exception() {
    class NombreDuplicado(val nombre: String) : ErrorInventario()
    class NoEncontrado : ErrorInventario()
}
