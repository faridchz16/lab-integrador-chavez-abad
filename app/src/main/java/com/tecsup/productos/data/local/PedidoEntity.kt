package com.tecsup.productos.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: Long,
    val total: Double,
    val cantidadArticulos: Int,
    val correoUsuario: String
)

@Entity(
    tableName = "detalles_pedido",
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pedidoId"])]
)
data class DetallePedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pedidoId: Long,
    val productoId: Int,
    val titulo: String,
    val imagenUrl: String?,
    val precioUnitario: Double,
    val cantidad: Int,
    val unidadesDescontadas: Int
)

data class PedidoConDetalles(
    @Embedded val pedido: PedidoEntity,
    @Relation(parentColumn = "id", entityColumn = "pedidoId")
    val detalles: List<DetallePedidoEntity>
)
