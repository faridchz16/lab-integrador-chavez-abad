package com.tecsup.productos

import android.content.Context
import androidx.room.Room
import com.tecsup.productos.data.auth.AuthRepository
import com.tecsup.productos.data.auth.FirebaseAuthRepository
import com.tecsup.productos.data.carrito.CarritoRepository
import com.tecsup.productos.data.carrito.RoomCarritoRepository
import com.tecsup.productos.data.categorias.CategoriaRepository
import com.tecsup.productos.data.categorias.RoomCategoriaRepository
import com.tecsup.productos.data.exportacion.ExportadorArchivos
import com.tecsup.productos.data.exportacion.ExportadorArchivosAndroid
import com.tecsup.productos.data.inventario.InventarioRepository
import com.tecsup.productos.data.inventario.RoomInventarioRepository
import com.tecsup.productos.data.local.AppDatabase
import com.tecsup.productos.data.local.MIGRACION_2_3
import com.tecsup.productos.data.local.MIGRACION_3_4
import com.tecsup.productos.data.local.MIGRACION_4_5
import com.tecsup.productos.data.local.MIGRACION_5_6
import com.tecsup.productos.data.local.MIGRACION_6_7
import com.tecsup.productos.data.pedidos.PedidoRepository
import com.tecsup.productos.data.pedidos.RoomPedidoRepository
import com.tecsup.productos.data.preferencias.DataStorePreferenciasRepository
import com.tecsup.productos.data.preferencias.PreferenciasRepository
import com.tecsup.productos.data.recursos.ProveedorTextos
import com.tecsup.productos.data.recursos.ProveedorTextosAndroid
import com.tecsup.productos.data.remote.ApiService
import com.tecsup.productos.data.remote.RetrofitClient
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.data.repository.ItemRepositoryImpl
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.notification.MonitorStock
import com.tecsup.productos.notification.NotificadorAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(private val context: Context) {

    private val api: ApiService = RetrofitClient.api

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "productos.db")
            .addMigrations(MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5, MIGRACION_5_6, MIGRACION_6_7)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val itemRepository: ItemRepository by lazy {
        ItemRepositoryImpl(
            api = api,
            dao = database.itemDao(),
            categoriaDao = database.categoriaDao(),
            nombreSinCategoria = context.getString(R.string.categoria_sin_nombre)
        )
    }

    val categoriaRepository: CategoriaRepository by lazy {
        RoomCategoriaRepository(database.categoriaDao())
    }

    val carritoRepository: CarritoRepository by lazy {
        RoomCarritoRepository(database.carritoDao(), database.itemDao())
    }

    val pedidoRepository: PedidoRepository by lazy {
        RoomPedidoRepository(database)
    }

    val inventarioRepository: InventarioRepository by lazy {
        RoomInventarioRepository(database)
    }

    val notificador: Notificador by lazy {
        NotificadorAndroid(context)
    }

    val preferenciasRepository: PreferenciasRepository by lazy {
        DataStorePreferenciasRepository(context)
    }

    val textos: ProveedorTextos by lazy {
        ProveedorTextosAndroid(context)
    }

    val exportador: ExportadorArchivos by lazy {
        ExportadorArchivosAndroid(context)
    }

    private val alcanceAplicacion = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val monitorStock: MonitorStock by lazy {
        MonitorStock(itemRepository, notificador, textos, alcanceAplicacion)
    }
}