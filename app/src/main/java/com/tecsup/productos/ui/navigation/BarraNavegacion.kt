package com.tecsup.productos.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.tecsup.productos.R

@Composable
fun BarraNavegacion(
    destinos: List<DestinoPrincipal>,
    destinoActual: NavDestination?,
    insignias: Map<DestinoPrincipal, Int>,
    onSeleccionar: (DestinoPrincipal) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        destinos.forEach { destino ->
            val seleccionado = destinoActual?.hierarchy?.any {
                it.hasRoute(destino.ruta::class)
            } == true
            NavigationBarItem(
                selected = seleccionado,
                onClick = { onSeleccionar(destino) },
                icon = {
                    IconoPestana(
                        destino = destino,
                        seleccionado = seleccionado,
                        cantidad = insignias[destino] ?: 0
                    )
                },
                label = { Text(text = stringResource(destino.etiqueta)) }
            )
        }
    }
}

@Composable
private fun IconoPestana(
    destino: DestinoPrincipal,
    seleccionado: Boolean,
    cantidad: Int
) {
    val escala by animateFloatAsState(
        targetValue = if (seleccionado) ESCALA_SELECCION else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "escalaPestana"
    )
    val descripcionInsignia = pluralStringResource(
        R.plurals.navegacion_insignia,
        cantidad,
        cantidad
    )
    BadgedBox(
        modifier = Modifier.semantics {
            if (cantidad > 0) stateDescription = descripcionInsignia
        },
        badge = {
            if (cantidad > 0) {
                Badge {
                    AnimatedContent(
                        targetState = cantidad,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInVertically { it } + fadeIn()) togetherWith
                                    (slideOutVertically { -it } + fadeOut())
                            } else {
                                (slideInVertically { -it } + fadeIn()) togetherWith
                                    (slideOutVertically { it } + fadeOut())
                            }
                        },
                        label = "contadorInsignia"
                    ) { valor ->
                        Text(text = valor.toString())
                    }
                }
            }
        }
    ) {
        Crossfade(targetState = seleccionado, label = "iconoPestana") { activo ->
            Icon(
                painter = painterResource(
                    if (activo) destino.iconoActivo else destino.iconoInactivo
                ),
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                }
            )
        }
    }
}

private const val ESCALA_SELECCION = 1.15f
