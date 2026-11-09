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
import com.tecsup.productos.domain.model.ModoTema
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.admin.AdminScreen
import com.tecsup.productos.ui.admin.AdminViewModel
import com.tecsup.productos.ui.ajustes.AjustesScreen
import com.tecsup.productos.ui.ajustes.AjustesViewModel
import com.tecsup.productos.ui.components.DialogoCerrarSesion
import com.tecsup.productos.ui.components.LocalAlcanceAnimacionVisible
import com.tecsup.productos.ui.components.LocalAlcanceTransicionCompartida
import com.tecsup.productos.ui.components.LogoStockify
import com.tecsup.productos.ui.components.SolicitarPermisoNotificaciones
import com.tecsup.productos.ui.detalle.DetalleScreen
import com.tecsup.productos.ui.detalle.DetalleViewModel
import com.tecsup.productos.ui.favoritos.FavoritosScreen
import com.tecsup.productos.ui.favoritos.FavoritosViewModel
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel
import com.tecsup.productos.ui.login.LoginScreen
import com.tecsup.productos.ui.login.LoginViewModel

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
        if (sesion != null) Rutas.Lista else Rutas.Login
    }
    val sesionVigente by rememberUpdatedState(sesion)
    var mostrarDialogoSalir by rememberSaveable { mutableStateOf(false) }

    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val destinosVisibles = remember(sesion?.rol) {
        DestinoPrincipal.entries.filter { !it.soloAdmin || sesion?.rol == Rol.ADMIN }
    }
    val mostrarBarra = sesion != null && destinosVisibles.any { destino ->
        destinoActual?.hierarchy?.any { it.hasRoute(destino.ruta::class) } == true
    }

    val insigniasViewModel: InsigniasViewModel = viewModel(
        factory = InsigniasViewModel.factory(container)
    )
    val cantidadFavoritos by insigniasViewModel.favoritos.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = mostrarBarra,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                BarraNavegacion(
                    destinos = destinosVisibles,
                    destinoActual = destinoActual,
                    insignias = mapOf(DestinoPrincipal.FAVORITOS to cantidadFavoritos),
                    onSeleccionar = { destino -> navController.irAPestana(destino) }
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
                                navController.navigate(Rutas.Lista) {
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
                    pantalla<Rutas.Lista> {
                        val viewModel: ListaViewModel = viewModel(
                            factory = ListaViewModel.factory(container)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        val aviso by viewModel.aviso.collectAsStateWithLifecycle()
                        ListaScreen(
                            estado = estado,
                            aviso = aviso,
                            onReintentar = viewModel::refrescar,
                            onProductoClick = { item ->
                                navController.navigate(Rutas.Detalle(item.id, item.imagenUrl))
                            },
                            onAlternarFavorito = viewModel::alternarFavorito
                        )
                    }
                    pantalla<Rutas.Detalle> { entrada ->
                        val ruta = entrada.toRoute<Rutas.Detalle>()
                        val viewModel: DetalleViewModel = viewModel(
                            factory = DetalleViewModel.factory(container, ruta.id)
                        )
                        val estado by viewModel.estado.collectAsStateWithLifecycle()
                        DetalleScreen(
                            estado = estado,
                            imagenInicial = ruta.imagen,
                            idProducto = ruta.id,
                            onVolver = { navController.navigateUp() },
                            onReintentar = viewModel::cargar,
                            onAlternarFavorito = viewModel::alternarFavorito
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
                            onAlternarFavorito = viewModel::alternarFavorito
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
                            onResultadoMostrado = viewModel::resultadoMostrado
                        )
                    }
                    pantalla<Rutas.Ajustes> {
                        val viewModel: AjustesViewModel = viewModel(
                            factory = AjustesViewModel.factory(container)
                        )
                        val modoTema by viewModel.modoTema.collectAsStateWithLifecycle()
                        AjustesScreen(
                            sesion = sesionVigente,
                            modoTema = modoTema ?: ModoTema.SISTEMA,
                            onCambiarModoTema = viewModel::cambiarModoTema,
                            onCerrarSesion = { mostrarDialogoSalir = true }
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

private fun NavHostController.irAPestana(destino: DestinoPrincipal) {
    navigate(destino.ruta) {
        popUpTo<Rutas.Lista> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
