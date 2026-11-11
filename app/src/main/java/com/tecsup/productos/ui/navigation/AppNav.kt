package com.tecsup.productos.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tecsup.productos.AppContainer
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.ModoTema
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.admin.AdminScreen
import com.tecsup.productos.ui.admin.AdminViewModel
import com.tecsup.productos.ui.admin.ResultadoEdicion
import com.tecsup.productos.ui.admin.TipoResultadoEdicion
import com.tecsup.productos.ui.admin.categorias.AdminCategoriasScreen
import com.tecsup.productos.ui.admin.categorias.AdminCategoriasViewModel
import com.tecsup.productos.ui.admin.productos.AdminProductosScreen
import com.tecsup.productos.ui.admin.productos.AdminProductosViewModel
import com.tecsup.productos.ui.admin.productos.EditarProductoScreen
import com.tecsup.productos.ui.admin.productos.EditarProductoViewModel
import com.tecsup.productos.ui.ajustes.AjustesScreen
import com.tecsup.productos.ui.alertas.AlertasScreen
import com.tecsup.productos.ui.alertas.AlertasViewModel
import com.tecsup.productos.ui.ajustes.AjustesViewModel
import com.tecsup.productos.ui.carrito.CarritoScreen
import com.tecsup.productos.ui.carrito.CarritoViewModel
import com.tecsup.productos.ui.categorias.CategoriasScreen
import com.tecsup.productos.ui.categorias.CategoriasViewModel
import com.tecsup.productos.ui.components.DialogoCerrarSesion
import com.tecsup.productos.ui.components.LocalAlcanceAnimacionVisible
import com.tecsup.productos.ui.components.LocalAlcanceTransicionCompartida
import com.tecsup.productos.ui.components.LogoStockify
import com.tecsup.productos.ui.components.SolicitarPermisoNotificaciones
import com.tecsup.productos.ui.detalle.DetalleScreen
import com.tecsup.productos.ui.detalle.DetalleViewModel
import com.tecsup.productos.ui.inicio.AccionesInicio
import com.tecsup.productos.ui.inicio.InicioScreen
import com.tecsup.productos.ui.inicio.InicioViewModel
import com.tecsup.productos.ui.favoritos.FavoritosScreen
import com.tecsup.productos.ui.favoritos.FavoritosViewModel
import com.tecsup.productos.ui.lista.AccionesCatalogo
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel
import com.tecsup.productos.ui.login.LoginScreen
import com.tecsup.productos.ui.login.LoginViewModel
import com.tecsup.productos.ui.pedidos.PedidosScreen
import com.tecsup.productos.ui.pedidos.PedidosViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNav(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val sesionViewModel: SesionViewModel = viewModel(
        factory = SesionViewModel.factory(container)
    )
    val estadoSesion by sesionViewModel.estado.collectAsStateWithLifecycle()

    SolicitarPermisoNotificaciones()

    when (val estado = estadoSesion) {
        is UiState.Exito -> NavegacionPrincipal(
            container = container,
            sesion = estado.datos,
            sesionViewModel = sesionViewModel,
            modifier = modifier
        )
        else -> Surface(modifier = modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                LogoStockify(tamano = 120.dp)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
private fun NavegacionPrincipal(
    container: AppContainer,
    sesion: Sesion?,
    sesionViewModel: SesionViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val destinoInicial: Rutas = remember {
        when {
            sesion == null -> Rutas.Login
            sesion.rol == Rol.ADMIN -> Rutas.Admin
            else -> Rutas.Inicio
        }
    }
    val sesionVigente by rememberUpdatedState(sesion)
    var mostrarDialogoSalir by rememberSaveable { mutableStateOf(false) }

    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val esAdmin = sesion?.rol == Rol.ADMIN
    val raizPestanas: Rutas = if (esAdmin) Rutas.Admin else Rutas.Inicio
    val destinosVisibles = remember(esAdmin) {
        DestinoPrincipal.entries.filter { if (esAdmin) !it.soloCliente else !it.soloAdmin }
    }
    val mostrarBarra = sesion != null && destinosVisibles.any { destino ->
        destinoActual?.hierarchy?.any { it.hasRoute(destino.ruta::class) } == true
    }

    val insigniasViewModel: InsigniasViewModel = viewModel(
        factory = InsigniasViewModel.factory(container)
    )
    val cantidadFavoritos by insigniasViewModel.favoritos.collectAsStateWithLifecycle()
    val cantidadPorReponer by insigniasViewModel.porReponer.collectAsStateWithLifecycle()

    val peticionCatalogo: PeticionCatalogoViewModel = viewModel()

    val carritoViewModel: CarritoViewModel = viewModel(
        factory = CarritoViewModel.factory(container)
    )
    val cantidadCarrito by carritoViewModel.cantidadArticulos.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val textoVerCarrito = stringResource(R.string.carrito_ver)

    LaunchedEffect(carritoViewModel) {
        carritoViewModel.avisos.collect { aviso ->
            launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val resultado = snackbarHostState.showSnackbar(
                    message = aviso.texto,
                    actionLabel = if (aviso.mostrarAccion) textoVerCarrito else null,
                    duration = SnackbarDuration.Short
                )
                if (resultado == SnackbarResult.ActionPerformed) {
                    navController.irAPestana(DestinoPrincipal.CARRITO, raizPestanas)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = mostrarBarra,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                BarraNavegacion(
                    destinos = destinosVisibles,
                    destinoActual = destinoActual,
                    insignias = mapOf(
                        DestinoPrincipal.CARRITO to cantidadCarrito,
                        DestinoPrincipal.ALERTAS to cantidadPorReponer
                    ),
                    onSeleccionar = { destino -> navController.irAPestana(destino, raizPestanas) }
                )
            }
        }
    ) { relleno ->
        SharedTransitionLayout {
            CompositionLocalProvider(LocalAlcanceTransicionCompartida provides this) {
                NavHost(
                    navController = navController,
                    startDestination = destinoInicial,
                    modifier = Modifier
                        .padding(bottom = relleno.calculateBottomPadding())
                        .consumeWindowInsets(relleno),
                    enterTransition = { entradaNavegacion() },
                    exitTransition = { salidaNavegacion() },
                    popEnterTransition = { entradaNavegacion() },
                    popExitTransition = { salidaNavegacion() }
                ) {
                    pantalla<Rutas.Login> {
                        val viewModel: LoginViewModel = viewModel(
                            factory = LoginViewModel.factory(container)
                        )
                        val formulario by viewModel.formulario.collectAsStateWithLifecycle()
                        val estado by viewModel.estado.collectAsStateWithLifecycle()

                        LaunchedEffect(estado) {
                            val actual = estado
                            if (actual is UiState.Exito) {
                                sesionViewModel.establecer(actual.datos)
                                val destinoTrasLogin: Rutas = if (actual.datos.rol == Rol.ADMIN) {
                                    Rutas.Admin
                                } else {
                                    Rutas.Inicio
                                }
                                navController.navigate(destinoTrasLogin) {
                                    popUpTo<Rutas.Login> { inclusive = true }
                                }
                            }
                        }

                        LoginScreen(
                            formulario = formulario,
                            estado = estado,
                            onCorreoChange = viewModel::cambiarCorreo,
                            onClaveChange = viewModel::cambiarClave,
                            onIngresar = viewModel::iniciarSesion
                        )
                    }
                    pantalla<Rutas.Inicio> {
                        val viewModel: InicioViewModel = viewModel(
                            factory = InicioViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val acciones = remember(viewModel, raizPestanas) {
                            AccionesInicio(
                                onProductoClick = { item ->
                                    navController.navigate(Rutas.Detalle(item.id, item.imagenUrl))
                                },
                                onAgregarAlCarrito = { item -> carritoViewModel.agregar(item) },
                                onTipoClick = { tipo ->
                                    peticionCatalogo.pedir(PeticionCatalogo.Tipo(tipo))
                                    navController.irAPestana(DestinoPrincipal.CATALOGO, raizPestanas)
                                },
                                onMarcaClick = { categoria ->
                                    peticionCatalogo.pedir(PeticionCatalogo.Categoria(categoria.id))
                                    navController.irAPestana(DestinoPrincipal.CATALOGO, raizPestanas)
                                },
                                onVerCatalogo = {
                                    peticionCatalogo.pedir(PeticionCatalogo.Todo)
                                    navController.irAPestana(DestinoPrincipal.CATALOGO, raizPestanas)
                                },
                                onActualizar = viewModel::sincronizar
                            )
                        }
                        InicioScreen(estado = estado, acciones = acciones)
                    }
                    pantalla<Rutas.Lista> {
                        val viewModel: ListaViewModel = viewModel(
                            factory = ListaViewModel.factory(container)
                        )
                        val peticion by peticionCatalogo.peticion.collectAsStateWithLifecycle()
                        LaunchedEffect(peticion) {
                            when (val actual = peticion) {
                                is PeticionCatalogo.Categoria -> viewModel.mostrarCategoria(actual.id)
                                is PeticionCatalogo.Tipo -> viewModel.mostrarTipo(actual.tipo)
                                PeticionCatalogo.Todo -> viewModel.limpiarTodo()
                                null -> return@LaunchedEffect
                            }
                            peticionCatalogo.atendida()
                        }
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val aviso by viewModel.aviso.collectAsStateWithLifecycle()
                        val filtro by viewModel.filtro.collectAsStateWithLifecycle()
                        val categorias by viewModel.categorias.collectAsStateWithLifecycle()
                        val historial by viewModel.historial.collectAsStateWithLifecycle()
                        val rangoPrecios by viewModel.rangoPrecios.collectAsStateWithLifecycle()
                        val refrescando by viewModel.refrescando.collectAsStateWithLifecycle()
                        val acciones = remember(viewModel, esAdmin) {
                            AccionesCatalogo(
                                onReintentar = viewModel::refrescar,
                                onProductoClick = { item ->
                                    navController.navigate(Rutas.Detalle(item.id, item.imagenUrl))
                                },
                                onAlternarFavorito = if (esAdmin) null else viewModel::alternarFavorito,
                                onAgregarAlCarrito = if (esAdmin) {
                                    null
                                } else {
                                    { item -> carritoViewModel.agregar(item) }
                                },
                                onEditar = if (esAdmin) {
                                    { item -> navController.navigate(Rutas.EditarProducto(item.id)) }
                                } else {
                                    null
                                },
                                onConsultaChange = viewModel::cambiarConsulta,
                                onBuscar = viewModel::confirmarBusqueda,
                                onEliminarHistorial = viewModel::eliminarDelHistorial,
                                onLimpiarHistorial = viewModel::limpiarHistorial,
                                onSeleccionarCategoria = viewModel::seleccionarCategoria,
                                onAplicarFiltros = viewModel::aplicarFiltros,
                                onLimpiarFiltros = viewModel::limpiarFiltros,
                                onLimpiarConsulta = viewModel::limpiarConsulta,
                                onLimpiarTodo = viewModel::limpiarTodo
                            )
                        }
                        ListaScreen(
                            estado = estado,
                            aviso = aviso,
                            filtro = filtro,
                            categorias = categorias,
                            historial = historial,
                            rangoPrecios = rangoPrecios,
                            acciones = acciones,
                            refrescando = refrescando
                        )
                    }
                    pantalla<Rutas.Categorias> {
                        val viewModel: CategoriasViewModel = viewModel(
                            factory = CategoriasViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        CategoriasScreen(
                            estado = estado,
                            onReintentar = viewModel::recargar,
                            onCategoriaClick = { categoria ->
                                peticionCatalogo.pedir(PeticionCatalogo.Categoria(categoria.id))
                                navController.irAPestana(DestinoPrincipal.CATALOGO, raizPestanas)
                            }
                        )
                    }
                    pantalla<Rutas.Detalle> { entrada ->
                        val ruta = entrada.toRoute<Rutas.Detalle>()
                        val viewModel: DetalleViewModel = viewModel(
                            factory = DetalleViewModel.factory(container, ruta.id)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val resultadoEdicion by entrada.savedStateHandle
                            .getStateFlow<String?>(ResultadoEdicion.CLAVE, null)
                            .collectAsStateWithLifecycle()
                        LaunchedEffect(resultadoEdicion) {
                            val resultado = ResultadoEdicion.decodificar(resultadoEdicion)
                                ?: return@LaunchedEffect
                            entrada.savedStateHandle.set<String>(ResultadoEdicion.CLAVE, null)
                            if (resultado.tipo == TipoResultadoEdicion.ELIMINADO) {
                                navController.navigateUp()
                            } else {
                                viewModel.cargar()
                            }
                        }
                        DetalleScreen(
                            estado = estado,
                            imagenInicial = ruta.imagen,
                            idProducto = ruta.id,
                            onVolver = { navController.navigateUp() },
                            onReintentar = viewModel::cargar,
                            onAlternarFavorito = if (esAdmin) null else viewModel::alternarFavorito,
                            onAgregarAlCarrito = if (esAdmin) {
                                null
                            } else {
                                { item, cantidad -> carritoViewModel.agregar(item, cantidad) }
                            },
                            onEditar = if (esAdmin) {
                                { navController.navigate(Rutas.EditarProducto(ruta.id)) }
                            } else {
                                null
                            }
                        )
                    }
                    pantalla<Rutas.Favoritos> {
                        val viewModel: FavoritosViewModel = viewModel(
                            factory = FavoritosViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        FavoritosScreen(
                            estado = estado,
                            onReintentar = viewModel::recargar,
                            onProductoClick = { item ->
                                navController.navigate(Rutas.Detalle(item.id, item.imagenUrl))
                            },
                            onAlternarFavorito = viewModel::alternarFavorito,
                            onAgregarAlCarrito = { item -> carritoViewModel.agregar(item) },
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    pantalla<Rutas.Carrito> {
                        val estado by carritoViewModel.estado.collectAsStateWithLifecycle()
                        val procesando by carritoViewModel.procesando.collectAsStateWithLifecycle()
                        val pedidoConfirmado by carritoViewModel.pedidoConfirmado
                            .collectAsStateWithLifecycle()
                        CarritoScreen(
                            estado = estado,
                            procesando = procesando,
                            pedidoConfirmado = pedidoConfirmado,
                            onCambiarCantidad = carritoViewModel::cambiarCantidad,
                            onQuitar = carritoViewModel::quitar,
                            onVaciar = carritoViewModel::vaciar,
                            onConfirmar = {
                                carritoViewModel.confirmarPedido(sesionVigente?.correo.orEmpty())
                            },
                            onPedidoMostrado = carritoViewModel::pedidoMostrado,
                            onVerPedidos = { navController.navigate(Rutas.Pedidos) },
                            onIrCatalogo = { navController.irAPestana(DestinoPrincipal.CATALOGO, raizPestanas) },
                            onReintentar = carritoViewModel::recargar
                        )
                    }
                    pantalla<Rutas.Pedidos> {
                        val viewModel: PedidosViewModel = viewModel(
                            factory = PedidosViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        PedidosScreen(
                            estado = estado,
                            onReintentar = viewModel::recargar,
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    pantalla<Rutas.Admin> {
                        val administrador = sesionVigente?.takeIf { it.rol == Rol.ADMIN }
                        if (administrador == null) {
                            LaunchedEffect(Unit) { navController.navigateUp() }
                            return@pantalla
                        }
                        val viewModel: AdminViewModel = viewModel(
                            factory = AdminViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val trabajando by viewModel.trabajando.collectAsStateWithLifecycle()
                        val resultado by viewModel.resultado.collectAsStateWithLifecycle()
                        AdminScreen(
                            correo = administrador.correo,
                            estado = estado,
                            trabajando = trabajando,
                            resultado = resultado,
                            onSincronizar = viewModel::sincronizar,
                            onLimpiarCache = viewModel::limpiarCache,
                            onResultadoMostrado = viewModel::resultadoMostrado,
                            onGestionarProductos = { navController.navigate(Rutas.AdminProductos) },
                            onGestionarCategorias = { navController.navigate(Rutas.AdminCategorias) },
                            onExportar = viewModel::exportarInventario
                        )
                    }
                    pantalla<Rutas.AdminProductos> { entrada ->
                        if (sesionVigente?.rol != Rol.ADMIN) {
                            LaunchedEffect(Unit) { navController.navigateUp() }
                            return@pantalla
                        }
                        val viewModel: AdminProductosViewModel = viewModel(
                            factory = AdminProductosViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val consulta by viewModel.consulta.collectAsStateWithLifecycle()
                        val resultadoEdicion by entrada.savedStateHandle
                            .getStateFlow<String?>(ResultadoEdicion.CLAVE, null)
                            .collectAsStateWithLifecycle()
                        LaunchedEffect(resultadoEdicion) {
                            val codificado = resultadoEdicion ?: return@LaunchedEffect
                            ResultadoEdicion.decodificar(codificado)?.let(viewModel::procesarResultado)
                            entrada.savedStateHandle.set<String>(ResultadoEdicion.CLAVE, null)
                        }
                        AdminProductosScreen(
                            estado = estado,
                            consulta = consulta,
                            avisos = viewModel.avisos,
                            onConsultaChange = viewModel::cambiarConsulta,
                            onProductoClick = { item ->
                                navController.navigate(Rutas.EditarProducto(item.id))
                            },
                            onNuevoProducto = { navController.navigate(Rutas.EditarProducto()) },
                            onEliminar = viewModel::eliminar,
                            onDeshacer = viewModel::deshacer,
                            onReintentar = { viewModel.cambiarConsulta(consulta) },
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    pantalla<Rutas.EditarProducto> { entrada ->
                        if (sesionVigente?.rol != Rol.ADMIN) {
                            LaunchedEffect(Unit) { navController.navigateUp() }
                            return@pantalla
                        }
                        val ruta = entrada.toRoute<Rutas.EditarProducto>()
                        val viewModel: EditarProductoViewModel = viewModel(
                            factory = EditarProductoViewModel.factory(container, ruta.id)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val categorias by viewModel.categorias.collectAsStateWithLifecycle()
                        val resultado by viewModel.resultado.collectAsStateWithLifecycle()
                        LaunchedEffect(resultado) {
                            val actual = resultado ?: return@LaunchedEffect
                            navController.previousBackStackEntry?.savedStateHandle
                                ?.set(ResultadoEdicion.CLAVE, actual.codificar())
                            viewModel.resultadoProcesado()
                            navController.navigateUp()
                        }
                        EditarProductoScreen(
                            estado = estado,
                            categorias = categorias,
                            avisos = viewModel.avisos,
                            onCambiarCampo = viewModel::cambiarCampo,
                            onSeleccionarCategoria = viewModel::seleccionarCategoria,
                            onSeleccionarTipo = viewModel::seleccionarTipo,
                            onGuardar = viewModel::guardar,
                            onEliminar = viewModel::eliminar,
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    pantalla<Rutas.Alertas> { entrada ->
                        if (sesionVigente?.rol != Rol.ADMIN) {
                            LaunchedEffect(Unit) { navController.navigateUp() }
                            return@pantalla
                        }
                        val viewModel: AlertasViewModel = viewModel(
                            factory = AlertasViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val filtro by viewModel.filtro.collectAsStateWithLifecycle()
                        val resultadoEdicion by entrada.savedStateHandle
                            .getStateFlow<String?>(ResultadoEdicion.CLAVE, null)
                            .collectAsStateWithLifecycle()
                        LaunchedEffect(resultadoEdicion) {
                            val codificado = resultadoEdicion ?: return@LaunchedEffect
                            ResultadoEdicion.decodificar(codificado)?.let(viewModel::procesarResultado)
                            entrada.savedStateHandle.set<String>(ResultadoEdicion.CLAVE, null)
                        }
                        AlertasScreen(
                            estado = estado,
                            filtro = filtro,
                            avisos = viewModel.avisos,
                            onSeleccionarFiltro = viewModel::seleccionarFiltro,
                            onProductoClick = { item ->
                                navController.navigate(Rutas.EditarProducto(item.id))
                            },
                            onReponer = viewModel::reponer,
                            onDeshacer = viewModel::deshacer,
                            onReintentar = { viewModel.seleccionarFiltro(null) }
                        )
                    }
                    pantalla<Rutas.AdminCategorias> {
                        if (sesionVigente?.rol != Rol.ADMIN) {
                            LaunchedEffect(Unit) { navController.navigateUp() }
                            return@pantalla
                        }
                        val viewModel: AdminCategoriasViewModel = viewModel(
                            factory = AdminCategoriasViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val edicion by viewModel.edicion.collectAsStateWithLifecycle()
                        AdminCategoriasScreen(
                            estado = estado,
                            edicion = edicion,
                            avisos = viewModel.avisos,
                            onNueva = viewModel::abrirNueva,
                            onEditar = viewModel::abrirEdicion,
                            onCambiarNombre = viewModel::cambiarNombre,
                            onGuardar = viewModel::guardar,
                            onCerrarEdicion = viewModel::cerrarEdicion,
                            onEliminar = viewModel::eliminar,
                            onDeshacer = viewModel::deshacer,
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    pantalla<Rutas.Ajustes> {
                        val viewModel: AjustesViewModel = viewModel(
                            factory = AjustesViewModel.factory(container)
                        )
                        val modoTema by viewModel.modoTema.collectAsStateWithLifecycle()
                        AjustesScreen(
                            sesion = sesionVigente,
                            modoTema = modoTema ?: ModoTema.CLARO,
                            onCambiarModoTema = viewModel::cambiarModoTema,
                            onCerrarSesion = { mostrarDialogoSalir = true },
                            cantidadFavoritos = cantidadFavoritos,
                            onAbrirFavoritos = { navController.navigate(Rutas.Favoritos) },
                            onAbrirPedidos = { navController.navigate(Rutas.Pedidos) }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogoSalir) {
        DialogoCerrarSesion(
            onConfirmar = {
                mostrarDialogoSalir = false
                sesionViewModel.cerrarSesion()
                navController.navigate(Rutas.Login) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
            onCancelar = { mostrarDialogoSalir = false }
        )
    }
}

private inline fun <reified T : Rutas> NavGraphBuilder.pantalla(
    noinline contenido: @Composable (NavBackStackEntry) -> Unit
) {
    composable<T> { entrada ->
        CompositionLocalProvider(LocalAlcanceAnimacionVisible provides this) {
            contenido(entrada)
        }
    }
}

private fun NavHostController.irAPestana(destino: DestinoPrincipal, raiz: Rutas) {
    navigate(destino.ruta) {
        when (raiz) {
            is Rutas.Inicio -> popUpTo<Rutas.Inicio> { saveState = true }
            is Rutas.Admin -> popUpTo<Rutas.Admin> { saveState = true }
            else -> popUpTo<Rutas.Lista> { saveState = true }
        }
        launchSingleTop = true
        restoreState = true
    }
}
