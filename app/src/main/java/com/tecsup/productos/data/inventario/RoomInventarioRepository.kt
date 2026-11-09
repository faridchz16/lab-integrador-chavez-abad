package com.tecsup.productos.data.inventario

import androidx.room.withTransaction
import com.tecsup.productos.data.local.AppDatabase
import com.tecsup.productos.data.local.CategoriaEntity
import com.tecsup.productos.data.local.ItemEntity
import com.tecsup.productos.data.mapper.aModelo
import com.tecsup.productos.domain.inventario.CategoriaEliminada
import com.tecsup.productos.domain.inventario.ErrorInventario
import com.tecsup.productos.domain.model.Item

class RoomInventarioRepository(
    private val database: AppDatabase
) : InventarioRepository {

    private val itemDao = database.itemDao()
    private val categoriaDao = database.categoriaDao()
    private val carritoDao = database.carritoDao()

    override suspend fun obtenerProducto(id: Int): Item? {
        val entidad = itemDao.porId(id)?.takeIf { !it.eliminado } ?: return null
        return entidad.aModelo(entidad.categoriaId?.let { categoriaDao.porId(it)?.nombre })
    }

    override suspend fun guardarProducto(producto: Item, nuevo: Boolean): Int =
        database.withTransaction {
            val previo = if (nuevo) null else itemDao.porId(producto.id)
            if (!nuevo && previo == null) throw ErrorInventario.NoEncontrado()
            val id = if (nuevo) siguienteIdLocal() else producto.id
            val entidad = ItemEntity(
                id = id,
                titulo = producto.titulo,
                marca = producto.marca,
                precio = producto.precio,
                imagenUrl = producto.imagenUrl,
                rating = previo?.rating ?: producto.rating ?: 0.0,
                stock = producto.stock ?: 0,
                descuento = producto.descuento ?: 0.0,
                descripcion = producto.descripcion.orEmpty(),
                esFavorito = previo?.esFavorito ?: false,
                categoriaId = producto.categoriaId?.takeIf { categoriaDao.porId(it) != null },
                stockMinimo = producto.stockMinimo,
                stockMaximo = producto.stockMaximo,
                unidadesPorPedido = producto.unidadesPorPedido,
                editadoLocalmente = true,
                eliminado = false
            )
            itemDao.guardar(entidad)
            id
        }

    override suspend fun eliminarProducto(id: Int) {
        database.withTransaction {
            itemDao.porId(id) ?: throw ErrorInventario.NoEncontrado()
            itemDao.marcarEliminado(id, eliminado = true)
            carritoDao.quitar(id)
        }
    }

    override suspend fun restaurarProducto(id: Int) {
        itemDao.marcarEliminado(id, eliminado = false)
    }

    override suspend fun crearCategoria(nombre: String): Long {
        val limpio = nombre.trim()
        if (categoriaDao.porNombre(limpio) != null) throw ErrorInventario.NombreDuplicado(limpio)
        val id = categoriaDao.insertar(CategoriaEntity(nombre = limpio))
        if (id <= 0) throw ErrorInventario.NombreDuplicado(limpio)
        return id
    }

    override suspend fun renombrarCategoria(id: Long, nombre: String) {
        val limpio = nombre.trim()
        database.withTransaction {
            categoriaDao.porId(id) ?: throw ErrorInventario.NoEncontrado()
            val existente = categoriaDao.porNombre(limpio)
            if (existente != null && existente.id != id) {
                throw ErrorInventario.NombreDuplicado(limpio)
            }
            categoriaDao.renombrar(id, limpio)
        }
    }

    override suspend fun eliminarCategoria(id: Long): CategoriaEliminada =
        database.withTransaction {
            val categoria = categoriaDao.porId(id) ?: throw ErrorInventario.NoEncontrado()
            val productos = itemDao.idsPorCategoria(id)
            itemDao.quitarCategoria(id)
            categoriaDao.eliminar(id)
            CategoriaEliminada(id = categoria.id, nombre = categoria.nombre, productos = productos)
        }

    override suspend fun restaurarCategoria(categoria: CategoriaEliminada) {
        database.withTransaction {
            val insertado = categoriaDao.insertar(
                CategoriaEntity(id = categoria.id, nombre = categoria.nombre)
            )
            val destino = if (insertado > 0) {
                categoria.id
            } else {
                categoriaDao.porNombre(categoria.nombre)?.id
            }
            if (destino != null && categoria.productos.isNotEmpty()) {
                itemDao.asignarCategoria(categoria.productos, destino)
            }
        }
    }

    private suspend fun siguienteIdLocal(): Int = minOf(itemDao.menorId() ?: 0, 0) - 1
}
