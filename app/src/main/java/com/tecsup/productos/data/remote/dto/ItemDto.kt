package com.tecsup.productos.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: Int,
    val title: String,
    val description: String = "",
    val price: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val brand: String? = null,
    val category: String? = null,
    val thumbnail: String? = null,
    val images: List<String> = emptyList()
)

@Serializable
data class ProductosRespuestaDto(
    val products: List<ItemDto>,
    val total: Int = 0
)