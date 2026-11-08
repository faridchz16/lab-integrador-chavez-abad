package com.tecsup.productos

import android.content.Context
import androidx.room.Room
import com.tecsup.productos.data.auth.AuthRepository
import com.tecsup.productos.data.auth.FirebaseAuthRepository
import com.tecsup.productos.data.local.AppDatabase
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.data.remote.RetrofitClient
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.repository.ItemRepositoryImpl
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.notification.NotificadorAndroid

class AppContainer(private val context: Context) {

    private val api: ApiService = RetrofitClient.api

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "productos.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val itemRepository: ItemRepository by lazy {
        ItemRepositoryImpl(api, database.itemDao())
    }

    val notificador: Notificador by lazy {
        NotificadorAndroid(context)
    }
}