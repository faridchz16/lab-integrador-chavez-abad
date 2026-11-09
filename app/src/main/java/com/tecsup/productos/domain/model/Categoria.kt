package com.tecsup.productos.domain.model

data class Categoria(
    val id: Long,
    val nombre: String,
    val cantidadProductos: Int = 0
)
