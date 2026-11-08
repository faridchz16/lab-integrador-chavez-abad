package com.tecsup.productos.ui.admin

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.theme.ProductosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    correo: String,
    estado: UiState<ResumenAdmin>,
    trabajando: Boolean,
    resultado: ResultadoAdmin?,
    onVolver: () -> Unit,
    onSincronizar: () -> Unit,
    onLimpiarCache: () -> Unit,
    onResultadoMostrado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val recursos = LocalContext.current.resources
    var confirmarLimpieza by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(resultado) {
        val actual = resultado ?: return@LaunchedEffect
        val mensaje = when (actual) {
            is ResultadoAdmin.Sincronizado -> recursos.getQuantityString(
                R.plurals.admin_sincronizado,
                actual.cantidad,
                actual.cantidad
            )
            ResultadoAdmin.CacheLimpiada -> recursos.getString(R.string.admin_cache_limpiada)
            is ResultadoAdmin.Error -> actual.mensaje
        }
        snackbarHostState.showSnackbar(mensaje)
        onResultadoMostrado()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.admin_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.detalle_volver)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = estado,
                contentKey = { it::class },
                label = "estadoAdmin"
            ) { actual ->
                when (actual) {
                    UiState.Cargando -> Cargando()
                    is UiState.Error -> ErrorView(
                        mensaje = actual.mensaje,
                        onReintentar = onSincronizar
                    )
                    is UiState.Exito -> ContenidoAdmin(
                        correo = correo,
                        resumen = actual.datos,
                        trabajando = trabajando,
                        onSincronizar = onSincronizar,
                        onLimpiarCache = { confirmarLimpieza = true }
                    )
                }
            }
        }
    }

    if (confirmarLimpieza) {
        AlertDialog(
            onDismissRequest = { confirmarLimpieza = false },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_borrar),
                    contentDescription = null
                )
            },
            title = { Text(text = stringResource(R.string.admin_limpiar_pregunta)) },
            text = { Text(text = stringResource(R.string.admin_limpiar_mensaje)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarLimpieza = false
                        onLimpiarCache()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.admin_limpiar_confirmar),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpieza = false }) {
                    Text(text = stringResource(R.string.admin_cancelar))
                }
            }
        )
    }
}

@Composable
private fun ContenidoAdmin(
    correo: String,
    resumen: ResumenAdmin,
    trabajando: Boolean,
    onSincronizar: () -> Unit,
    onLimpiarCache: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_panel_admin),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.admin_sesion),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = correo,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.admin_resumen),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaEstadistica(
                icono = R.drawable.ic_inventario,
                valor = resumen.totalGuardado,
                etiqueta = stringResource(R.string.admin_total_guardado),
                acento = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                icono = R.drawable.ic_favorito,
                valor = resumen.totalFavoritos,
                etiqueta = stringResource(R.string.admin_total_favoritos),
                acento = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaEstadistica(
                icono = R.drawable.ic_alerta,
                valor = resumen.stockBajo,
                etiqueta = stringResource(R.string.admin_stock_bajo),
                acento = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                icono = R.drawable.ic_agotado,
                valor = resumen.agotados,
                etiqueta = stringResource(R.string.admin_agotados),
                acento = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.admin_acciones),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSincronizar,
            enabled = !trabajando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (trabajando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_sincronizar),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.admin_sincronizar))
            }
        }
        Text(
            text = stringResource(R.string.admin_sincronizar_ayuda),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )
        OutlinedButton(
            onClick = onLimpiarCache,
            enabled = !trabajando,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_borrar),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.admin_limpiar))
        }
        Text(
            text = stringResource(R.string.admin_limpiar_ayuda),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun TarjetaEstadistica(
    icono: Int,
    valor: Int,
    etiqueta: String,
    acento: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = acento
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminScreenPreview() {
    ProductosTheme {
        AdminScreen(
            correo = "admin@productos.com",
            estado = UiState.Exito(
                ResumenAdmin(totalGuardado = 16, totalFavoritos = 3, stockBajo = 2, agotados = 1)
            ),
            trabajando = false,
            resultado = null,
            onVolver = {},
            onSincronizar = {},
            onLimpiarCache = {},
            onResultadoMostrado = {}
        )
    }
}
