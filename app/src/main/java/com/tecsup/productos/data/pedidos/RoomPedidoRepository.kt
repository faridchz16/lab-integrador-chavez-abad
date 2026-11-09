package com.tecsup.productos.data.pedidos

import androidx.room.withTransaction
import com.tecsup.productos.data.local.AppDatabase
import com.tecsup.productos.data.local.DetallePedidoEntity
import com.tecsup.productos.data.local.PedidoEntity
import com.tecsup.productos.data.mapper.aModelo
import com.tecsup.productos.domain.carrito.ErrorPedido
import com.tecsup.productos.domain.carrito.Pedido
import com.tecsup.productos.domain.model.precioFinal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPedidoRepository(
    private val database: AppDatabase
) : PedidoRepository {

    private val pedidoDao = database.pedidoDao()
    private val carritoDao = database.carritoDao()
    private val itemDao = database.itemDao()

    override val pedidos: Flow<List<Pedido>> =
        pedidoDao.pedidosConDetalles().map { lista -> lista.map { it.aModelo() } }

    override fun pedidosDesde(desde: Long): Flow<Int> = pedidoDao.cantidadDesde(desde)

    override suspend fun confirmarPedido(correoUsuario: String): Result<Pedido> = try {
        val pedido = database.withTransaction {
            val lineas = carritoDao.lineasUnaVez()
            if (lineas.isEmpty()) throw ErrorPedido.CarritoVacio()
            val detalles = lineas.map { linea ->
                val producto = itemDao.porId(linea.productoId)
                    ?: throw ErrorPedido.StockInsuficiente(linea.titulo, 0)
                val unidadesNecesarias = linea.cantidad * producto.unidadesPorPedido
                if (producto.stock < unidadesNecesarias) {
                    throw ErrorPedido.StockInsuficiente(
                        titulo = producto.titulo,
                        disponible = producto.stock / maxOf(producto.unidadesPorPedido, 1)
                    )
                }
                DetallePedidoEntity(
                    pedidoId = 0,
                    productoId = producto.id,
                    titulo = producto.titulo,
                    imagenUrl = producto.imagenUrl,
                    precioUnitario = producto.aModelo().precioFinal(),
                    cantidad = linea.cantidad,
                    unidadesDescontadas = unidadesNecesarias
                )
            }
            val cabecera = PedidoEntity(
                fecha = System.currentTimeMillis(),
                total = detalles.sumOf { it.precioUnitario * it.cantidad },
                cantidadArticulos = detalles.sumOf { it.cantidad },
                correoUsuario = correoUsuario
            )
            val pedidoId = pedidoDao.insertar(cabecera)
            val detallesGuardados = detalles.map { it.copy(pedidoId = pedidoId) }
            pedidoDao.insertarDetalles(detallesGuardados)
            detallesGuardados.forEach { itemDao.descontarStock(it.productoId, it.unidadesDescontadas) }
            carritoDao.vaciar()
            cabecera.copy(id = pedidoId).aModelo(detallesGuardados)
        }
        Result.success(pedido)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
