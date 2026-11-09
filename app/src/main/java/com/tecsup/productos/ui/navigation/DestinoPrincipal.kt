package com.tecsup.productos.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tecsup.productos.R

enum class DestinoPrincipal(
    val ruta: Rutas,
    @StringRes val etiqueta: Int,
    @DrawableRes val iconoActivo: Int,
    @DrawableRes val iconoInactivo: Int,
    val soloAdmin: Boolean = false
) {
    CATALOGO(
        ruta = Rutas.Lista,
        etiqueta = R.string.navegacion_catalogo,
        iconoActivo = R.drawable.ic_catalogo,
        iconoInactivo = R.drawable.ic_catalogo_borde
    ),
    FAVORITOS(
        ruta = Rutas.Favoritos,
        etiqueta = R.string.navegacion_favoritos,
        iconoActivo = R.drawable.ic_favorito,
        iconoInactivo = R.drawable.ic_favorito_borde
    ),
    ADMIN(
        ruta = Rutas.Admin,
        etiqueta = R.string.navegacion_admin,
        iconoActivo = R.drawable.ic_panel_admin,
        iconoInactivo = R.drawable.ic_panel_admin_borde,
        soloAdmin = true
    ),
    AJUSTES(
        ruta = Rutas.Ajustes,
        etiqueta = R.string.navegacion_ajustes,
        iconoActivo = R.drawable.ic_ajustes,
        iconoInactivo = R.drawable.ic_ajustes_borde
    )
}
