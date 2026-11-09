package com.tecsup.productos.ui.admin

data class AvisoInventario(
    val texto: String,
    val conDeshacer: Boolean = false
)

enum class TipoResultadoEdicion { CREADO, ACTUALIZADO, ELIMINADO }

data class ResultadoEdicion(
    val tipo: TipoResultadoEdicion,
    val id: Int,
    val titulo: String
) {
    fun codificar(): String = listOf(tipo.name, id.toString(), titulo).joinToString(SEPARADOR)

    companion object {
        const val CLAVE = "resultado_edicion_producto"

        fun decodificar(texto: String?): ResultadoEdicion? {
            val partes = texto?.split(SEPARADOR, limit = PARTES) ?: return null
            if (partes.size != PARTES) return null
            val tipo = TipoResultadoEdicion.entries.firstOrNull { it.name == partes[0] } ?: return null
            val id = partes[1].toIntOrNull() ?: return null
            return ResultadoEdicion(tipo, id, partes[2])
        }

        private const val SEPARADOR = "|"
        private const val PARTES = 3
    }
}
