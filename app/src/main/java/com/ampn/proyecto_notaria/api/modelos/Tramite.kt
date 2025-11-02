package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo de Trámite que representa un trámite notarial disponible
 * Compatible con la base de datos PostgreSQL
 */
data class Tramite(
    @SerializedName("id")
    val id: Int,

    @SerializedName("codigo")
    val codigo: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("requisitos")
    val requisitos: String,

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("duracion_estimada")
    val duracionEstimada: String? = null,

    @SerializedName("categoria")
    val categoria: String? = null,

    @SerializedName("fecha_creacion")
    val fechaCreacion: String? = null,

    @SerializedName("activo")
    val activo: Boolean = true
) {
    // Propiedad adicional para compatibilidad
    val duracion_estimada: String? get() = duracionEstimada
}

