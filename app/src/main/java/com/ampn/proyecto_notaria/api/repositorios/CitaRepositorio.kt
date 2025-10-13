package com.ampn.proyecto_notaria.api.repositorios

import com.ampn.proyecto_notaria.api.modelos.Cita
import com.ampn.proyecto_notaria.api.servicios.ServicioApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar operaciones de citas
 */
class CitaRepositorio {

    private val api = ServicioApi.crearServicio()

    /**
     * Obtiene todas las citas de un usuario específico
     */
    suspend fun obtenerCitasUsuario(usuarioId: String): Result<List<Cita>> {
        return withContext(Dispatchers.IO) {
            try {
                val respuesta = api.obtenerCitasUsuario(usuarioId)

                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val apiResponse = respuesta.body()!!

                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.error ?: "Error desconocido al obtener citas"))
                    }
                } else {
                    Result.failure(Exception("Error al obtener citas: ${respuesta.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Crea una nueva cita
     */
    suspend fun crearCita(
        tramiteUsuarioId: String,
        horarioId: String
    ): Result<Cita> {
        return withContext(Dispatchers.IO) {
            try {
                val body = mapOf(
                    "tramite_usuario_id" to tramiteUsuarioId,
                    "horario_id" to horarioId
                )

                val respuesta = api.crearCita(body)

                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val apiResponse = respuesta.body()!!

                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.error ?: "Error al crear cita"))
                    }
                } else {
                    Result.failure(Exception("Error al crear cita: ${respuesta.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Cancela una cita existente
     */
    suspend fun cancelarCita(
        citaId: String,
        motivo: String
    ): Result<Cita> {
        return withContext(Dispatchers.IO) {
            try {
                val body = mapOf("motivo_cancelacion" to motivo)
                val respuesta = api.cancelarCita(citaId, body)

                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val apiResponse = respuesta.body()!!

                    if (apiResponse.success && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.error ?: "Error al cancelar cita"))
                    }
                } else {
                    Result.failure(Exception("Error al cancelar cita: ${respuesta.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
