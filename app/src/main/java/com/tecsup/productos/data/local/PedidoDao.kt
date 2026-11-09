package com.tecsup.productos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY fecha DESC")
    fun pedidosConDetalles(): Flow<List<PedidoConDetalles>>

    @Query("SELECT COUNT(*) FROM pedidos WHERE fecha >= :desde")
    fun cantidadDesde(desde: Long): Flow<Int>

    @Insert
    suspend fun insertar(pedido: PedidoEntity): Long

    @Insert
    suspend fun insertarDetalles(detalles: List<DetallePedidoEntity>)
}
