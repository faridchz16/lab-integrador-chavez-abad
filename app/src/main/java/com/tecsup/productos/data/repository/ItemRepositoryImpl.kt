package com.tecsup.productos.data.repository

import com.tecsup.productos.data.local.CategoriaDao
import com.tecsup.productos.data.local.CategoriaEntity
import com.tecsup.productos.data.local.ItemDao
import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.mapper.aEntity
import com.tecsup.productos.data.mapper.aModelo
import com.tecsup.productos.data.mapper.fusionarCon
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.data.remote.dto.ItemDto
import com.tecsup.productos.data.remote.enEspanol
import com.tecsup.productos.domain.model.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class ItemRepositoryImpl(
    private val api: ApiService,
    private val dao: ItemDao,
    private val categoriaDao: CategoriaDao,
    private val nombreSinCategoria: String
) : ItemRepository {

    override val items: Flow<List<Item>> =
        combine(dao.todos(), categoriaDao.todas()) { entidades, categorias ->
            entidades.conNombreCategoria(categorias)
        }

    override val favoritos: Flow<List<Item>> =
        combine(dao.favoritos(), categoriaDao.todas()) { entidades, categorias ->
            entidades.conNombreCategoria(categorias)
        }

    override suspend fun refrescar(): Result<Int> = try {
        val existentes = dao.todosUnaVez().associateBy { it.id }
        val productos = (api.listar().products + productosAdicionales())
            .distinctBy { it.id }
            .map { it.enEspanol() }
        val actualizados = productos.map { dto ->
            val previo = existentes[dto.id]
            previo?.fusionarCon(dto) ?: dto.aEntity(obtenerOCrearCategoria(dto.brand))
        }
        dao.guardarTodos(actualizados)
        Result.success(actualizados.count { !it.eliminado })
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: SerializationException) {
        Result.failure(e)
    }

    private suspend fun productosAdicionales(): List<ItemDto> =
        CATEGORIAS_ADICIONALES.flatMap { categoria ->
            try {
                api.listarCategoria(categoria).products
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
        }.filter { dto -> MARCAS_DEL_CATALOGO.any { it.equals(dto.brand, ignoreCase = true) } }

    override suspend fun obtenerDetalle(id: Int): Result<Item> {
        val guardado = dao.porId(id)?.let { entidad ->
            entidad.aModelo(entidad.categoriaId?.let { categoriaDao.porId(it)?.nombre })
        }
        if (id < 0) {
            return guardado?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException())
        }
        return try {
            val dto = api.detalle(id).enEspanol()
            Result.success(guardado?.let { dto.aModelo(it) } ?: dto.aModelo())
        } catch (e: IOException) {
            if (guardado != null) Result.success(guardado) else Result.failure(e)
        } catch (e: HttpException) {
            if (guardado != null) Result.success(guardado) else Result.failure(e)
        } catch (e: SerializationException) {
            if (guardado != null) Result.success(guardado) else Result.failure(e)
        }
    }

    override suspend fun alternarFavorito(id: Int) = dao.alternarFavorito(id)
    override suspend fun cantidadGuardada(): Int = dao.contar()
    override suspend fun cantidadFavoritos(): Int = dao.contarFavoritos()
    override suspend fun limpiarCache() = dao.borrarTodo()

    private suspend fun obtenerOCrearCategoria(marca: String?): Long? {
        val nombre = marca?.trim()?.takeIf { it.isNotEmpty() } ?: nombreSinCategoria
        categoriaDao.porNombre(nombre)?.let { return it.id }
        val nuevoId = categoriaDao.insertar(CategoriaEntity(nombre = nombre))
        return if (nuevoId > 0) nuevoId else categoriaDao.porNombre(nombre)?.id
    }

    private fun List<ItemEntity>.conNombreCategoria(
        categorias: List<CategoriaEntity>
    ): List<Item> {
        val nombres = categorias.associate { it.id to it.nombre }
        return map { it.aModelo(nombres[it.categoriaId]) }
    }

    private companion object {
        val CATEGORIAS_ADICIONALES = listOf("tablets", "mobile-accessories")
        val MARCAS_DEL_CATALOGO = setOf("Apple", "Samsung", "Oppo", "Vivo", "Xiaomi", "Realme")
    }
}
