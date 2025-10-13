package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName
import java.io.Serializable

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

    @SerializedName("duracion_estimada")
    val duracion_estimada: String? = null,

    @SerializedName("categoria")
    val categoria: String? = null,

    @SerializedName("activo")
    val activo: Boolean = true
) : Serializable
