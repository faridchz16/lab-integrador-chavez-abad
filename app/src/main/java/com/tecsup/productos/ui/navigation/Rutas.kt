package com.tecsup.productos.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Rutas {
    @Serializable
    data object Login : Rutas

    @Serializable
    data object Lista : Rutas

    @Serializable
    data class Detalle(val id: Int) : Rutas

    @Serializable
    data object Favoritos : Rutas

    @Serializable
    data object Admin : Rutas
}
