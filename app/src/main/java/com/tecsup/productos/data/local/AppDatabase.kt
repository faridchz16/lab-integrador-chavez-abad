package com.tecsup.productos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ItemEntity::class,
        CategoriaEntity::class,
        CarritoEntity::class,
        PedidoEntity::class,
        DetallePedidoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun carritoDao(): CarritoDao
    abstract fun pedidoDao(): PedidoDao
}
