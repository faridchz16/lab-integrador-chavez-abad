package com.tecsup.productos.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute

fun AnimatedContentTransitionScope<NavBackStackEntry>.entradaNavegacion(): EnterTransition {
    val desde = initialState.indicePestana()
    val hacia = targetState.indicePestana()
    return if (desde != null && hacia != null) {
        slideIntoContainer(direccion(desde, hacia), tween(DURACION_TRANSICION_MS)) +
            fadeIn(tween(DURACION_TRANSICION_MS))
    } else {
        fadeIn(tween(DURACION_TRANSICION_MS))
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.salidaNavegacion(): ExitTransition {
    val desde = initialState.indicePestana()
    val hacia = targetState.indicePestana()
    return if (desde != null && hacia != null) {
        slideOutOfContainer(direccion(desde, hacia), tween(DURACION_TRANSICION_MS)) +
            fadeOut(tween(DURACION_TRANSICION_MS))
    } else {
        fadeOut(tween(DURACION_TRANSICION_MS))
    }
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
