package com.ampn.proyecto_notaria.api.repositorios

import com.ampn.proyecto_notaria.api.RetrofitClient
import com.ampn.proyecto_notaria.api.modelos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para manejar operaciones de trámites
 */
class TramitesRepositorio {

    private val service = RetrofitClient.tramitesService

    /**
     * Obtener listado completo de trámites
     */
    suspend fun obtenerTramites(): Result<List<Tramite>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerTramites()
                if (response.isSuccessful && response.body()?.success == true) {
                    val tramites = response.body()?.data ?: emptyList()
                    Result.success(tramites)
                } else {
                    Result.failure(Exception("Error al obtener trámites"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Buscar trámites por nombre o descripción
     */
    suspend fun buscarTramites(query: String): Result<List<Tramite>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.buscarTramites(query)
                if (response.isSuccessful && response.body()?.success == true) {
                    val tramites = response.body()?.data ?: emptyList()
                    Result.success(tramites)
                } else {
                    Result.failure(Exception("No se encontraron resultados"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener detalle de un trámite específico
     */
    suspend fun obtenerDetalleTramite(codigo: String): Result<Tramite> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerDetalleTramite(codigo)
                if (response.isSuccessful && response.body()?.success == true) {
                    val tramite = response.body()?.data
                    if (tramite != null) {
                        Result.success(tramite)
                    } else {
                        Result.failure(Exception("Trámite no encontrado"))
                    }
                } else {
                    Result.failure(Exception("Error al obtener detalle"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener horarios disponibles para una fecha específica
     */
    suspend fun obtenerHorariosDisponibles(fecha: String): Result<List<HorarioDisponible>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerHorariosDisponibles(fecha)
                if (response.isSuccessful && response.body()?.success == true) {
                    val horarios = response.body()?.data ?: emptyList()
                    Result.success(horarios)
                } else {
                    Result.failure(Exception("No hay horarios disponibles para esta fecha"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

