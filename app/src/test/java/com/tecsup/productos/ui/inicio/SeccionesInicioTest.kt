package com.tecsup.productos.ui.inicio

import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.TipoProducto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeccionesInicioTest {

    private fun producto(
        id: Int,
        tipo: TipoProducto,
        descuento: Double,
        stock: Int = 20,
        rating: Double = 4.0
    ) = Item(
        id = id,
        titulo = "Producto $id",
        marca = "Apple",
        precio = 1000.0,
        imagenUrl = null,
        stock = stock,
        descuento = descuento,
        rating = rating,
        tipo = tipo
    )

    private val items = listOf(
        producto(1, TipoProducto.SMARTPHONE, descuento = 18.0),
        producto(2, TipoProducto.TABLET, descuento = 12.0),
        producto(3, TipoProducto.RELOJ, descuento = 9.0, rating = 4.9),
        producto(4, TipoProducto.ACCESORIO, descuento = 25.0, stock = 0),
        producto(5, TipoProducto.SMARTPHONE, descuento = 2.0)
    )

    @Test
    fun destacaElNuevoConMayorDescuentoQueTengaStock() {
        val secciones = construirInicio(items, emptyList())
        val primero = secciones.banners.first()
        assertTrue(primero is BannerInicio.Oferta)
        assertEquals(2, (primero as BannerInicio.Oferta).producto.id)
    }

    @Test
    fun lasOfertasExcluyenAlDestacadoAgotadosYDescuentosBajos() {
        val secciones = construirInicio(items, emptyList())
        assertEquals(listOf(1, 3), secciones.ofertas.map { it.id })
    }

    @Test
    fun creaUnBannerPorTipoDisponible() {
        val tipos = construirInicio(items, emptyList()).banners
            .filterIsInstance<BannerInicio.Tipo>()
            .map { it.tipo }
        assertEquals(listOf(TipoProducto.TABLET, TipoProducto.RELOJ, TipoProducto.SMARTPHONE), tipos)
    }

    @Test
    fun soloMuestraMarcasConProductos() {
        val marcas = listOf(
            Categoria(id = 1, nombre = "Apple", cantidadProductos = 5),
            Categoria(id = 2, nombre = "Xiaomi", cantidadProductos = 0)
        )
        assertEquals(listOf("Apple"), construirInicio(items, marcas).marcas.map { it.nombre })
    }
}
