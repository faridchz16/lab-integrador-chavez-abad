package com.tecsup.productos.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    var fallo by remember(recurso) { mutableStateOf(false) }
    val conLogo = recurso != 0 && !fallo
    Surface(
        shape = CircleShape,
        color = if (conLogo) Color.White else colorFondo,
        contentColor = colorLetra,
        shadowElevation = if (conLogo) 1.dp else 0.dp,
        modifier = modifier.size(tamano)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (conLogo) {
                AsyncImage(
                    model = ImageRequest.Builder(contexto)
                        .data(recurso)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    onError = { fallo = true },
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
