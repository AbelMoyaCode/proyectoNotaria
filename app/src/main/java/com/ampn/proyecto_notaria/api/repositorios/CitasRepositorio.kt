package com.ampn.proyecto_notaria.api.repositorios

import com.ampn.proyecto_notaria.api.RetrofitClient
import com.ampn.proyecto_notaria.api.modelos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar las operaciones de citas
 */
class CitasRepositorio {

    private val service = RetrofitClient.citasService

    /**
     * Crear una nueva cita
     */
    suspend fun crearCita(
        usuarioId: Int,
        tramiteCodigo: String,
        fecha: String,
        hora: String
    ): Result<CitaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CrearCitaRequest(
                    usuarioId = usuarioId.toString(),
                    tramiteCodigo = tramiteCodigo,
                    fecha = fecha,
                    hora = hora
                )

                val response = service.crearCita(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val cita = response.body()?.data
                    if (cita != null) {
                        Result.success(cita)
                    } else {
                        Result.failure(Exception("No se recibió información de la cita"))
                    }
                } else {
                    // Extraer el mensaje de error del servidor
                    val errorBody = response.errorBody()?.string()
                    val mensaje = if (errorBody != null) {
                        try {
                            // Parsear el JSON de error para obtener el mensaje
                            val jsonError = org.json.JSONObject(errorBody)
                            jsonError.optString("mensaje", "Error al crear la cita")
                        } catch (e: Exception) {
                            response.body()?.mensaje ?: "Error al crear la cita"
                        }
                    } else {
                        response.body()?.mensaje ?: "Error al crear la cita"
                    }

                    android.util.Log.e("CitasRepositorio", "❌ Error del servidor: $mensaje")
                    Result.failure(Exception(mensaje))
                }
            } catch (e: Exception) {
                android.util.Log.e("CitasRepositorio", "❌ Excepción: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener todas las citas de un usuario
     */
    suspend fun obtenerCitasUsuario(usuarioId: Int): Result<List<CitaResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerMisCitas(usuarioId.toString())

                if (response.isSuccessful && response.body()?.success == true) {
                    val citas = response.body()?.data ?: emptyList()
                    Result.success(citas)
                } else {
                    Result.failure(Exception("Error al obtener citas"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * ¡NUEVO! Obtener el detalle de una sola cita
     */
    suspend fun obtenerDetalleCita(citaId: Int): Result<CitaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerDetalleCita(citaId.toString())

                if (response.isSuccessful && response.body()?.success == true) {
                    val cita = response.body()?.data
                    if (cita != null) {
                        Result.success(cita)
                    } else {
                        Result.failure(Exception("No se encontró el detalle de la cita"))
                    }
                } else {
                    Result.failure(Exception("Error al obtener el detalle de la cita"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Cancelar una cita
     */
    suspend fun cancelarCita(citaId: Int, motivo: String): Result<CitaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CancelarCitaRequest(motivo)
                val response = service.cancelarCita(citaId.toString(), request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val cita = response.body()?.data
                    if (cita != null) {
                        Result.success(cita)
                    } else {
                        Result.failure(Exception("Error al cancelar"))
                    }
                } else {
                    Result.failure(Exception("Error al cancelar la cita"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Eliminar físicamente una cita (solo citas canceladas o finalizadas)
     */
    suspend fun eliminarCita(citaId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.eliminarCita(citaId.toString())

                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(true)
                } else {
                    val mensaje = response.body()?.mensaje ?: "Error al eliminar la cita"
                    Result.failure(Exception(mensaje))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Reprogramar una cita
     */
    suspend fun reprogramarCita(citaId: Int, fecha: String, hora: String): Result<CitaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ReprogramarCitaRequest(fecha, hora)
                val response = service.reprogramarCita(citaId.toString(), request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val cita = response.body()?.data
                    if (cita != null) {
                        Result.success(cita)
                    } else {
                        Result.failure(Exception("Error al reprogramar"))
                    }
                } else {
                    Result.failure(Exception("Error al reprogramar la cita"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
