package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Usuario (sincronizado con la tabla usuarios en PostgreSQL)
 */
data class Usuario(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("tipo_doc")
    val tipoDocumento: String,

    @SerializedName("nro_doc")
    val numeroDocumento: String,

    @SerializedName("nombres")
    val nombres: String,

    @SerializedName("apellidos")
    val apellidos: String,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("direccion")
    val direccion: String? = null,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("estado")
    val estado: String = "ACTIVO",

    @SerializedName("password_hash")
    val passwordHash: String? = null,

    @SerializedName("created_at")
    val creadoEn: String? = null,

    @SerializedName("updated_at")
    val actualizadoEn: String? = null
)

/**
 * Modelo para registro de usuario
 */
data class RegistroUsuarioRequest(
    @SerializedName("tipo_doc")
    val tipoDocumento: String,

    @SerializedName("nro_doc")
    val numeroDocumento: String,

    @SerializedName("nombres")
    val nombres: String,

    @SerializedName("apellidos")
    val apellidos: String,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("direccion")
    val direccion: String? = null,

    @SerializedName("telefono")
    val telefono: String? = null
)

/**
 * Modelo para login de usuario
 */
data class LoginRequest(
    @SerializedName("correo")
    val correo: String,

    @SerializedName("password")
    val password: String
)

/**
 * Respuesta del login
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("usuario")
    val usuario: Usuario,

    @SerializedName("mensaje")
    val mensaje: String
)
