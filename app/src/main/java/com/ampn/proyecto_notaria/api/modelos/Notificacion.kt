package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Notificación
 */
data class Notificacion(
    @SerializedName("id")
    val id: String,

    @SerializedName("usuario_id")
    val usuarioId: String,

    @SerializedName("tramite_usuario_id")
    val tramiteUsuarioId: String? = null,

    @SerializedName("tipo")
    val tipo: String,

    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("mensaje")
    val mensaje: String,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("leido")
    val leido: Boolean = false
)

/**
 * Modelo para marcar notificación como leída
 */
data class MarcarLeidaRequest(
    @SerializedName("notificacion_id")
    val notificacionId: String
)

