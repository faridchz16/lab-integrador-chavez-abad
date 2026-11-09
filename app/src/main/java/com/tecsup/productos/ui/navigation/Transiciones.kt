package com.tecsup.productos.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute

fun AnimatedContentTransitionScope<NavBackStackEntry>.entradaNavegacion(): EnterTransition {
    if (targetState.destination.hasRoute(Rutas.Carrito::class)) {
        return scaleIn(tween(DURACION_TRANSICION_MS), initialScale = ESCALA_CARRITO) +
            fadeIn(tween(DURACION_TRANSICION_MS))
    }
    val (desde, hacia) = posiciones()
    return if (desde != null && hacia != null && desde != hacia) {
        slideIntoContainer(direccion(desde, hacia), tween(DURACION_TRANSICION_MS)) +
            fadeIn(tween(DURACION_TRANSICION_MS))
    } else {
        fadeIn(tween(DURACION_TRANSICION_MS))
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.salidaNavegacion(): ExitTransition {
    if (initialState.destination.hasRoute(Rutas.Carrito::class)) {
        return scaleOut(tween(DURACION_TRANSICION_MS), targetScale = ESCALA_CARRITO) +
            fadeOut(tween(DURACION_TRANSICION_MS))
    }
    val (desde, hacia) = posiciones()
    return if (desde != null && hacia != null && desde != hacia) {
        slideOutOfContainer(direccion(desde, hacia), tween(DURACION_TRANSICION_MS)) +
            fadeOut(tween(DURACION_TRANSICION_MS))
    } else {
        fadeOut(tween(DURACION_TRANSICION_MS))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.posiciones(): Pair<Int?, Int?> {
    val pestanaDesde = initialState.indicePestana()
    val pestanaHacia = targetState.indicePestana()
    if (pestanaDesde != null && pestanaHacia != null) return pestanaDesde to pestanaHacia
    return initialState.nivelAdmin() to targetState.nivelAdmin()
}

private fun NavBackStackEntry.nivelAdmin(): Int? = when {
    destination.hasRoute(Rutas.Admin::class) -> 0
    destination.hasRoute(Rutas.AdminProductos::class) -> 1
    destination.hasRoute(Rutas.AdminCategorias::class) -> 1
    destination.hasRoute(Rutas.EditarProducto::class) -> 2
    else -> null
}

private fun NavBackStackEntry.indicePestana(): Int? =
    DestinoPrincipal.entries
        .indexOfFirst { destination.hasRoute(it.ruta::class) }
        .takeIf { it >= 0 }

private fun direccion(desde: Int, hacia: Int) = if (hacia > desde) {
    AnimatedContentTransitionScope.SlideDirection.Start
} else {
    AnimatedContentTransitionScope.SlideDirection.End
}

private const val DURACION_TRANSICION_MS = 350
private const val ESCALA_CARRITO = 0.92f
