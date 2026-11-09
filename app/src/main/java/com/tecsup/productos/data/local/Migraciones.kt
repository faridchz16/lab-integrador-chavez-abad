package com.tecsup.productos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
