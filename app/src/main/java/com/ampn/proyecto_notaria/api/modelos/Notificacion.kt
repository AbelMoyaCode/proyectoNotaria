package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo para notificaciones del sistema
 */
data class Notificacion(
    @SerializedName("id")
    val id: Int,

    @SerializedName("tipo")
    val tipo: String, // "CONFIRMACION", "REPROGRAMACION", "RECORDATORIO", "CANCELACION"

    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("mensaje")
    val mensaje: String,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("leida")
    val leida: Boolean = false,

    @SerializedName("cita_id")
    val citaId: Int? = null
) {
    companion object {
        const val TIPO_CONFIRMACION = "CONFIRMACION"
        const val TIPO_REPROGRAMACION = "REPROGRAMACION"
        const val TIPO_RECORDATORIO = "RECORDATORIO"
        const val TIPO_CANCELACION = "CANCELACION"
    }

    fun getIcono(): String {
        return when (tipo) {
            TIPO_CONFIRMACION -> "✅"
            TIPO_REPROGRAMACION -> "🔄"
            TIPO_RECORDATORIO -> "⏰"
            TIPO_CANCELACION -> "❌"
            else -> "📢"
        }
    }
}

