package com.tecsup.productos.domain.model

enum class TipoProducto {
    SMARTPHONE,
    TABLET,
    RELOJ,
    ACCESORIO;

    companion object {
        private const val CATEGORIA_TABLETS = "tablets"
        private const val CATEGORIA_ACCESORIOS = "mobile-accessories"
        private const val PALABRA_RELOJ = "watch"

        fun desdeApi(categoria: String?, titulo: String): TipoProducto = when {
            categoria == CATEGORIA_TABLETS -> TABLET
            titulo.contains(PALABRA_RELOJ, ignoreCase = true) -> RELOJ
            categoria == CATEGORIA_ACCESORIOS -> ACCESORIO
            else -> SMARTPHONE
        }

        fun desdeNombre(nombre: String?): TipoProducto =
            entries.firstOrNull { it.name == nombre } ?: SMARTPHONE
    }
}
