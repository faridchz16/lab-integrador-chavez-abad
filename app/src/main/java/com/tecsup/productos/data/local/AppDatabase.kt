package com.tecsup.productos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ItemEntity::class, CategoriaEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun categoriaDao(): CategoriaDao
}
