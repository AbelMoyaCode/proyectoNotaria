package com.ampn.proyecto_notaria.api.utils

import android.content.Context
import android.content.SharedPreferences
import com.ampn.proyecto_notaria.api.modelos.Usuario
import com.google.gson.Gson

/**
 * Gestor de sesión para manejar el token de autenticación y datos del usuario
 * Utiliza SharedPreferences para persistir los datos
 */
class GestorSesion(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "TramiNotarPrefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USUARIO = "usuario"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * Guardar token de autenticación
     */
    fun guardarToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    /**
     * Obtener token de autenticación
     */
    fun obtenerToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Guardar información del usuario
     */
    fun guardarUsuario(usuario: Usuario) {
        val usuarioJson = gson.toJson(usuario)
        prefs.edit()
            .putString(KEY_USUARIO, usuarioJson)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    /**
     * Obtener información del usuario
     */
    fun obtenerUsuario(): Usuario? {
        val usuarioJson = prefs.getString(KEY_USUARIO, null)
        return if (usuarioJson != null) {
            gson.fromJson(usuarioJson, Usuario::class.java)
        } else {
            null
        }
    }

    /**
     * Obtener ID del usuario autenticado
     */
    fun obtenerUsuarioId(): String? {
        return obtenerUsuario()?.id
    }

    /**
     * Verificar si el usuario está autenticado
     */
    fun estaAutenticado(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && obtenerToken() != null
    }

    /**
     * Cerrar sesión y limpiar datos
     */
    fun cerrarSesion() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USUARIO)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    /**
     * Obtener header de autorización formateado para Retrofit
     */
    fun obtenerHeaderAutorizacion(): String? {
        val token = obtenerToken()
        return if (token != null) "Bearer $token" else null
    }
}
