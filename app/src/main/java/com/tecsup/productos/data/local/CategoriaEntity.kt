package com.tecsup.productos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categorias",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)

data class CategoriaConConteo(
    val id: Long,
    val nombre: String,
    val cantidad: Int
)
