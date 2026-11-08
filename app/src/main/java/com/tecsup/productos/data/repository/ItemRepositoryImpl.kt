package com.tecsup.productos.data.repository

import com.tecsup.productos.data.mapper.aModelo
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import retrofit2.HttpException
import java.io.IOException

class ItemRepositoryImpl(
    private val api: ApiService
) : ItemRepository {

    private val memoria = MutableStateFlow<List<Item>>(emptyList())

    override val items: Flow<List<Item>> = memoria
    override val favoritos: Flow<List<Item>> = memoria.map { lista -> lista.filter { it.esFavorito } }

    override suspend fun refrescar(): Result<Int> = try {
        val idsFavoritos = memoria.value.filter { it.esFavorito }.map { it.id }.toSet()
        val nuevos = api.listar().products
            .map { it.aModelo(esFavorito = it.id in idsFavoritos) }
        memoria.value = nuevos
        Result.success(nuevos.size)
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    }

    override suspend fun obtenerDetalle(id: Int): Result<Item> = try {
        val esFavorito = memoria.value.any { it.id == id && it.esFavorito }
        Result.success(api.detalle(id).aModelo(esFavorito))
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(e)
    }

    override suspend fun alternarFavorito(id: Int) {
        memoria.update { lista ->
            lista.map { if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it }
        }
    }

    override suspend fun cantidadGuardada(): Int = memoria.value.size
    override suspend fun cantidadFavoritos(): Int = memoria.value.count { it.esFavorito }
    override suspend fun limpiarCache() { memoria.value = emptyList() }
}