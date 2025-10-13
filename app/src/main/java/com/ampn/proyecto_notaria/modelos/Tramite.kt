package com.ampn.proyecto_notaria.modelos

import java.io.Serializable

/**
 * Representa el modelo de datos para un trámite notarial.
 * Sus propiedades coinciden con la tabla 'tramites' de la base de datos.
 */
data class Tramite(
    val codigo: String,  // Cambiado de id: Int a codigo: String
    val nombre: String,
    val descripcion: String,
    val requisitos: String,
    val precio: Double,
    val duracion_estimada: String? = null, // Tiempo estimado del trámite (ej: "2-3 días")
    val categoria: String? = null // Categoría del trámite (ej: "Poderes", "Escrituras", etc.)
) : Serializable
