package com.tecsup.productos.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tecsup.productos.R

enum class DestinoPrincipal(
    val ruta: Rutas,
    @StringRes val etiqueta: Int,
    @DrawableRes val iconoActivo: Int,
    @DrawableRes val iconoInactivo: Int,
    val soloAdmin: Boolean = false,
    val soloCliente: Boolean = false
) {
    INICIO(
        ruta = Rutas.Inicio,
        etiqueta = R.string.navegacion_inicio,
        iconoActivo = R.drawable.ic_inicio,
        iconoInactivo = R.drawable.ic_inicio_borde,
        soloCliente = true
    ),
    CATALOGO(
        ruta = Rutas.Lista,
        etiqueta = R.string.navegacion_catalogo,
        iconoActivo = R.drawable.ic_catalogo,
        iconoInactivo = R.drawable.ic_catalogo_borde
    ),
    CATEGORIAS(
        ruta = Rutas.Categorias,
        etiqueta = R.string.navegacion_categorias,
        iconoActivo = R.drawable.ic_categoria,
        iconoInactivo = R.drawable.ic_categoria_borde
    ),
    CARRITO(
        ruta = Rutas.Carrito,
        etiqueta = R.string.navegacion_carrito,
        iconoActivo = R.drawable.ic_carrito,
        iconoInactivo = R.drawable.ic_carrito_borde,
        soloCliente = true
    ),
    ADMIN(
        ruta = Rutas.Admin,
        etiqueta = R.string.navegacion_admin,
        iconoActivo = R.drawable.ic_panel_admin,
        iconoInactivo = R.drawable.ic_panel_admin_borde,
        soloAdmin = true
    ),
    ALERTAS(
        ruta = Rutas.Alertas,
        etiqueta = R.string.navegacion_alertas,
        iconoActivo = R.drawable.ic_alertas,
        iconoInactivo = R.drawable.ic_alertas_borde,
        soloAdmin = true
    ),
    AJUSTES(
        ruta = Rutas.Ajustes,
        etiqueta = R.string.navegacion_perfil,
        iconoActivo = R.drawable.ic_persona,
        iconoInactivo = R.drawable.ic_persona_borde
    )
}
