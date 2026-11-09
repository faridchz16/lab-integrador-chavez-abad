package com.tecsup.productos.notification

import com.tecsup.productos.R
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.inventario.AlertaStock
import com.tecsup.productos.domain.inventario.NivelAlerta
import com.tecsup.productos.domain.inventario.nivelesCriticos
import com.tecsup.productos.domain.inventario.nuevasAlertasCriticas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MonitorStock(
    private val repo: ItemRepository,
    private val notificador: Notificador,
    private val textos: ProveedorTextos,
    private val alcance: CoroutineScope
) {

    private var trabajo: Job? = null

    fun iniciar() {
        if (trabajo?.isActive == true) return
        trabajo = alcance.launch {
            var anteriores: Map<Int, NivelAlerta>? = null
            repo.items
                .catch { }
                .collect { items ->
                    val previos = anteriores
                    if (previos != null && items.isNotEmpty()) {
                        val nuevas = nuevasAlertasCriticas(previos, items)
                        if (nuevas.isNotEmpty()) notificar(nuevas)
                    }
                    anteriores = if (items.isEmpty()) null else items.nivelesCriticos()
                }
        }
    }

    private fun notificar(alertas: List<AlertaStock>) {
        val unica = alertas.singleOrNull()
        if (unica != null) {
            val producto = unica.producto
            if (unica.nivel == NivelAlerta.AGOTADO) {
                notificador.mostrarAlertaStock(
                    textos.texto(R.string.alertas_notificacion_agotado_titulo, producto.titulo),
                    textos.texto(R.string.alertas_notificacion_agotado_texto)
                )
            } else {
                notificador.mostrarAlertaStock(
                    textos.texto(R.string.alertas_notificacion_bajo_titulo, producto.titulo),
                    textos.texto(
                        R.string.alertas_notificacion_bajo_texto,
                        unica.stock,
                        producto.stockMinimo
                    )
                )
            }
            return
        }
        notificador.mostrarAlertaStock(
            textos.plural(R.plurals.alertas_notificacion_varios_titulo, alertas.size, alertas.size),
            alertas.take(MAXIMO_NOMBRES).joinToString { it.producto.titulo } +
                if (alertas.size > MAXIMO_NOMBRES) {
                    textos.texto(R.string.alertas_notificacion_y_mas, alertas.size - MAXIMO_NOMBRES)
                } else {
                    ""
                }
        )
    }

    private companion object {
        const val MAXIMO_NOMBRES = 3
    }
}
