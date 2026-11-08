package com.tecsup.productos.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tecsup.productos.AppContainer
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.admin.AdminScreen
import com.tecsup.productos.ui.admin.AdminViewModel
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.DialogoCerrarSesion
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

    when (val estado = estadoSesion) {
        is UiState.Exito -> NavegacionPrincipal(
            container = container,
            sesion = estado.datos,
            sesionViewModel = sesionViewModel,
            modifier = modifier
        )
        else -> Surface(modifier = modifier.fillMaxSize()) {
            Cargando()
        }
    }
}

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

    NavHost(
        navController = navController,
        startDestination = destinoInicial,
        modifier = modifier
    ) {
        composable<Rutas.Login> {
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
        composable<Rutas.Lista> {
            val viewModel: ListaViewModel = viewModel(
                factory = ListaViewModel.factory(container)
            )
            val estado by viewModel.estado.collectAsStateWithLifecycle()
            val aviso by viewModel.aviso.collectAsStateWithLifecycle()
            ListaScreen(
                estado = estado,
                aviso = aviso,
                onReintentar = viewModel::refrescar,
                onProductoClick = { id -> navController.navigate(Rutas.Detalle(id)) },
                onAlternarFavorito = viewModel::alternarFavorito,
                acciones = {
                    if (sesionVigente?.rol == Rol.ADMIN) {
                        IconButton(onClick = { navController.navigate(Rutas.Admin) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_panel_admin),
                                contentDescription = stringResource(R.string.admin_abrir)
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate(Rutas.Favoritos) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_favorito),
                            contentDescription = stringResource(R.string.favoritos_ver)
                        )
                    }
                    IconButton(onClick = { mostrarDialogoSalir = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cerrar_sesion),
                            contentDescription = stringResource(R.string.sesion_cerrar)
                        )
                    }
                }
            )
        }
        composable<Rutas.Detalle> { entrada ->
            val ruta = entrada.toRoute<Rutas.Detalle>()
            val viewModel: DetalleViewModel = viewModel(
                factory = DetalleViewModel.factory(container, ruta.id)
            )
            val estado by viewModel.estado.collectAsStateWithLifecycle()
            DetalleScreen(
                estado = estado,
                onVolver = { navController.navigateUp() },
                onReintentar = viewModel::cargar,
                onAlternarFavorito = viewModel::alternarFavorito
            )
        }
        composable<Rutas.Favoritos> {
            val viewModel: FavoritosViewModel = viewModel(
                factory = FavoritosViewModel.factory(container)
            )
            val estado by viewModel.estado.collectAsStateWithLifecycle()
            FavoritosScreen(
                estado = estado,
                onVolver = { navController.navigateUp() },
                onReintentar = viewModel::recargar,
                onProductoClick = { id -> navController.navigate(Rutas.Detalle(id)) },
                onAlternarFavorito = viewModel::alternarFavorito
            )
        }
        composable<Rutas.Admin> {
            val administrador = sesionVigente?.takeIf { it.rol == Rol.ADMIN }
            if (administrador == null) {
                LaunchedEffect(Unit) { navController.navigateUp() }
                return@composable
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
                onVolver = { navController.navigateUp() },
                onSincronizar = viewModel::sincronizar,
                onLimpiarCache = viewModel::limpiarCache,
                onResultadoMostrado = viewModel::resultadoMostrado
            )
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
