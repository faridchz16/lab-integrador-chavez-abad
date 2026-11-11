package com.tecsup.productos.ui.inicio

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.TipoProducto
import com.tecsup.productos.domain.model.precioFinal
import com.tecsup.productos.domain.model.tieneDescuento
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.BotonAgregarCarrito
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.EtiquetaDescuento
import com.tecsup.productos.ui.components.ImagenProducto
import com.tecsup.productos.ui.components.LogoMarca
import com.tecsup.productos.ui.components.LogoStockify
import com.tecsup.productos.ui.components.nombreTipoProducto
import com.tecsup.productos.ui.components.rebote
import com.tecsup.productos.ui.theme.ProductosTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AccionesInicio(
    val onProductoClick: (Item) -> Unit,
    val onAgregarAlCarrito: (Item) -> Unit,
    val onTipoClick: (TipoProducto) -> Unit,
    val onMarcaClick: (Categoria) -> Unit,
    val onVerCatalogo: () -> Unit,
    val onActualizar: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    estado: UiState<SeccionesInicio>,
    acciones: AccionesInicio,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LogoStockify(tamano = 36.dp, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.inicio_eslogan),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = acciones.onVerCatalogo) {
                        Icon(
                            painter = painterResource(R.drawable.ic_buscar),
                            contentDescription = stringResource(R.string.inicio_buscar)
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
            when (estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(mensaje = estado.mensaje, onReintentar = acciones.onActualizar)
                is UiState.Exito -> if (estado.datos.estaVacio) {
                    EstadoVacio(
                        titulo = stringResource(R.string.inicio_vacio_titulo),
                        mensaje = stringResource(R.string.inicio_vacio_mensaje),
                        accion = {
                            Button(onClick = acciones.onActualizar) {
                                Text(text = stringResource(R.string.inicio_actualizar))
                            }
                        }
                    )
                } else {
                    ContenidoInicio(secciones = estado.datos, acciones = acciones)
                }
            }
        }
    }
}

@Composable
private fun ContenidoInicio(
    secciones: SeccionesInicio,
    acciones: AccionesInicio
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (secciones.banners.isNotEmpty()) {
            item(key = CLAVE_CARRUSEL) {
                CarruselInicio(banners = secciones.banners, acciones = acciones)
            }
        }
        item(key = CLAVE_TIPOS) {
            TituloSeccion(texto = stringResource(R.string.inicio_compra_por_tipo))
            CompraPorTipo(
                cantidades = secciones.cantidadPorTipo,
                onTipoClick = acciones.onTipoClick
            )
        }
        if (secciones.ofertas.isNotEmpty()) {
            item(key = CLAVE_OFERTAS) {
                TituloSeccion(
                    texto = stringResource(R.string.inicio_ofertas),
                    onVerTodo = acciones.onVerCatalogo
                )
                FilaProductos(productos = secciones.ofertas, acciones = acciones)
            }
        }
        if (secciones.nuevos.isNotEmpty()) {
            item(key = CLAVE_NUEVOS) {
                TituloSeccion(texto = stringResource(R.string.inicio_nuevos))
                FilaProductos(productos = secciones.nuevos, acciones = acciones)
            }
        }
        if (secciones.marcas.isNotEmpty()) {
            item(key = CLAVE_MARCAS) {
                TituloSeccion(texto = stringResource(R.string.inicio_marcas))
                FilaMarcas(marcas = secciones.marcas, onMarcaClick = acciones.onMarcaClick)
            }
        }
        if (secciones.mejorValorados.isNotEmpty()) {
            item(key = CLAVE_VALORADOS) {
                TituloSeccion(texto = stringResource(R.string.inicio_mejor_valorados))
                FilaProductos(productos = secciones.mejorValorados, acciones = acciones)
            }
        }
        item(key = CLAVE_BENEFICIOS) {
            Beneficios()
        }
        item(key = CLAVE_VER_CATALOGO) {
            OutlinedButton(
                onClick = acciones.onVerCatalogo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
            ) {
                Text(text = stringResource(R.string.inicio_ver_catalogo))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarruselInicio(
    banners: List<BannerInicio>,
    acciones: AccionesInicio
) {
    val estadoCarrusel = rememberPagerState(pageCount = { banners.size })
    LaunchedEffect(estadoCarrusel, banners.size) {
        while (banners.size > 1) {
            delay(INTERVALO_CARRUSEL_MS)
            if (!estadoCarrusel.isScrollInProgress) {
                estadoCarrusel.animateScrollToPage((estadoCarrusel.currentPage + 1) % banners.size)
            }
        }
    }
    Column(modifier = Modifier.padding(top = 8.dp)) {
        HorizontalPager(
            state = estadoCarrusel,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { pagina ->
            when (val banner = banners[pagina]) {
                is BannerInicio.Oferta -> BannerOferta(
                    producto = banner.producto,
                    onClick = { acciones.onProductoClick(banner.producto) },
                    onAgregar = { acciones.onAgregarAlCarrito(banner.producto) }
                )
                is BannerInicio.Tipo -> BannerTipo(
                    banner = banner,
                    onClick = { acciones.onTipoClick(banner.tipo) }
                )
            }
        }
        IndicadorCarrusel(estado = estadoCarrusel, cantidad = banners.size)
    }
}

@Composable
private fun BannerOferta(
    producto: Item,
    onClick: () -> Unit,
    onAgregar: () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaccion,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTO_BANNER.dp)
            .rebote(interaccion)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = stringResource(R.string.inicio_etiqueta_nuevo),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.inicio_oferta_destacada),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = producto.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (producto.tieneDescuento()) {
                    Text(
                        text = stringResource(R.string.producto_precio, producto.precio),
                        style = MaterialTheme.typography.labelMedium,
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = ALFA_TEXTO_SECUNDARIO)
                    )
                }
                Text(
                    text = stringResource(R.string.producto_precio, producto.precioFinal()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAgregar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_carrito_agregar),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.inicio_agregar))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(contentAlignment = Alignment.TopEnd) {
                ImagenCircular(url = producto.imagenUrl, tamano = TAMANO_IMAGEN_BANNER)
                EtiquetaDescuento(item = producto)
            }
        }
    }
}

@Composable
private fun BannerTipo(
    banner: BannerInicio.Tipo,
    onClick: () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaccion,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTO_BANNER.dp)
            .rebote(interaccion)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreTipoProducto(banner.tipo),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(tituloBanner(banner.tipo)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.inicio_banner_desde,
                        stringResource(R.string.producto_precio, banner.precioDesde),
                        pluralStringResource(R.plurals.inicio_productos, banner.cantidad, banner.cantidad)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = stringResource(R.string.inicio_ver_productos))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            ImagenCircular(url = banner.imagenUrl, tamano = TAMANO_IMAGEN_BANNER)
        }
    }
}

@Composable
private fun ImagenCircular(url: String?, tamano: Int) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.size(tamano.dp)
    ) {
        ImagenProducto(
            url = url,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )
    }
}

@Composable
private fun IndicadorCarrusel(estado: PagerState, cantidad: Int) {
    val alcance = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(cantidad) { indice ->
            val activo = estado.currentPage == indice
            val ancho by animateDpAsState(
                targetValue = if (activo) ANCHO_PUNTO_ACTIVO.dp else TAMANO_PUNTO.dp,
                label = "anchoPunto"
            )
            val color by animateColorAsState(
                targetValue = if (activo) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                label = "colorPunto"
            )
            val descripcion = stringResource(R.string.inicio_indicador, indice + 1)
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(ancho)
                    .height(TAMANO_PUNTO.dp)
                    .clip(CircleShape)
                    .background(color)
                    .semantics { contentDescription = descripcion }
                    .clickable { alcance.launch { estado.animateScrollToPage(indice) } }
            )
        }
    }
}

@Composable
private fun TituloSeccion(texto: String, onVerTodo: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (onVerTodo != null) {
            TextButton(onClick = onVerTodo) {
                Text(text = stringResource(R.string.inicio_ver_todo))
            }
        }
    }
}

@Composable
private fun CompraPorTipo(
    cantidades: Map<TipoProducto, Int>,
    onTipoClick: (TipoProducto) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        TipoProducto.entries.forEach { tipo ->
            val cantidad = cantidades[tipo] ?: 0
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(enabled = cantidad > 0) { onTipoClick(tipo) }
                    .padding(vertical = 8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(TAMANO_ICONO_TIPO.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(iconoTipo(tipo)),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = nombreTipoProducto(tipo),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pluralStringResource(R.plurals.inicio_productos, cantidad, cantidad),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FilaProductos(
    productos: List<Item>,
    acciones: AccionesInicio
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = productos, key = { it.id }) { producto ->
            TarjetaProductoCompacta(
                producto = producto,
                onClick = { acciones.onProductoClick(producto) },
                onAgregar = { acciones.onAgregarAlCarrito(producto) }
            )
        }
    }
}

@Composable
private fun TarjetaProductoCompacta(
    producto: Item,
    onClick: () -> Unit,
    onAgregar: () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    ElevatedCard(
        onClick = onClick,
        interactionSource = interaccion,
        modifier = Modifier
            .width(ANCHO_TARJETA.dp)
            .rebote(interaccion)
    ) {
        Box {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ALTO_IMAGEN_TARJETA.dp)
            ) {
                ImagenProducto(
                    url = producto.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }
            EtiquetaDescuento(
                item = producto,
                modifier = Modifier.padding(8.dp)
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = producto.marca,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = producto.titulo,
                style = MaterialTheme.typography.bodyMedium,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    if (producto.tieneDescuento()) {
                        Text(
                            text = stringResource(R.string.producto_precio, producto.precio),
                            style = MaterialTheme.typography.labelSmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = stringResource(R.string.producto_precio, producto.precioFinal()),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    producto.rating?.let { calificacion ->
                        Text(
                            text = stringResource(R.string.inicio_calificacion, calificacion),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                BotonAgregarCarrito(
                    onClick = onAgregar,
                    habilitado = (producto.stock ?: 0) > 0
                )
            }
        }
    }
}

@Composable
private fun FilaMarcas(
    marcas: List<Categoria>,
    onMarcaClick: (Categoria) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = marcas, key = { it.id }) { marca ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onMarcaClick(marca) }
                    .padding(4.dp)
            ) {
                LogoMarca(
                    nombre = marca.nombre,
                    colorFondo = MaterialTheme.colorScheme.primaryContainer,
                    colorLetra = MaterialTheme.colorScheme.primary,
                    tamano = TAMANO_ICONO_TIPO.dp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = marca.nombre,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun Beneficios() {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
            Beneficio(
                icono = R.drawable.ic_dinero,
                texto = stringResource(R.string.inicio_beneficio_soles),
                modifier = Modifier.weight(1f)
            )
            Beneficio(
                icono = R.drawable.ic_inventario,
                texto = stringResource(R.string.inicio_beneficio_stock),
                modifier = Modifier.weight(1f)
            )
            Beneficio(
                icono = R.drawable.ic_sincronizar,
                texto = stringResource(R.string.inicio_beneficio_sin_conexion),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Beneficio(
    @DrawableRes icono: Int,
    texto: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            painter = painterResource(icono),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            minLines = 2
        )
    }
}

@DrawableRes
private fun iconoTipo(tipo: TipoProducto): Int = when (tipo) {
    TipoProducto.SMARTPHONE -> R.drawable.ic_smartphone
    TipoProducto.TABLET -> R.drawable.ic_tablet
    TipoProducto.RELOJ -> R.drawable.ic_reloj
    TipoProducto.ACCESORIO -> R.drawable.ic_audifonos
}

private fun tituloBanner(tipo: TipoProducto): Int = when (tipo) {
    TipoProducto.SMARTPHONE -> R.string.inicio_banner_smartphone
    TipoProducto.TABLET -> R.string.inicio_banner_tablet
    TipoProducto.RELOJ -> R.string.inicio_banner_reloj
    TipoProducto.ACCESORIO -> R.string.inicio_banner_accesorio
}

private const val CLAVE_CARRUSEL = "carrusel"
private const val CLAVE_TIPOS = "tipos"
private const val CLAVE_OFERTAS = "ofertas"
private const val CLAVE_NUEVOS = "nuevos"
private const val CLAVE_MARCAS = "marcas"
private const val CLAVE_VALORADOS = "valorados"
private const val CLAVE_BENEFICIOS = "beneficios"
private const val CLAVE_VER_CATALOGO = "ver_catalogo"
private const val INTERVALO_CARRUSEL_MS = 4_500L
private const val ALTO_BANNER = 210
private const val TAMANO_IMAGEN_BANNER = 112
private const val TAMANO_PUNTO = 8
private const val ANCHO_PUNTO_ACTIVO = 24
private const val TAMANO_ICONO_TIPO = 60
private const val ANCHO_TARJETA = 164
private const val ALTO_IMAGEN_TARJETA = 120
private const val ALFA_TEXTO_SECUNDARIO = 0.75f

@Preview(showBackground = true)
@Composable
private fun InicioScreenPreview() {
    val productos = listOf(
        Item(id = 1, titulo = "iPad mini 2021", marca = "Apple", precio = 1729.97, imagenUrl = null, stock = 20, descuento = 12.0, rating = 4.6, tipo = TipoProducto.TABLET),
        Item(id = 2, titulo = "Galaxy S10", marca = "Samsung", precio = 2420.0, imagenUrl = null, stock = 30, descuento = 8.0, rating = 4.2),
        Item(id = 3, titulo = "Apple Watch Series 4", marca = "Apple", precio = 1210.0, imagenUrl = null, stock = 12, descuento = 6.0, rating = 4.8, tipo = TipoProducto.RELOJ)
    )
    ProductosTheme {
        InicioScreen(
            estado = UiState.Exito(
                construirInicio(productos, listOf(Categoria(id = 1, nombre = "Apple", cantidadProductos = 2)))
            ),
            acciones = AccionesInicio(
                onProductoClick = {},
                onAgregarAlCarrito = {},
                onTipoClick = {},
                onMarcaClick = {},
                onVerCatalogo = {},
                onActualizar = {}
            )
        )
    }
}
