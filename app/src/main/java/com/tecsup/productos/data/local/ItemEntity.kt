package com.tecsup.productos.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tecsup.productos.domain.model.STOCK_MAXIMO_PREDETERMINADO
import com.tecsup.productos.domain.model.STOCK_MINIMO_PREDETERMINADO
import com.tecsup.productos.domain.model.UNIDADES_POR_PEDIDO_PREDETERMINADAS

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val marca: String,
    val precio: Double,
    val imagenUrl: String?,
    val rating: Double,
    val stock: Int,
    val descuento: Double,
    val descripcion: String = "",
    val esFavorito: Boolean = false,
    val categoriaId: Long? = null,
    @ColumnInfo(defaultValue = "10")
    val stockMinimo: Int = STOCK_MINIMO_PREDETERMINADO,
    @ColumnInfo(defaultValue = "80")
    val stockMaximo: Int = STOCK_MAXIMO_PREDETERMINADO,
    @ColumnInfo(defaultValue = "1")
    val unidadesPorPedido: Int = UNIDADES_POR_PEDIDO_PREDETERMINADAS,
    @ColumnInfo(defaultValue = "0")
    val editadoLocalmente: Boolean = false
)
