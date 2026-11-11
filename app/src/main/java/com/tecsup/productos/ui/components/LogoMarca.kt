package com.tecsup.productos.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tecsup.productos.domain.catalogo.normalizada

@SuppressLint("DiscouragedApi")
@Composable
fun LogoMarca(
    nombre: String,
    colorFondo: Color,
    colorLetra: Color,
    modifier: Modifier = Modifier,
    tamano: Dp = 52.dp
) {
    val contexto = LocalContext.current
    val recurso = remember(nombre) {
        val clave = PREFIJO_RECURSO + nombre.normalizada().replace(Regex("[^a-z0-9]+"), "_").trim('_')
        contexto.resources.getIdentifier(clave, TIPO_RECURSO, contexto.packageName)
    }
    Surface(
        shape = CircleShape,
        color = if (recurso != 0) Color.White else colorFondo,
        contentColor = colorLetra,
        shadowElevation = if (recurso != 0) 1.dp else 0.dp,
        modifier = modifier.size(tamano)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (recurso != 0) {
                Image(
                    painter = painterResource(recurso),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tamano / RELLENO_RELATIVO)
                )
            } else {
                Text(
                    text = nombre.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private const val PREFIJO_RECURSO = "marca_"
private const val TIPO_RECURSO = "drawable"
private const val RELLENO_RELATIVO = 5
