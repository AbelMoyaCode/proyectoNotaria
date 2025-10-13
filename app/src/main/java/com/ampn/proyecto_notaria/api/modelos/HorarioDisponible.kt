package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Horario Disponible
 */

data class HorarioDisponible(
    @SerializedName("id")
    val id: String,

    @SerializedName("fecha")
    val fecha: String, // Formato: YYYY-MM-DD

    @SerializedName("hora")
    val hora: String, // Formato: HH:mm

    @SerializedName("capacidad")
    val capacidad: Int,

    @SerializedName("disponible")
    val disponible: Boolean,

    @SerializedName("created_at")
    val creadoEn: String? = null
)

/**
 * Modelo para consultar horarios por fecha
 */
data class HorariosRequest(
    @SerializedName("fecha")
    val fecha: String // Formato: YYYY-MM-DD
)

