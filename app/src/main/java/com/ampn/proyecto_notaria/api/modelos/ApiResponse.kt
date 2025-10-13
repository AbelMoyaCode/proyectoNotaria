package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Respuesta genérica de la API
 * Wrapper para todas las respuestas del backend
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("mensaje")
    val mensaje: String? = null
)

