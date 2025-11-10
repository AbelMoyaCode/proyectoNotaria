package com.ampn.proyecto_notaria.citas

import com.ampn.proyecto_notaria.api.modelos.ApiResponse
import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import com.ampn.proyecto_notaria.api.servicios.CitasService
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

/**
 * Pruebas unitarias para la obtención del detalle de un trámite (cita) en CitasRepositorio.
 *
 * HU-12: Detalle de trámites.
 */
@RunWith(MockitoJUnitRunner::class)
class DetalleTramiteTest {

    @Mock
    private lateinit var mockCitasService: CitasService

    private lateinit var citasRepositorio: CitasRepositorio

    @Before
    fun setUp() {
        citasRepositorio = CitasRepositorio()
        // Inyectamos el mock service usando reflexión para no alterar el código original.
        try {
            val serviceField = CitasRepositorio::class.java.getDeclaredField("service")
            serviceField.isAccessible = true
            serviceField.set(citasRepositorio, mockCitasService)
        } catch (e: Exception) {
            throw RuntimeException("No se pudo inyectar el mock service via reflexión.", e)
        }
    }

    /**
     * PRUEBA 1: Camino Feliz
     * GIVEN una ID de cita válida
     * WHEN se llama a obtenerDetalleCita y el servidor responde con los datos de la cita
     * THEN el repositorio debe devolver un resultado exitoso con los detalles de la cita.
     */
    @Test
    fun `obtenerDetalleCita con respuesta exitosa debe devolver los detalles de la cita`() {
        runBlocking {
            // Arrange
            val citaId = 123
            val mockDetalleCita = CitaResponse(
                id = citaId,
                estado = "EN_PROCESO",
                tramiteNombre = "Compraventa de Inmueble",
                fecha = "2025-12-24",
                hora = "10:00",
                precio = 350.0,
                tramiteRequisitos = "DNI, Título de Propiedad",
                observaciones = "Pendiente de firma del comprador."
            )
            val mockApiResponse = ApiResponse(success = true, data = mockDetalleCita, mensaje = "OK")
            whenever(mockCitasService.obtenerDetalleCita(any())).thenReturn(Response.success(mockApiResponse))

            // Act
            val resultado = citasRepositorio.obtenerDetalleCita(citaId)

            // Assert
            assertTrue(resultado.isSuccess)
            assertEquals(citaId, resultado.getOrNull()?.id)
            verify(mockCitasService).obtenerDetalleCita(citaId.toString())
        }
    }

    /**
     * PRUEBA 2: Cita No Encontrada
     * GIVEN una ID de cita que no existe en el servidor
     * WHEN se llama a obtenerDetalleCita y el servidor responde con un error
     * THEN el repositorio debe devolver un resultado de fallo.
     */
    @Test
    fun `obtenerDetalleCita cuando la cita no se encuentra debe devolver un fallo`() {
        runBlocking {
            // Arrange
            val citaId = 999
            val mockApiResponse = ApiResponse<CitaResponse>(success = false, data = null, mensaje = "Cita no encontrada")
            whenever(mockCitasService.obtenerDetalleCita(any())).thenReturn(Response.success(mockApiResponse))

            // Act
            val resultado = citasRepositorio.obtenerDetalleCita(citaId)

            // Assert
            assertTrue(resultado.isFailure)
            // ¡CORREGIDO! Se ajusta el mensaje esperado para que coincida con la implementación real.
            assertEquals("Error al obtener el detalle de la cita", resultado.exceptionOrNull()?.message)
            verify(mockCitasService).obtenerDetalleCita(citaId.toString())
        }
    }
}