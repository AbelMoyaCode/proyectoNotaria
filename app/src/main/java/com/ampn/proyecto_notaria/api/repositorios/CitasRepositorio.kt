package com.ampn.proyecto_notaria.api.repositorios

import com.ampn.proyecto_notaria.api.RetrofitClient
import com.ampn.proyecto_notaria.api.modelos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para manejar operaciones de citas
 */
class CitasRepositorio {

    private val service = RetrofitClient.citasService

    /**
     * Crear/Reservar una nueva cita
     */
    suspend fun crearCita(solicitud: CrearCitaRequest): Result<CitaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.crearCita(solicitud)
                if (response.isSuccessful && response.body()?.success == true) {
                    val cita = response.body()?.data
                    if (cita != null) {
                        Result.success(cita)
                    } else {
                        Result.failure(Exception("Error al crear la cita"))
                    }
                } else {
                    val mensaje = response.body()?.mensaje ?: response.body()?.message ?: response.body()?.error ?: "Error al reservar la cita"
                    Result.failure(Exception(mensaje))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener citas del usuario
     */
    suspend fun obtenerMisCitas(usuarioId: String): Result<List<CitaResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerMisCitas(usuarioId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(response.body()?.data ?: emptyList())
                } else {
                    Result.failure(Exception("Error al obtener citas"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Reprogramar una cita existente
     */
    suspend fun reprogramarCita(
        citaId: String,
        nuevaFecha: String,
        nuevaHora: String
    ): Result<CitaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ReprogramarCitaRequest(nuevaFecha, nuevaHora)
                val response = service.reprogramarCita(citaId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val cita = response.body()?.data
                    if (cita != null) {
                        Result.success(cita)
                    } else {
                        Result.failure(Exception("Error al reprogramar"))
                    }
                } else {
                    val mensaje = response.body()?.mensaje ?: response.body()?.message ?: response.body()?.error ?: "No se pudo reprogramar la cita"
                    Result.failure(Exception(mensaje))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Cancelar una cita
     */
    suspend fun cancelarCita(citaId: String, motivo: String): Result<CitaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CancelarCitaRequest(motivo)
                val response = service.cancelarCita(citaId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val cita = response.body()?.data
                    if (cita != null) {
                        Result.success(cita)
                    } else {
                        Result.failure(Exception("Error al cancelar"))
                    }
                } else {
                    val mensaje = response.body()?.mensaje ?: response.body()?.message ?: response.body()?.error ?: "No se pudo cancelar la cita"
                    Result.failure(Exception(mensaje))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
