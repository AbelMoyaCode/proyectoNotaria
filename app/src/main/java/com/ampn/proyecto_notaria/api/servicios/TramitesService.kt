package com.ampn.proyecto_notaria.api.servicios

import com.ampn.proyecto_notaria.api.modelos.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de servicio para gestión de trámites
 */
interface TramitesService {

    /**
     * Obtener listado completo de trámites disponibles
     * GET /tramites
     */
    @GET("tramites")
    suspend fun obtenerTramites(): Response<ApiResponse<List<Tramite>>>

    /**
     * Buscar trámites por nombre o descripción
     * GET /tramites/buscar?q={query}
     */
    @GET("tramites/buscar")
    suspend fun buscarTramites(
        @Query("q") query: String
    ): Response<ApiResponse<List<Tramite>>>

    /**
     * Obtener detalle de un trámite específico
     * GET /tramites/{codigo}
     */
    @GET("tramites/{codigo}")
    suspend fun obtenerDetalleTramite(
        @Path("codigo") codigo: String
    ): Response<ApiResponse<Tramite>>

    /**
     * Obtener horarios disponibles para una fecha
     * GET /horarios?fecha={fecha}
     */
    @GET("horarios")
    suspend fun obtenerHorariosDisponibles(
        @Query("fecha") fecha: String
    ): Response<ApiResponse<List<HorarioDisponible>>>
}

