package com.tecsup.productos

import android.content.Context
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.data.remote.RetrofitClient
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.repository.ItemRepositoryImpl

class AppContainer(private val context: Context) {

    private val api: ApiService = RetrofitClient.api

    val itemRepository: ItemRepository by lazy {
        ItemRepositoryImpl(api)
    }
}