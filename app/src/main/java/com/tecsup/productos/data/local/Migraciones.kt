package com.tecsup.productos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tecsup.productos.domain.model.TIPO_CAMBIO_DOLAR_A_SOL

val MIGRACION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE items ADD COLUMN categoriaId INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE items ADD COLUMN stockMinimo INTEGER NOT NULL DEFAULT 10")
        db.execSQL("ALTER TABLE items ADD COLUMN stockMaximo INTEGER NOT NULL DEFAULT 80")
        db.execSQL("ALTER TABLE items ADD COLUMN unidadesPorPedido INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE items ADD COLUMN editadoLocalmente INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS categorias " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_categorias_nombre ON categorias (nombre)"
        )
    }
}

val MIGRACION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS carrito (" +
                "productoId INTEGER NOT NULL, " +
                "cantidad INTEGER NOT NULL, " +
                "agregadoEn INTEGER NOT NULL, " +
                "PRIMARY KEY(productoId), " +
                "FOREIGN KEY(productoId) REFERENCES items(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS pedidos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "fecha INTEGER NOT NULL, " +
                "total REAL NOT NULL, " +
                "cantidadArticulos INTEGER NOT NULL, " +
                "correoUsuario TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS detalles_pedido (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "pedidoId INTEGER NOT NULL, " +
                "productoId INTEGER NOT NULL, " +
                "titulo TEXT NOT NULL, " +
                "imagenUrl TEXT, " +
                "precioUnitario REAL NOT NULL, " +
                "cantidad INTEGER NOT NULL, " +
                "unidadesDescontadas INTEGER NOT NULL, " +
                "FOREIGN KEY(pedidoId) REFERENCES pedidos(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_detalles_pedido_pedidoId ON detalles_pedido (pedidoId)"
        )
    }
}

val MIGRACION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE items ADD COLUMN eliminado INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRACION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "UPDATE categorias SET nombre = 'Xiaomi' WHERE nombre = 'Realme' COLLATE NOCASE " +
                "AND NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'Xiaomi' COLLATE NOCASE)"
        )
        db.execSQL(
            "UPDATE items SET categoriaId = " +
                "(SELECT id FROM categorias WHERE nombre = 'Xiaomi' COLLATE NOCASE LIMIT 1) " +
                "WHERE categoriaId IN (SELECT id FROM categorias WHERE nombre = 'Realme' COLLATE NOCASE)"
        )
        db.execSQL("DELETE FROM categorias WHERE nombre = 'Realme' COLLATE NOCASE")
        db.execSQL("UPDATE items SET marca = 'Xiaomi' WHERE marca = 'Realme' COLLATE NOCASE")
        db.execSQL(
            "UPDATE items SET titulo = REPLACE(titulo, 'Realme', 'Redmi') " +
                "WHERE titulo LIKE 'Realme%' AND editadoLocalmente = 0"
        )
    }
}

val MIGRACION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE items SET precio = ROUND(precio * $TIPO_CAMBIO_DOLAR_A_SOL, 2)")
        db.execSQL("UPDATE pedidos SET total = ROUND(total * $TIPO_CAMBIO_DOLAR_A_SOL, 2)")
        db.execSQL(
            "UPDATE detalles_pedido SET precioUnitario = ROUND(precioUnitario * $TIPO_CAMBIO_DOLAR_A_SOL, 2)"
        )
    }
}
