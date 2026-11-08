package com.tecsup.productos.ui.lista

import com.tecsup.productos.domain.model.Item

internal object ProductosDePrueba {
    val lista = listOf(
        Item(
            id = 1,
            titulo = "iPhone 13 Pro",
            marca = "Apple",
            precio = 1099.99,
            imagenUrl = null,
            rating = 4.6,
            stock = 25,
            descuento = 9.5
        ),
        Item(
            id = 2,
            titulo = "Samsung Galaxy S10",
            marca = "Samsung",
            precio = 699.99,
            imagenUrl = null,
            esFavorito = true,
            rating = 4.1,
            stock = 4
        ),
        Item(
            id = 3,
            titulo = "Oppo F19 Pro Plus",
            marca = "Oppo",
            precio = 399.99,
            imagenUrl = null,
            rating = 3.9,
            stock = 0,
            descuento = 15.0
        ),
        Item(
            id = 4,
            titulo = "Realme XT",
            marca = "Realme",
            precio = 349.99,
            imagenUrl = null,
            rating = 4.3,
            stock = 50,
            descuento = 5.0
        )
    )
}
