package com.tecsup.productos.data.mapper

import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.remote.dto.ItemDto
import com.tecsup.productos.domain.model.dolaresASoles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    @Test
    fun fusionarConConservaEsFavoritoAlSincronizar() {
        val entidadOriginal = ItemEntity(
            id = 1,
            titulo = "Producto Original",
            marca = "Marca A",
            precio = 100.0,
            imagenUrl = "http://imagen.png",
            rating = 4.5,
            stock = 10,
            descuento = 5.0,
            descripcion = "Descripcion original",
            esFavorito = true,
            categoriaId = 2L,
            editadoLocalmente = false,
        )
        val dto = ItemDto(
            id = 1,
            title = "Producto Sincronizado",
            price = 50.0,
            description = "Nueva descripcion",
        )

        val resultado = entidadOriginal.fusionarCon(dto)

        assertTrue(resultado.esFavorito)
    }

    @Test
    fun fusionarConActualizaTituloPrecioYDescripcionSinPerderCategoriaId() {
        val entidadOriginal = ItemEntity(
            id = 1,
            titulo = "Titulo Viejo",
            marca = "Marca Vieja",
            precio = 20.0,
            imagenUrl = "http://imagen.png",
            rating = 3.0,
            stock = 5,
            descuento = 0.0,
            descripcion = "Descripcion vieja",
            esFavorito = false,
            categoriaId = 10L,
            editadoLocalmente = false,
        )
        val dto = ItemDto(
            id = 1,
            title = "Nuevo Titulo API",
            price = 100.0,
            description = "Nueva Descripcion API",
        )

        val resultado = entidadOriginal.fusionarCon(dto)

        assertEquals("Nuevo Titulo API", resultado.titulo)
        assertEquals(100.0.dolaresASoles(), resultado.precio, 0.0001)
        assertEquals("Nueva Descripcion API", resultado.descripcion)
        assertEquals(10L, resultado.categoriaId)
    }

    @Test
    fun fusionarConNoCambiaEntidadSiEditadoLocalmenteEsTrue() {
        val entidadEditada = ItemEntity(
            id = 1,
            titulo = "Titulo Local",
            marca = "Marca Local",
            precio = 150.0,
            imagenUrl = "http://local.png",
            rating = 5.0,
            stock = 20,
            descuento = 10.0,
            descripcion = "Descripcion editada localmente",
            esFavorito = true,
            categoriaId = 3L,
            editadoLocalmente = true,
        )
        val dto = ItemDto(
            id = 1,
            title = "Titulo API",
            price = 50.0,
            description = "Descripcion API",
        )

        val resultado = entidadEditada.fusionarCon(dto)

        assertEquals(entidadEditada, resultado)
    }

    @Test
    fun aEntityConBrandNullDejaMarcaVaciaYEsFavoritoEnFalse() {
        val dto = ItemDto(
            id = 1,
            title = "Producto Sin Marca",
            price = 10.0,
            brand = null,
        )

        val entidad = dto.aEntity(categoriaId = 1L)

        assertEquals("", entidad.marca)
        assertFalse(entidad.esFavorito)
    }

    @Test
    fun aModeloMantieneEsFavorito() {
        val entidadFavorito = ItemEntity(
            id = 1,
            titulo = "Producto Favorito",
            marca = "Marca",
            precio = 100.0,
            imagenUrl = null,
            rating = 4.0,
            stock = 10,
            descuento = 0.0,
            esFavorito = true,
        )
        val entidadNoFavorito = ItemEntity(
            id = 2,
            titulo = "Producto No Favorito",
            marca = "Marca",
            precio = 50.0,
            imagenUrl = null,
            rating = 4.0,
            stock = 10,
            descuento = 0.0,
            esFavorito = false,
        )

        val modeloFavorito = entidadFavorito.aModelo()
        val modeloNoFavorito = entidadNoFavorito.aModelo()

        assertTrue(modeloFavorito.esFavorito)
        assertFalse(modeloNoFavorito.esFavorito)
    }
}
