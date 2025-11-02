package com.ampn.proyecto_notaria.api.servicios

import com.ampn.proyecto_notaria.api.modelos.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de servicio para gestión de citas
 */
interface CitasService {

    /**
     * Crear/Reservar una nueva cita
     * POST /citas
     */
    @POST("citas")
    suspend fun crearCita(
        @Body request: CrearCitaRequest
    ): Response<ApiResponse<CitaResponse>>

    /**
     * Obtener citas de un usuario
     * GET /citas/usuario/{usuarioId}
     */
    @GET("citas/usuario/{usuarioId}")
    suspend fun obtenerMisCitas(
        @Path("usuarioId") usuarioId: String
    ): Response<ApiResponse<List<CitaResponse>>>

    /**
     * Reprogramar una cita existente
     * PATCH /citas/{id}/reprogramar
     */
    @PATCH("citas/{id}/reprogramar")
    suspend fun reprogramarCita(
        @Path("id") citaId: String,
        @Body request: ReprogramarCitaRequest
    ): Response<ApiResponse<CitaResponse>>

    /**
     * Cancelar una cita
     * PATCH /citas/{id}/cancelar
     */
    @PATCH("citas/{id}/cancelar")
    suspend fun cancelarCita(
        @Path("id") citaId: String,
        @Body request: CancelarCitaRequest
    ): Response<ApiResponse<CitaResponse>>

    /**
     * Eliminar físicamente una cita
     * DELETE /citas/{id}
     */
    @DELETE("citas/{id}")
    suspend fun eliminarCita(
        @Path("id") citaId: String
    ): Response<ApiResponse<Unit>>

    /**
     * Obtener "Mis Trámites" del usuario autenticado
     * GET /mis-tramites
     */
    @GET("mis-tramites")
    suspend fun obtenerMisTramites(): Response<ApiResponse<List<MiTramite>>>

    /**
     * Obtener "Mis Trámites" filtrados por estado
     * GET /mis-tramites?estado={estado}
     */
    @GET("mis-tramites")
    suspend fun obtenerMisTramitesPorEstado(
        @Query("estado") estado: String
    ): Response<ApiResponse<List<MiTramite>>>

    /**
     * Obtener detalle de un trámite específico del usuario
     * GET /mis-tramites/{id}
     */
    @GET("mis-tramites/{id}")
    suspend fun obtenerDetalleMiTramite(
        @Path("id") tramiteId: String
    ): Response<ApiResponse<MiTramite>>
}
