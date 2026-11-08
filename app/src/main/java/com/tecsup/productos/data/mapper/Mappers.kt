package com.tecsup.productos.data.mapper

import com.tecsup.productos.data.remote.dto.ItemDto
import com.tecsup.productos.domain.model.Item

fun ItemDto.aModelo(esFavorito: Boolean = false): Item = Item(
    id = id,
    titulo = title,
    marca = brand ?: "Sin marca",
    precio = price,
    imagenUrl = thumbnail,
    esFavorito = esFavorito,
    descripcion = description,
    rating = rating,
    stock = stock,
    descuento = discountPercentage,
    imagenes = images
)