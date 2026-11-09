package com.tecsup.productos.data.mapper

import com.tecsup.productos.data.local.CategoriaConConteo
import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.remote.dto.ItemDto
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item

fun ItemDto.aEntity(categoriaId: Long?, esFavorito: Boolean = false): ItemEntity = ItemEntity(
    id = id,
    titulo = title,
    marca = brand.orEmpty(),
    precio = price,
    imagenUrl = thumbnail,
    rating = rating,
    stock = stock,
    descuento = discountPercentage,
    descripcion = description,
    esFavorito = esFavorito,
    categoriaId = categoriaId
)

fun ItemEntity.fusionarCon(dto: ItemDto, categoriaId: Long?): ItemEntity {
    val categoria = this.categoriaId ?: categoriaId
    if (editadoLocalmente) return copy(categoriaId = categoria, imagenUrl = dto.thumbnail)
    return copy(
        titulo = dto.title,
        marca = dto.brand.orEmpty(),
        precio = dto.price,
        imagenUrl = dto.thumbnail,
        rating = dto.rating,
        descuento = dto.discountPercentage,
        descripcion = dto.description,
        categoriaId = categoria
    )
}

fun ItemEntity.aModelo(nombreCategoria: String? = null): Item = Item(
    id = id,
    titulo = titulo,
    marca = marca,
    precio = precio,
    imagenUrl = imagenUrl,
    esFavorito = esFavorito,
    descripcion = descripcion,
    rating = rating,
    stock = stock,
    descuento = descuento,
    categoriaId = categoriaId,
    categoria = nombreCategoria,
    stockMinimo = stockMinimo,
    stockMaximo = stockMaximo,
    unidadesPorPedido = unidadesPorPedido,
    editadoLocalmente = editadoLocalmente
)

fun ItemDto.aModelo(base: Item): Item =
    if (base.editadoLocalmente) {
        base.copy(imagenes = images)
    } else {
        base.copy(
            titulo = title,
            marca = brand.orEmpty(),
            precio = price,
            imagenUrl = thumbnail,
            descripcion = description,
            rating = rating,
            descuento = discountPercentage,
            imagenes = images
        )
    }

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

fun CategoriaConConteo.aModelo(): Categoria = Categoria(
    id = id,
    nombre = nombre,
    cantidadProductos = cantidad
)
