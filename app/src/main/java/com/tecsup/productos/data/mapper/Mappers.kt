package com.tecsup.productos.data.mapper

import com.tecsup.productos.data.local.CategoriaConConteo
import com.tecsup.productos.data.local.DetallePedidoEntity
import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.local.LineaCarritoEntity
import com.tecsup.productos.data.local.PedidoConDetalles
import com.tecsup.productos.data.local.PedidoEntity
import com.tecsup.productos.data.remote.dto.ItemDto
import com.tecsup.productos.domain.carrito.LineaCarrito
import com.tecsup.productos.domain.carrito.LineaPedido
import com.tecsup.productos.domain.carrito.Pedido
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.TipoProducto
import com.tecsup.productos.domain.model.dolaresASoles

fun ItemDto.aEntity(categoriaId: Long?, esFavorito: Boolean = false): ItemEntity = ItemEntity(
    id = id,
    titulo = title,
    marca = brand.orEmpty(),
    precio = price.dolaresASoles(),
    imagenUrl = thumbnail,
    rating = rating,
    stock = stock,
    descuento = discountPercentage,
    descripcion = description,
    esFavorito = esFavorito,
    categoriaId = categoriaId,
    tipo = TipoProducto.desdeApi(category, title).name
)

fun ItemEntity.fusionarCon(dto: ItemDto): ItemEntity {
    if (editadoLocalmente) return this
    return copy(
        titulo = dto.title,
        marca = dto.brand.orEmpty(),
        precio = dto.price.dolaresASoles(),
        imagenUrl = dto.thumbnail,
        rating = dto.rating,
        descuento = dto.discountPercentage,
        descripcion = dto.description,
        tipo = TipoProducto.desdeApi(dto.category, dto.title).name
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
    editadoLocalmente = editadoLocalmente,
    tipo = TipoProducto.desdeNombre(tipo)
)

fun ItemDto.aModelo(base: Item): Item =
    if (base.editadoLocalmente) {
        base.copy(imagenes = images)
    } else {
        base.copy(
            titulo = title,
            marca = brand.orEmpty(),
            precio = price.dolaresASoles(),
            imagenUrl = thumbnail,
            descripcion = description,
            rating = rating,
            descuento = discountPercentage,
            imagenes = images,
            tipo = TipoProducto.desdeApi(category, title)
        )
    }

fun ItemDto.aModelo(esFavorito: Boolean = false): Item = Item(
    id = id,
    titulo = title,
    marca = brand.orEmpty(),
    precio = price.dolaresASoles(),
    imagenUrl = thumbnail,
    esFavorito = esFavorito,
    descripcion = description,
    rating = rating,
    stock = stock,
    descuento = discountPercentage,
    imagenes = images,
    tipo = TipoProducto.desdeApi(category, title)
)

fun CategoriaConConteo.aModelo(): Categoria = Categoria(
    id = id,
    nombre = nombre,
    cantidadProductos = cantidad
)

fun LineaCarritoEntity.aModelo(): LineaCarrito = LineaCarrito(
    productoId = productoId,
    titulo = titulo,
    imagenUrl = imagenUrl,
    precioOriginal = precio,
    precioUnitario = if (descuento > 0) precio * (1 - descuento / 100) else precio,
    cantidad = cantidad,
    stockDisponible = stock,
    unidadesPorPedido = unidadesPorPedido
)

fun DetallePedidoEntity.aModelo(): LineaPedido = LineaPedido(
    productoId = productoId,
    titulo = titulo,
    imagenUrl = imagenUrl,
    precioUnitario = precioUnitario,
    cantidad = cantidad
)

fun PedidoEntity.aModelo(detalles: List<DetallePedidoEntity>): Pedido = Pedido(
    id = id,
    fecha = fecha,
    total = total,
    cantidadArticulos = cantidadArticulos,
    lineas = detalles.map { it.aModelo() }
)

fun PedidoConDetalles.aModelo(): Pedido = pedido.aModelo(detalles)
