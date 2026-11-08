package com.tecsup.productos.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.tecsup.productos.ui.detalle.DetalleScreen
import com.tecsup.productos.ui.detalle.DetalleViewModel
import com.tecsup.productos.ui.favoritos.FavoritosScreen
import com.tecsup.productos.ui.favoritos.FavoritosViewModel
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel

@Composable
fun AppNav(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Rutas.Lista,
        modifier = modifier
    ) {
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
                    IconButton(onClick = { navController.navigate(Rutas.Favoritos) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_favorito),
                            contentDescription = stringResource(R.string.favoritos_ver)
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
    }
}
