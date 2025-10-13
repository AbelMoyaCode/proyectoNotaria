package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Estados posibles de una cita
 */
enum class EstadoCita {
    AGENDADO,
    EN_PROCESO,
    FINALIZADO,
    CANCELADO
}

/**
 * Modelo de datos para Cita
 */
data class Cita(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("tramite_usuario_id")
    val tramiteUsuarioId: String,

    @SerializedName("horario_id")
    val horarioId: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("creada_en")
    val creadaEn: String? = null,

    @SerializedName("reprogramada_en")
    val reprogramadaEn: String? = null,

    @SerializedName("motivo_cancelacion")
    val motivoCancelacion: String? = null
)

/**
 * Modelo para crear una cita
 */
data class CrearCitaRequest(
    @SerializedName("usuario_id")
    val usuarioId: String,

    @SerializedName("tramite_codigo")
    val tramiteCodigo: String,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("hora")
    val hora: String
)

/**
 * Modelo para reprogramar una cita
 */
data class ReprogramarCitaRequest(
    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("hora")
    val hora: String
)

/**
 * Modelo para cancelar una cita
 */
data class CancelarCitaRequest(
    @SerializedName("motivo")
    val motivo: String
)

/**
 * Respuesta del servidor al crear/modificar una cita
 */
data class CitaResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("tramite_usuario_id")
    val tramiteUsuarioId: String,

    @SerializedName("horario_id")
    val horarioId: String,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("hora")
    val hora: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("tramite")
    val tramite: Tramite? = null,

    @SerializedName("creada_en")
    val creadaEn: String? = null,

    @SerializedName("reprogramada_en")
    val reprogramadaEn: String? = null,

    @SerializedName("motivo_cancelacion")
    val motivoCancelacion: String? = null
)
