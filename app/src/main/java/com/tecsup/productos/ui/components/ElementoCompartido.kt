package com.tecsup.productos.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalAlcanceTransicionCompartida = compositionLocalOf<SharedTransitionScope?> { null }

val LocalAlcanceAnimacionVisible = compositionLocalOf<AnimatedVisibilityScope?> { null }

fun claveImagenProducto(id: Int): String = "producto-imagen-$id"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.elementoCompartido(clave: String): Modifier {
    val transicion = LocalAlcanceTransicionCompartida.current ?: return this
    val visibilidad = LocalAlcanceAnimacionVisible.current ?: return this
    return with(transicion) {
        this@elementoCompartido.sharedElement(
            rememberSharedContentState(key = clave),
            visibilidad
        )
    }
}
