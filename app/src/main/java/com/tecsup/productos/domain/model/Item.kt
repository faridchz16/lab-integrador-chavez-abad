package com.tecsup.productos.domain.model

data class Item(
    val id: Int,
    val titulo: String,
    val marca: String,
    val precio: Double,
    val imagenUrl: String?,
    val esFavorito: Boolean = false,

    val descripcion: String? = null,
    val rating: Double? = null,
    val stock: Int? = null,
    val descuento: Double? = null,
    val imagenes: List<String> = emptyList()
)