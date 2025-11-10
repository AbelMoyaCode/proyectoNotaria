package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Trámite de Usuario
 */

data class TramiteUsuario(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("usuario_id")
    val usuarioId: String,

    @SerializedName("tramite_codigo")
    val tramiteCodigo: String,

    @SerializedName("estado_general")
    val estadoGeneral: String,

    @SerializedName("observaciones")
    val observaciones: String? = null,

    @SerializedName("creado_en")
    val creadoEn: String? = null,

    @SerializedName("updated_at")
    val actualizadoEn: String? = null,

    // Datos anidados del trámite
    @SerializedName("tramite")
    val tramite: Tramite? = null,

    // Datos anidados de la cita
    @SerializedName("cita")
    val cita: Cita? = null
)

/**
 * Modelo para "Mis Trámites" con información completa
 */
data class MiTramite(
    @SerializedName("id")
    val id: String,

    @SerializedName("tramite_nombre")
    val tramiteNombre: String,

    @SerializedName("tramite_descripcion")
    val tramiteDescripcion: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("fecha")
    val fecha: String? = null,

    @SerializedName("hora")
    val hora: String? = null,

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("observaciones")
    val observaciones: String? = null,

    @SerializedName("creado_en")
    val creadoEn: String
)
