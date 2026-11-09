package com.tecsup.productos

import android.app.Application

class ProductosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.monitorStock.iniciar()
    }
}