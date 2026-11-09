package com.tecsup.productos.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Query(
        """
        SELECT c.productoId AS productoId, c.cantidad AS cantidad, i.titulo AS titulo,
               i.imagenUrl AS imagenUrl, i.precio AS precio, i.descuento AS descuento,
               i.stock AS stock, i.unidadesPorPedido AS unidadesPorPedido
        FROM carrito c
        INNER JOIN items i ON i.id = c.productoId
        ORDER BY c.agregadoEn
        """
    )
    fun lineas(): Flow<List<LineaCarritoEntity>>

    @Query(
        """
        SELECT c.productoId AS productoId, c.cantidad AS cantidad, i.titulo AS titulo,
               i.imagenUrl AS imagenUrl, i.precio AS precio, i.descuento AS descuento,
               i.stock AS stock, i.unidadesPorPedido AS unidadesPorPedido
        FROM carrito c
        INNER JOIN items i ON i.id = c.productoId
        ORDER BY c.agregadoEn
        """
    )
    suspend fun lineasUnaVez(): List<LineaCarritoEntity>

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM carrito")
    fun cantidadArticulos(): Flow<Int>

    @Query("SELECT * FROM carrito WHERE productoId = :productoId")
    suspend fun porProducto(productoId: Int): CarritoEntity?

    @Upsert
    suspend fun guardar(linea: CarritoEntity)

    @Query("DELETE FROM carrito WHERE productoId = :productoId")
    suspend fun quitar(productoId: Int)

    @Query("DELETE FROM carrito")
    suspend fun vaciar()
}
