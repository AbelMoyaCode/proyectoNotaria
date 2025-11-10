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
 * Respuesta al crear/obtener una cita
 */
data class CitaResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("tramite_usuario_id")
    val tramiteUsuarioId: Int? = null,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("hora")
    val hora: String,

    @SerializedName("tramite_nombre")
    val tramiteNombre: String,

    @SerializedName("tramite_descripcion")
    val tramiteDescripcion: String? = null,

    // --- ¡CAMPOS AÑADIDOS! ---
    @SerializedName("tramite_requisitos")
    val tramiteRequisitos: String? = null,

    @SerializedName("observaciones")
    val observaciones: String? = null,
    // -------------------------

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("creada_en")
    val creadaEn: String? = null
)

/**
 * Request para reprogramar una cita
 */
data class ReprogramarCitaRequest(
    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("hora")
    val hora: String
)

/**
 * Request para cancelar una cita
 */
data class CancelarCitaRequest(
    @SerializedName("motivo")
    val motivo: String
)
