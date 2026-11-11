package com.tecsup.productos.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Rutas {
    @Serializable
    data object Login : Rutas

    @Serializable
    data object Inicio : Rutas

    @Serializable
    data object Lista : Rutas

    @Serializable
    data class Detalle(val id: Int, val imagen: String? = null) : Rutas

    @Serializable
    data object Categorias : Rutas

    @Serializable
    data object Favoritos : Rutas

    @Serializable
    data object Carrito : Rutas

    @Serializable
    data object Pedidos : Rutas

    @Serializable
    data object Admin : Rutas

    @Serializable
    data object AdminProductos : Rutas

    @Serializable
    data class EditarProducto(val id: Int = 0) : Rutas

    @Serializable
    data object AdminCategorias : Rutas

    @Serializable
    data object Alertas : Rutas

    @Serializable
    data object Ajustes : Rutas
}
