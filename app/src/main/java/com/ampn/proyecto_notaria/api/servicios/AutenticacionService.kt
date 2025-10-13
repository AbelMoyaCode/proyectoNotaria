package com.ampn.proyecto_notaria.api.servicios

import com.ampn.proyecto_notaria.api.modelos.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de servicio para autenticación de usuarios
 */
interface AutenticacionService {

    /**
     * Registro de nuevo usuario
     * POST /auth/register
     */
    @POST("auth/register")
    suspend fun registrarUsuario(
        @Body request: RegistroUsuarioRequest
    ): Response<ApiResponse<Usuario>>

    /**
     * Login de usuario
     * POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    /**
     * Obtener perfil del usuario autenticado
     * GET /auth/perfil
     */
    @GET("auth/perfil")
    suspend fun obtenerPerfil(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Usuario>>

    /**
     * Actualizar perfil del usuario
     * PUT /auth/perfil
     */
    @PUT("auth/perfil")
    suspend fun actualizarPerfil(
        @Header("Authorization") token: String,
        @Body usuario: Usuario
    ): Response<ApiResponse<Usuario>>

    /**
     * Cerrar sesión
     * POST /auth/logout
     */
    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ApiResponse<String>>
}

