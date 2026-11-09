package com.tecsup.productos.data.mapper

import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.remote.dto.ItemDto
import com.tecsup.productos.domain.model.Item

fun ItemDto.aEntity(esFavorito: Boolean = false): ItemEntity = ItemEntity(
    id = id,
    titulo = title,
    marca = brand.orEmpty(),
    precio = price,
    imagenUrl = thumbnail,
    rating = rating,
    stock = stock,
    descuento = discountPercentage,
    descripcion = description,
    esFavorito = esFavorito
)

fun ItemEntity.aModelo(): Item = Item(
    id = id,
    titulo = titulo,
    marca = marca,
    precio = precio,
    imagenUrl = imagenUrl,
    esFavorito = esFavorito,
    descripcion = descripcion,
    rating = rating,
    stock = stock,
    descuento = descuento
)

fun ItemDto.aModelo(esFavorito: Boolean = false): Item = Item(
    id = id,
    titulo = title,
    marca = brand.orEmpty(),
    precio = price,
    imagenUrl = thumbnail,
    esFavorito = esFavorito,
    descripcion = description,
    rating = rating,
    stock = stock,
    descuento = discountPercentage,
    imagenes = images
)