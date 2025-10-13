package com.ampn.proyecto_notaria.api.repositorios

import com.ampn.proyecto_notaria.api.RetrofitClient
import com.ampn.proyecto_notaria.api.modelos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para manejar operaciones de notificaciones
 */
class NotificacionesRepositorio {

    private val service = RetrofitClient.notificacionesService

    /**
     * Obtener todas las notificaciones del usuario
     */
    suspend fun obtenerNotificaciones(token: String): Result<List<Notificacion>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerNotificaciones("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val notificaciones = response.body()?.data ?: emptyList()
                    Result.success(notificaciones)
                } else {
                    Result.failure(Exception("Error al obtener notificaciones"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener solo las notificaciones no leídas
     */
    suspend fun obtenerNotificacionesNoLeidas(token: String): Result<List<Notificacion>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerNotificacionesNoLeidas("Bearer $token", false)
                if (response.isSuccessful && response.body()?.success == true) {
                    val notificaciones = response.body()?.data ?: emptyList()
                    Result.success(notificaciones)
                } else {
                    Result.failure(Exception("Error al obtener notificaciones"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Marcar una notificación como leída
     */
    suspend fun marcarComoLeida(token: String, notificacionId: String): Result<Notificacion> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.marcarComoLeida("Bearer $token", notificacionId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val notificacion = response.body()?.data
                    if (notificacion != null) {
                        Result.success(notificacion)
                    } else {
                        Result.failure(Exception("Error al marcar como leída"))
                    }
                } else {
                    Result.failure(Exception("No se pudo actualizar la notificación"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Marcar todas las notificaciones como leídas
     */
    suspend fun marcarTodasComoLeidas(token: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.marcarTodasComoLeidas("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Error al actualizar notificaciones"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

