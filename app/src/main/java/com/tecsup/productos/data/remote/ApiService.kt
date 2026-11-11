package com.tecsup.productos.data.remote

import com.tecsup.productos.data.remote.dto.ItemDto
import com.tecsup.productos.data.remote.dto.ProductosRespuestaDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("products/category/smartphones")
    suspend fun listar(): ProductosRespuestaDto

    @GET("products/category/{categoria}")
    suspend fun listarCategoria(@Path("categoria") categoria: String): ProductosRespuestaDto

    @GET("products/{id}")
    suspend fun detalle(@Path("id") id: Int): ItemDto
}