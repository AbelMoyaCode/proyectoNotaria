package com.ampn.proyecto_notaria.api.servicios

import com.ampn.proyecto_notaria.api.modelos.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de servicio para gestión de notificaciones
 */
interface NotificacionesService {

    /**
     * Obtener todas las notificaciones del usuario
     * GET /notificaciones
     */
    @GET("notificaciones")
    suspend fun obtenerNotificaciones(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Notificacion>>>

    /**
     * Obtener solo las notificaciones no leídas
     * GET /notificaciones?leido=false
     */
    @GET("notificaciones")
    suspend fun obtenerNotificacionesNoLeidas(
        @Header("Authorization") token: String,
        @Query("leido") leido: Boolean = false
    ): Response<ApiResponse<List<Notificacion>>>

    /**
     * Marcar una notificación como leída
     * PATCH /notificaciones/{id}/marcar-leida
     */
    @PATCH("notificaciones/{id}/marcar-leida")
    suspend fun marcarComoLeida(
        @Header("Authorization") token: String,
        @Path("id") notificacionId: String
    ): Response<ApiResponse<Notificacion>>

    /**
     * Marcar todas las notificaciones como leídas
     * PATCH /notificaciones/marcar-todas-leidas
     */
    @PATCH("notificaciones/marcar-todas-leidas")
    suspend fun marcarTodasComoLeidas(
        @Header("Authorization") token: String
    ): Response<ApiResponse<String>>
}

