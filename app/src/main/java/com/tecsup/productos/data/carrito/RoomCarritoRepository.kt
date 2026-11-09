package com.tecsup.productos.data.carrito

import com.tecsup.productos.data.local.CarritoDao
import com.tecsup.productos.data.local.CarritoEntity
import com.tecsup.productos.data.local.ItemDao
import com.tecsup.productos.data.mapper.aModelo
import com.tecsup.productos.domain.carrito.LineaCarrito
import com.tecsup.productos.domain.carrito.ResultadoCarrito
import com.tecsup.productos.domain.carrito.maximoUnidades
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCarritoRepository(
    private val carritoDao: CarritoDao,
    private val itemDao: ItemDao
) : CarritoRepository {

    override val lineas: Flow<List<LineaCarrito>> =
        carritoDao.lineas().map { lista -> lista.map { it.aModelo() } }

    override val cantidadArticulos: Flow<Int> = carritoDao.cantidadArticulos()

    override suspend fun agregar(productoId: Int, cantidad: Int): ResultadoCarrito {
        val producto = itemDao.porId(productoId) ?: return ResultadoCarrito.NoDisponible
        val maximo = maximoUnidades(producto.stock, producto.unidadesPorPedido)
        if (maximo <= 0) return ResultadoCarrito.Agotado(producto.titulo)
        val actual = carritoDao.porProducto(productoId)
        val deseada = (actual?.cantidad ?: 0) + cantidad
        val final = minOf(deseada, maximo)
        if (final != actual?.cantidad) {
            carritoDao.guardar(
                CarritoEntity(
                    productoId = productoId,
                    cantidad = final,
                    agregadoEn = actual?.agregadoEn ?: System.currentTimeMillis()
                )
            )
        }
        return if (deseada > maximo) {
            ResultadoCarrito.LimiteStock(producto.titulo, maximo)
        } else {
            ResultadoCarrito.Agregado(producto.titulo, final)
        }
    }

    override suspend fun cambiarCantidad(productoId: Int, cantidad: Int): ResultadoCarrito {
        if (cantidad <= 0) {
            carritoDao.quitar(productoId)
            return ResultadoCarrito.Actualizado
        }
        val producto = itemDao.porId(productoId) ?: return ResultadoCarrito.NoDisponible
        val actual = carritoDao.porProducto(productoId) ?: return ResultadoCarrito.NoDisponible
        val maximo = maximoUnidades(producto.stock, producto.unidadesPorPedido)
        val final = cantidad.coerceAtMost(maximo)
        if (final <= 0) {
            carritoDao.quitar(productoId)
            return ResultadoCarrito.Agotado(producto.titulo)
        }
        carritoDao.guardar(actual.copy(cantidad = final))
        return if (cantidad > maximo) {
            ResultadoCarrito.LimiteStock(producto.titulo, maximo)
        } else {
            ResultadoCarrito.Actualizado
        }
    }

    override suspend fun quitar(productoId: Int) = carritoDao.quitar(productoId)

    override suspend fun vaciar() = carritoDao.vaciar()
}
