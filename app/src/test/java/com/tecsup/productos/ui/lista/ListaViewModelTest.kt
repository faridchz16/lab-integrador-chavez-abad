package com.tecsup.productos.ui.lista

import com.tecsup.productos.ReglaDespachadorPrincipal
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ListaViewModelTest {

    @get:Rule
    val regla = ReglaDespachadorPrincipal()

    private val iphone = Item(id = 1, titulo = "iPhone 9", marca = "Apple", precio = 1899.0, imagenUrl = null, stock = 30)
    private val galaxy = Item(id = 2, titulo = "Galaxy S24", marca = "Samsung", precio = 2999.0, imagenUrl = null, stock = 0)

    private fun crear(repo: ItemRepositoryFalso, notificador: NotificadorFalso = NotificadorFalso()) =
        ListaViewModel(
            repo = repo,
            categoriaRepository = CategoriaRepositoryFalso(),
            preferencias = PreferenciasRepositoryFalso(),
            notificador = notificador,
            textos = ProveedorTextosFalso()
        )

    private fun productosDe(viewModel: ListaViewModel): List<Item> {
        val estado = viewModel.estado.value
        assertTrue("Se esperaba Exito y llegó $estado", estado is UiState.Exito)
        return (estado as UiState.Exito).datos
    }

    @Test
    fun alSincronizarMuestraLosProductosYNotifica() = runTest(regla.despachador) {
        val notificador = NotificadorFalso()
        val viewModel = crear(ItemRepositoryFalso(remotos = listOf(iphone, galaxy)), notificador)

        advanceUntilIdle()

        assertEquals(listOf(galaxy, iphone), productosDe(viewModel))
        assertEquals(1, notificador.mostrados.size)
        assertNull(viewModel.aviso.value)
    }

    @Test
    fun sinConexionNiDatosGuardadosMuestraError() = runTest(regla.despachador) {
        val viewModel = crear(ItemRepositoryFalso(error = IOException()))

        advanceUntilIdle()

        assertTrue(viewModel.estado.value is UiState.Error)
    }

    @Test
    fun sinConexionConDatosGuardadosMuestraLaListaYUnAviso() = runTest(regla.despachador) {
        val viewModel = crear(ItemRepositoryFalso(guardados = listOf(iphone), error = IOException()))

        advanceUntilIdle()

        assertEquals(listOf(iphone), productosDe(viewModel))
        assertNotNull(viewModel.aviso.value)
    }

    @Test
    fun laBusquedaFiltraSinImportarMayusculas() = runTest(regla.despachador) {
        val viewModel = crear(ItemRepositoryFalso(remotos = listOf(iphone, galaxy)))

        viewModel.cambiarConsulta("GALAXY")

        advanceUntilIdle()

        assertEquals(listOf(galaxy), productosDe(viewModel))
    }

    @Test
    fun limpiarTodoRestauraLaListaCompleta() = runTest(regla.despachador) {
        val viewModel = crear(ItemRepositoryFalso(remotos = listOf(iphone, galaxy)))

        viewModel.cambiarConsulta("galaxy")
        viewModel.limpiarTodo()

        advanceUntilIdle()

        assertEquals(2, productosDe(viewModel).size)
    }

    @Test
    fun marcarFavoritoSeGuardaEnElRepositorio() = runTest(regla.despachador) {
        val repo = ItemRepositoryFalso(remotos = listOf(iphone))
        val viewModel = crear(repo)

        viewModel.alternarFavorito(iphone.id)

        advanceUntilIdle()

        assertTrue(repo.actuales().single().esFavorito)
        assertTrue(productosDe(viewModel).single().esFavorito)
    }
}
