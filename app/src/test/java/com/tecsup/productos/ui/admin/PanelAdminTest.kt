package com.tecsup.productos.ui.admin

import com.tecsup.productos.domain.carrito.Pedido
import com.tecsup.productos.domain.model.Item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PanelAdminTest {

    private fun momento(dia: Int, hora: Int): Long = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.NOVEMBER, dia, hora, 0)
    }.timeInMillis

    private fun pedido(id: Long, fecha: Long, total: Double) = Pedido(
        id = id,
        fecha = fecha,
        total = total,
        cantidadArticulos = 1,
        lineas = emptyList()
    )

    private fun producto(id: Int, stock: Int, precio: Double = 100.0) = Item(
        id = id,
        titulo = "Producto $id",
        marca = "Marca",
        precio = precio,
        imagenUrl = null,
        stock = stock
    )

    private val ahora = momento(dia = 8, hora = 18)

    @Test
    fun cuentaSoloLosPedidosYVentasDeHoy() {
        val panel = construirPanel(
            items = emptyList(),
            pedidos = listOf(
                pedido(1, momento(8, 9), 150.0),
                pedido(2, momento(8, 17), 50.0),
                pedido(3, momento(7, 20), 300.0)
            ),
            totalCategorias = 0,
            ahora = ahora
        )
        assertEquals(2, panel.pedidosHoy)
        assertEquals(200.0, panel.ventasHoy, 0.001)
    }

    @Test
    fun agrupaLasVentasDeLosUltimosSieteDias() {
        val panel = construirPanel(
            items = emptyList(),
            pedidos = listOf(
                pedido(1, momento(8, 9), 100.0),
                pedido(2, momento(2, 12), 40.0),
                pedido(3, momento(1, 12), 999.0)
            ),
            totalCategorias = 0,
            ahora = ahora
        )
        assertEquals(7, panel.ventasSemana.size)
        assertTrue(panel.ventasSemana.last().esHoy)
        assertEquals(40.0, panel.ventasSemana.first().total, 0.001)
        assertEquals(140.0, panel.totalSemana, 0.001)
    }

    @Test
    fun calculaDistribucionYValorDelInventario() {
        val panel = construirPanel(
            items = listOf(
                producto(1, stock = 0),
                producto(2, stock = 5),
                producto(3, stock = 12),
                producto(4, stock = 40),
                producto(5, stock = 100)
            ),
            pedidos = emptyList(),
            totalCategorias = 3,
            ahora = ahora
        )
        with(panel.distribucion) {
            assertEquals(1, correcto)
            assertEquals(1, cercaMinimo)
            assertEquals(2, critico)
            assertEquals(1, sobreMaximo)
        }
        assertEquals(1, panel.resumen.agotados)
        assertEquals(15_700.0, panel.resumen.valorInventario, 0.001)
        assertEquals(listOf(1, 2, 3, 4, 5), panel.menorStock.map { it.id })
    }
}
