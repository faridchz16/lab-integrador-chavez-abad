package com.tecsup.productos.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY titulo COLLATE NOCASE")
    fun todos(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE esFavorito = 1 ORDER BY titulo COLLATE NOCASE")
    fun favoritos(): Flow<List<ItemEntity>>

    @Query("SELECT id FROM items WHERE esFavorito = 1")
    suspend fun idsFavoritos(): List<Int>

    @Query("SELECT esFavorito FROM items WHERE id = :id")
    suspend fun esFavorito(id: Int): Boolean?

    @Query("SELECT * FROM items")
    suspend fun todosUnaVez(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun porId(id: Int): ItemEntity?

    @Query("SELECT COUNT(*) FROM items")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM items WHERE esFavorito = 1")
    suspend fun contarFavoritos(): Int

    @Upsert
    suspend fun guardarTodos(items: List<ItemEntity>)

    @Query("UPDATE items SET esFavorito = NOT esFavorito WHERE id = :id")
    suspend fun alternarFavorito(id: Int)

    @Query("UPDATE items SET stock = MAX(stock - :unidades, 0) WHERE id = :id")
    suspend fun descontarStock(id: Int, unidades: Int)

    @Query("DELETE FROM items")
    suspend fun borrarTodo()
}