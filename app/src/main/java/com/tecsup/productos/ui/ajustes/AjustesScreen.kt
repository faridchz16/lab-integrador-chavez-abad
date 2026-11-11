package com.tecsup.productos.ui.ajustes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.ModoTema
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.components.LogoStockify
import com.tecsup.productos.ui.theme.ProductosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    sesion: Sesion?,
    modoTema: ModoTema,
    onCambiarModoTema: (ModoTema) -> Unit,
    onCerrarSesion: () -> Unit,
    cantidadFavoritos: Int,
    onAbrirFavoritos: () -> Unit,
    onAbrirPedidos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.ajustes_titulo)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (sesion?.rol != Rol.ADMIN) {
                SeccionAjustes(titulo = stringResource(R.string.perfil_actividad)) {
                    FilaAcceso(
                        icono = R.drawable.ic_favorito_borde,
                        titulo = stringResource(R.string.perfil_favoritos),
                        cantidad = cantidadFavoritos,
                        onClick = onAbrirFavoritos
                    )
                    FilaAcceso(
                        icono = R.drawable.ic_recibo,
                        titulo = stringResource(R.string.perfil_pedidos),
                        cantidad = 0,
                        onClick = onAbrirPedidos
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            SeccionAjustes(titulo = stringResource(R.string.ajustes_apariencia)) {
                Text(
                    text = stringResource(R.string.ajustes_tema),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ModoTema.entries.forEachIndexed { indice, modo ->
                        SegmentedButton(
                            selected = modo == modoTema,
                            onClick = { onCambiarModoTema(modo) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = indice,
                                count = ModoTema.entries.size
                            ),
                            label = { Text(text = etiquetaModoTema(modo)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.ajustes_tema_ayuda),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (sesion != null) {
                Spacer(modifier = Modifier.height(16.dp))
                SeccionAjustes(titulo = stringResource(R.string.ajustes_cuenta)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_persona),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = sesion.correo,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = stringResource(
                                    if (sesion.rol == Rol.ADMIN) {
                                        R.string.ajustes_rol_admin
                                    } else {
                                        R.string.ajustes_rol_usuario
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onCerrarSesion,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cerrar_sesion),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.sesion_cerrar))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SeccionAjustes(titulo = stringResource(R.string.ajustes_acerca)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoStockify(tamano = 56.dp, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.ajustes_version),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.ajustes_descripcion_app),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FilaAcceso(
    @DrawableRes icono: Int,
    titulo: String,
    cantidad: Int,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text = titulo) },
        leadingContent = {
            Icon(painter = painterResource(icono), contentDescription = null)
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (cantidad > 0) {
                    Badge { Text(text = cantidad.toString()) }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    painter = painterResource(R.drawable.ic_siguiente),
                    contentDescription = null
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun SeccionAjustes(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            contenido()
        }
    }
}

@Composable
private fun etiquetaModoTema(modo: ModoTema): String = stringResource(
    when (modo) {
        ModoTema.CLARO -> R.string.ajustes_tema_claro
        ModoTema.OSCURO -> R.string.ajustes_tema_oscuro
    }
)

@Preview(showBackground = true)
@Composable
private fun AjustesScreenPreview() {
    ProductosTheme {
        AjustesScreen(
            sesion = Sesion(uid = "1", correo = "admin@productos.com", rol = Rol.ADMIN),
            modoTema = ModoTema.CLARO,
            onCambiarModoTema = {},
            onCerrarSesion = {},
            cantidadFavoritos = 3,
            onAbrirFavoritos = {},
            onAbrirPedidos = {}
        )
    }
}
