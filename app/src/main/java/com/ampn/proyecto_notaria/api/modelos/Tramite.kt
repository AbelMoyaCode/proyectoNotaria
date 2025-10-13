package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Trámite (sincronizado con la tabla tramites en PostgreSQL)
 */
data class Tramite(
    @SerializedName("codigo")
    val codigo: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("requisitos")
    val requisitos: String? = null,

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("activo")
    val activo: Boolean = true
)

