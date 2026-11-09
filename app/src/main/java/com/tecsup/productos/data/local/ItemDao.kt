package com.tecsup.productos.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE eliminado = 0 ORDER BY titulo COLLATE NOCASE")
    fun todos(): Flow<List<ItemEntity>>

    @Query(
        "SELECT * FROM items WHERE esFavorito = 1 AND eliminado = 0 ORDER BY titulo COLLATE NOCASE"
    )
    fun favoritos(): Flow<List<ItemEntity>>

    @Query("SELECT id FROM items WHERE esFavorito = 1 AND eliminado = 0")
    suspend fun idsFavoritos(): List<Int>

    @Query("SELECT esFavorito FROM items WHERE id = :id")
    suspend fun esFavorito(id: Int): Boolean?

    @Query("SELECT * FROM items")
    suspend fun todosUnaVez(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun porId(id: Int): ItemEntity?

    @Query("SELECT COUNT(*) FROM items WHERE eliminado = 0")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM items WHERE esFavorito = 1 AND eliminado = 0")
    suspend fun contarFavoritos(): Int

    @Query("SELECT MIN(id) FROM items")
    suspend fun menorId(): Int?

    @Query("SELECT id FROM items WHERE categoriaId = :categoriaId")
    suspend fun idsPorCategoria(categoriaId: Long): List<Int>

    @Upsert
    suspend fun guardar(item: ItemEntity)

    @Upsert
    suspend fun guardarTodos(items: List<ItemEntity>)

    @Query("UPDATE items SET esFavorito = NOT esFavorito WHERE id = :id")
    suspend fun alternarFavorito(id: Int)

    @Query("UPDATE items SET stock = MAX(stock - :unidades, 0) WHERE id = :id")
    suspend fun descontarStock(id: Int, unidades: Int)

    @Query("UPDATE items SET stock = :stock WHERE id = :id")
    suspend fun actualizarStock(id: Int, stock: Int)

    @Query("UPDATE items SET eliminado = :eliminado WHERE id = :id")
    suspend fun marcarEliminado(id: Int, eliminado: Boolean)

    @Query("UPDATE items SET categoriaId = NULL WHERE categoriaId = :categoriaId")
    suspend fun quitarCategoria(categoriaId: Long)

    @Query("UPDATE items SET categoriaId = :categoriaId WHERE id IN (:ids)")
    suspend fun asignarCategoria(ids: List<Int>, categoriaId: Long)

    @Query("DELETE FROM items")
    suspend fun borrarTodo()
}
