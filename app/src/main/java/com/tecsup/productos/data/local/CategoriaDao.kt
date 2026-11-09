package com.tecsup.productos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Query(
        """
        SELECT c.id AS id, c.nombre AS nombre, COUNT(i.id) AS cantidad
        FROM categorias c
        LEFT JOIN items i ON i.categoriaId = c.id
        GROUP BY c.id
        ORDER BY c.nombre COLLATE NOCASE
        """
    )
    fun conConteo(): Flow<List<CategoriaConConteo>>

    @Query("SELECT * FROM categorias ORDER BY nombre COLLATE NOCASE")
    fun todas(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun porNombre(nombre: String): CategoriaEntity?

    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun porId(id: Long): CategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(categoria: CategoriaEntity): Long
}
