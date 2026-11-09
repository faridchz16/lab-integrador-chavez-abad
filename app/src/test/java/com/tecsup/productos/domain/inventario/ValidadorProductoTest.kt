package com.tecsup.productos.domain.inventario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorProductoTest {

    private val valido = FormularioProducto(
        titulo = "Galaxy A55",
        marca = "Samsung",
        precio = "1299.90",
        descuento = "10",
        stock = "25",
        stockMinimo = "10",
        stockMaximo = "80",
        unidadesPorPedido = "1"
    )

    private fun errorDe(formulario: FormularioProducto, campo: CampoProducto): ErrorCampo? =
        ValidadorProducto.validar(formulario)[campo]

    @Test
    fun formularioCompletoNoTieneErrores() {
        assertTrue(ValidadorProducto.validar(valido).isEmpty())
    }

    @Test
    fun nombreVacioEsObligatorio() {
        assertEquals(ErrorCampo.OBLIGATORIO, errorDe(valido.copy(titulo = "   "), CampoProducto.TITULO))
    }

    @Test
    fun nombreMuyCortoEsInvalido() {
        assertEquals(ErrorCampo.TITULO_CORTO, errorDe(valido.copy(titulo = "AB"), CampoProducto.TITULO))
    }

    @Test
    fun precioAceptaComaDecimal() {
        assertNull(errorDe(valido.copy(precio = "1299,90"), CampoProducto.PRECIO))
    }

    @Test
    fun precioCeroEstaFueraDeRango() {
        assertEquals(ErrorCampo.PRECIO_FUERA_RANGO, errorDe(valido.copy(precio = "0"), CampoProducto.PRECIO))
    }

    @Test
    fun precioConLetrasNoEsNumero() {
        assertEquals(ErrorCampo.NUMERO_INVALIDO, errorDe(valido.copy(precio = "abc"), CampoProducto.PRECIO))
    }

    @Test
    fun descuentoMayorAlPermitidoEsInvalido() {
        assertEquals(
            ErrorCampo.DESCUENTO_FUERA_RANGO,
            errorDe(valido.copy(descuento = "95"), CampoProducto.DESCUENTO)
        )
    }

    @Test
    fun stockNegativoEsInvalido() {
        assertEquals(ErrorCampo.NEGATIVO, errorDe(valido.copy(stock = "-3"), CampoProducto.STOCK))
    }

    @Test
    fun stockConDecimalesNoEsEntero() {
        assertEquals(ErrorCampo.ENTERO_INVALIDO, errorDe(valido.copy(stock = "2.5"), CampoProducto.STOCK))
    }

    @Test
    fun maximoDebeSerMayorQueMinimo() {
        assertEquals(
            ErrorCampo.MAXIMO_MENOR_MINIMO,
            errorDe(valido.copy(stockMinimo = "50", stockMaximo = "40"), CampoProducto.STOCK_MAXIMO)
        )
    }

    @Test
    fun unidadesPorPedidoDebenSerAlMenosUna() {
        assertEquals(
            ErrorCampo.UNIDADES_INVALIDAS,
            errorDe(valido.copy(unidadesPorPedido = "0"), CampoProducto.UNIDADES)
        )
    }

    @Test
    fun imagenSinProtocoloEsInvalida() {
        assertEquals(
            ErrorCampo.URL_INVALIDA,
            errorDe(valido.copy(imagenUrl = "www.imagen.com/a.png"), CampoProducto.IMAGEN)
        )
    }

    @Test
    fun convertirFormularioMarcaElProductoComoEditado() {
        val producto = valido.copy(precio = "1299,90").aProducto(base = null, id = -1)
        assertEquals(-1, producto.id)
        assertEquals(1299.90, producto.precio, 0.001)
        assertEquals(25, producto.stock)
        assertTrue(producto.editadoLocalmente)
    }
}
