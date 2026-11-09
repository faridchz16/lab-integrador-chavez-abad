package com.tecsup.productos.ui.admin

import com.tecsup.productos.domain.carrito.Pedido
import com.tecsup.productos.domain.model.EstadoStock
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.domain.model.precioFinal
import com.tecsup.productos.domain.model.tieneStockBajo
import java.util.Calendar

data class ResumenAdmin(
    val totalGuardado: Int,
    val valorInventario: Double,
    val stockBajo: Int,
    val agotados: Int
)

data class DistribucionStock(
    val correcto: Int,
    val cercaMinimo: Int,
    val critico: Int,
    val sobreMaximo: Int
) {
    val total: Int get() = correcto + cercaMinimo + critico + sobreMaximo
}

data class VentaDia(
    val inicioDia: Long,
    val total: Double,
    val pedidos: Int,
    val esHoy: Boolean
)

data class PanelAdmin(
    val resumen: ResumenAdmin,
    val pedidosHoy: Int,
    val ventasHoy: Double,
    val distribucion: DistribucionStock,
    val ventasSemana: List<VentaDia>,
    val menorStock: List<Item>,
    val totalCategorias: Int
) {
    val totalSemana: Double get() = ventasSemana.sumOf { it.total }
}

fun construirPanel(
    items: List<Item>,
    pedidos: List<Pedido>,
    totalCategorias: Int,
    ahora: Long
): PanelAdmin {
    val semana = (DIAS_SEMANA - 1 downTo 0).map { diasAtras ->
        val inicio = inicioDelDia(ahora, diasAtras)
        val fin = inicioDelDia(ahora, diasAtras - 1)
        val delDia = pedidos.filter { it.fecha in inicio until fin }
        VentaDia(
            inicioDia = inicio,
            total = delDia.sumOf { it.total },
            pedidos = delDia.size,
            esHoy = diasAtras == 0
        )
    }
    val hoy = semana.last()
    val estados = items.map { it.estadoStock() }
    return PanelAdmin(
        resumen = ResumenAdmin(
            totalGuardado = items.size,
            valorInventario = items.sumOf { it.precioFinal() * (it.stock ?: 0).coerceAtLeast(0) },
            stockBajo = items.count { it.tieneStockBajo() },
            agotados = estados.count { it == EstadoStock.AGOTADO }
        ),
        pedidosHoy = hoy.pedidos,
        ventasHoy = hoy.total,
        distribucion = DistribucionStock(
            correcto = estados.count { it == EstadoStock.DISPONIBLE },
            cercaMinimo = estados.count { it == EstadoStock.CERCA_MINIMO },
            critico = estados.count {
                it == EstadoStock.BAJO_MINIMO || it == EstadoStock.AGOTADO
            },
            sobreMaximo = estados.count { it == EstadoStock.SOBRE_MAXIMO }
        ),
        ventasSemana = semana,
        menorStock = items
            .filter { it.stock != null }
            .sortedBy { (it.stock ?: 0).toFloat() / maxOf(it.stockMinimo, 1) }
            .take(PRODUCTOS_MENOR_STOCK),
        totalCategorias = totalCategorias
    )
}

private fun inicioDelDia(momento: Long, diasAtras: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = momento
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -diasAtras)
    }.timeInMillis

private const val DIAS_SEMANA = 7
private const val PRODUCTOS_MENOR_STOCK = 5
