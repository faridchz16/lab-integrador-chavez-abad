package com.tecsup.productos.ui.lista

import com.tecsup.productos.data.categorias.CategoriaRepository
import com.tecsup.productos.data.preferencias.PreferenciasRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.ModoTema
import com.tecsup.productos.notification.Notificador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ItemRepositoryFalso(
    guardados: List<Item> = emptyList(),
    private val remotos: List<Item> = emptyList(),
    private val error: Exception? = null
) : ItemRepository {

    private val datos = MutableStateFlow(guardados)

    override val items: Flow<List<Item>> = datos

    override val favoritos: Flow<List<Item>> = datos.map { lista -> lista.filter { it.esFavorito } }

    override suspend fun refrescar(): Result<Int> {
        if (error != null) return Result.failure(error)
        datos.value = remotos
        return Result.success(remotos.size)
    }

    override suspend fun obtenerDetalle(id: Int): Result<Item> =
        datos.value.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException())

    override suspend fun alternarFavorito(id: Int) {
        datos.value = datos.value.map { if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it }
    }

    override suspend fun cantidadGuardada(): Int = datos.value.size

    override suspend fun cantidadFavoritos(): Int = datos.value.count { it.esFavorito }

    override suspend fun limpiarCache() {
        datos.value = emptyList()
    }

    fun actuales(): List<Item> = datos.value
}

class CategoriaRepositoryFalso : CategoriaRepository {
    override val categorias: Flow<List<Categoria>> = MutableStateFlow(emptyList())
}

class PreferenciasRepositoryFalso : PreferenciasRepository {
    private val historial = MutableStateFlow<List<String>>(emptyList())
    override val modoTema: Flow<ModoTema> = MutableStateFlow(ModoTema.SISTEMA)
    override val historialBusqueda: Flow<List<String>> = historial
    override suspend fun guardarModoTema(modo: ModoTema) = Unit
    override suspend fun agregarBusqueda(consulta: String) {
        historial.value = listOf(consulta) + historial.value
    }
    override suspend fun eliminarBusqueda(consulta: String) {
        historial.value = historial.value - consulta
    }
    override suspend fun limpiarHistorial() {
        historial.value = emptyList()
    }
}

class NotificadorFalso : Notificador {
    val mostrados = mutableListOf<String>()
    override fun mostrar(titulo: String, texto: String) {
        mostrados += texto
    }
    override fun mostrarAlertaStock(titulo: String, texto: String) {
        mostrados += texto
    }
}

class ProveedorTextosFalso : ProveedorTextos {
    override fun texto(id: Int, vararg argumentos: Any): String = "texto-$id"
    override fun plural(id: Int, cantidad: Int, vararg argumentos: Any): String = "plural-$cantidad"
}
