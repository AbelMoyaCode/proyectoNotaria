package com.ampn.proyecto_notaria.api.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo de Usuario que representa a un usuario registrado en el sistema
 * Compatible con la base de datos PostgreSQL
 */
data class Usuario(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nroDocumento")
    val nroDocumento: String,

    @SerializedName("nombres")
    val nombres: String,

    @SerializedName("apellidoPaterno")
    val apellidoPaterno: String,

    @SerializedName("apellidoMaterno")
    val apellidoMaterno: String,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("direccion")
    val direccion: String? = null,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("fechaRegistro")
    val fechaRegistro: String? = null,

    @SerializedName("estado")
    val estado: String = "ACTIVO"
) {
    fun nombreCompleto(): String {
        return "$nombres $apellidoPaterno $apellidoMaterno"
    }
}

/**
 * Request para registro de usuario
 */
data class RegistroUsuarioRequest(
    @SerializedName("nro_documento")
    val nroDocumento: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("apellido_paterno")
    val apellidoPaterno: String,

    @SerializedName("apellido_materno")
    val apellidoMaterno: String,

    @SerializedName("fecha_nacimiento")
    val fechaNacimiento: String,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("direccion")
    val direccion: String? = null,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("contrasena")
    val contrasena: String
)

/**
 * Request para login
 */
data class LoginRequest(
    @SerializedName("correo")
    val correo: String,

    @SerializedName("contrasena")
    val contrasena: String
)

/**
 * Response de login
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("usuario")
    val usuario: Usuario,

    @SerializedName("mensaje")
    val mensaje: String? = null
)
