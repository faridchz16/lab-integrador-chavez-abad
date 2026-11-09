package com.tecsup.productos.ui.lista

import com.tecsup.productos.R
import com.tecsup.productos.ReglaDespachadorPrincipal
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ListaViewModelErroresTest {

    @get:Rule
    val regla = ReglaDespachadorPrincipal()

    private val proveedorTextos = ProveedorTextosFalso()

    private fun crear(
        repo: ItemRepositoryFalso,
        notificador: NotificadorFalso = NotificadorFalso(),
    ) = ListaViewModel(
        repo = repo,
        categoriaRepository = CategoriaRepositoryFalso(),
        preferencias = PreferenciasRepositoryFalso(),
        notificador = notificador,
        textos = proveedorTextos,
    )

    @Test
    fun siRefrescarFallaConHttpException500YSinDatosGuardadosEstadoEsErrorServidor() =
        runTest(regla.despachador) {
            val error = HttpException(Response.error<Any>(500, "".toResponseBody(null)))
            val viewModel = crear(ItemRepositoryFalso(guardados = emptyList(), error = error))

            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertTrue("Se esperaba UiState.Error y fue $estado", estado is UiState.Error)
            assertEquals(
                proveedorTextos.texto(R.string.error_servidor),
                (estado as UiState.Error).mensaje,
            )
        }

    @Test
    fun siFallaConSerializationExceptionEstadoEsErrorInesperado() =
        runTest(regla.despachador) {
            val error = SerializationException("Error de deserialización JSON")
            val viewModel = crear(ItemRepositoryFalso(guardados = emptyList(), error = error))

            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertTrue("Se esperaba UiState.Error y fue $estado", estado is UiState.Error)
            assertEquals(
                proveedorTextos.texto(R.string.error_inesperado),
                (estado as UiState.Error).mensaje,
            )
        }

    @Test
    fun siFallaConIOExceptionNoSeEnviaNotificacion() =
        runTest(regla.despachador) {
            val notificador = NotificadorFalso()
            val viewModel = crear(ItemRepositoryFalso(error = IOException()), notificador)

            advanceUntilIdle()

            assertNotNull(viewModel)
            assertTrue(notificador.mostrados.isEmpty())
        }

    @Test
    fun siApiRespondeListaVaciaEstadoEsExitoConListaVaciaYAvisoEsNull() =
        runTest(regla.despachador) {
            val viewModel = crear(ItemRepositoryFalso(remotos = emptyList()))

            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertTrue("Se esperaba UiState.Exito y fue $estado", estado is UiState.Exito)
            assertTrue((estado as UiState.Exito).datos.isEmpty())
            assertNull(viewModel.aviso.value)
        }
}
