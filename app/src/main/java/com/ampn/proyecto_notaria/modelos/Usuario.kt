package com.ampn.proyecto_notaria.modelos

import java.util.Date

/**
 * Representa el modelo de datos para un usuario en la aplicación.
 * Sus propiedades coinciden con la tabla 'usuarios' de la base de datos.
 */
data class Usuario(
    val id: Int = 0, // Generalmente autogenerado por la base de datos
    val nro_documento: String,
    val nombre: String,
    val apellido_paterno: String,
    val apellido_materno: String,
    val fecha_nacimiento: String, // Usamos String por simplicidad, se podría usar Date
    val correo: String,
    val direccion: String,
    // La contraseña no se suele incluir en el modelo por seguridad,
    // pero la añadimos si es necesaria para la lógica de creación.
    val contrasena: String? = null 
)