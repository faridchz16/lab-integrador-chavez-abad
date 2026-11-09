package com.tecsup.productos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "carrito",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CarritoEntity(
    @PrimaryKey val productoId: Int,
    val cantidad: Int,
    val agregadoEn: Long
)

data class LineaCarritoEntity(
    val productoId: Int,
    val cantidad: Int,
    val titulo: String,
    val imagenUrl: String?,
    val precio: Double,
    val descuento: Double,
    val stock: Int,
    val unidadesPorPedido: Int
)
