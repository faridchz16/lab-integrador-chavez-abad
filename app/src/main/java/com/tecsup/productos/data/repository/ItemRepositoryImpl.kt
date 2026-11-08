package com.tecsup.productos.data.repository

import com.tecsup.productos.data.local.ItemDao
import com.tecsup.productos.data.mapper.aEntity
import com.tecsup.productos.data.mapper.aModelo
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class ItemRepositoryImpl(
    private val api: ApiService,
    private val dao: ItemDao
) : ItemRepository {

    override val items: Flow<List<Item>> =
        dao.todos().map { lista -> lista.map { it.aModelo() } }

    override val favoritos: Flow<List<Item>> =
        dao.favoritos().map { lista -> lista.map { it.aModelo() } }

    override suspend fun refrescar(): Result<Int> = try {
        val idsFavoritos = dao.idsFavoritos().toSet()
        val nuevos = api.listar().products
            .map { it.aEntity(esFavorito = it.id in idsFavoritos) }
        dao.guardarTodos(nuevos)
        Result.success(nuevos.size)
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    }

    override suspend fun obtenerDetalle(id: Int): Result<Item> = try {
        val esFavorito = dao.esFavorito(id) ?: false
        Result.success(api.detalle(id).aModelo(esFavorito))
    } catch (e: IOException) {
        val guardado = dao.porId(id)
        if (guardado != null) Result.success(guardado.aModelo()) else Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    }

    override suspend fun alternarFavorito(id: Int) = dao.alternarFavorito(id)
    override suspend fun cantidadGuardada(): Int = dao.contar()
    override suspend fun cantidadFavoritos(): Int = dao.contarFavoritos()
    override suspend fun limpiarCache() = dao.borrarTodo()
}