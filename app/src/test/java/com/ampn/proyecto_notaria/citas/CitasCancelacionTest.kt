package com.ampn.proyecto_notaria.citas

import com.ampn.proyecto_notaria.api.modelos.ApiResponse
import com.ampn.proyecto_notaria.api.modelos.CancelarCitaRequest
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
// ¡NUEVOS IMPORTS! Se usan las funciones de mockito-kotlin
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

/**
 * Pruebas unitarias para la cancelación de citas en CitasRepositorio.
 * HU-10: Cancelación de citas.
 */
@RunWith(MockitoJUnitRunner::class)
class CitasCancelacionTest {

    @Mock
    private lateinit var mockCitasService: CitasService

    private lateinit var citasRepositorio: CitasRepositorio

    // Se usa el argumentCaptor de mockito-kotlin
    private val cancelarRequestCaptor = argumentCaptor<CancelarCitaRequest>()

    @Before
    fun setUp() {
        citasRepositorio = CitasRepositorio()
        // Se mantiene la inyección por reflexión para no alterar el código original del repositorio
        try {
            val serviceField = CitasRepositorio::class.java.getDeclaredField("service")
            serviceField.isAccessible = true
            serviceField.set(citasRepositorio, mockCitasService)
        } catch (e: Exception) {
            throw RuntimeException("No se pudo inyectar el mock service via reflexión.", e)
        }
    }

    @Test
    fun `cancelarCita con respuesta exitosa debe devolver un resultado de éxito`() {
        runBlocking {
            // Arrange
            val citaId = 1
            val motivo = "Cancelado por el usuario"
            val mockCitaResponse = CitaResponse(id = citaId, estado = "CANCELADO", tramiteNombre = "Test", fecha = "", hora = "", precio = 0.0, tramiteRequisitos = null, observaciones = null)
            val mockApiResponse = ApiResponse(success = true, data = mockCitaResponse, mensaje = "OK")
            // Se usa whenever() de mockito-kotlin, que es 100% compatible y seguro
            whenever(mockCitasService.cancelarCita(any(), any())).thenReturn(Response.success(mockApiResponse))

            // Act
            val resultado = citasRepositorio.cancelarCita(citaId, motivo)

            // Assert & Verify
            assertTrue(resultado.isSuccess)
            // Se verifica la llamada y se captura el argumento de forma segura
            verify(mockCitasService).cancelarCita(any(), cancelarRequestCaptor.capture())
            assertEquals(motivo, cancelarRequestCaptor.firstValue.motivo)
        }
    }

    @Test
    fun `cancelarCita con respuesta de servidor fallida debe devolver un resultado de fallo`() {
        runBlocking {
            // Arrange
            val mockApiResponse = ApiResponse<CitaResponse>(success = false, data = null, mensaje = "Error del servidor")
            whenever(mockCitasService.cancelarCita(any(), any())).thenReturn(Response.success(mockApiResponse))
            
            // Act
            val resultado = citasRepositorio.cancelarCita(2, "motivo")

            // Assert
            assertTrue(resultado.isFailure)
            assertEquals("Error al cancelar la cita", resultado.exceptionOrNull()?.message)
        }
    }

    @Test
    fun `cancelarCita cuando hay una excepción de red debe devolver un fallo`() {
        runBlocking {
            // Arrange
            val exception = RuntimeException("Error de red simulado")
            whenever(mockCitasService.cancelarCita(any(), any())).thenThrow(exception)

            // Act
            val resultado = citasRepositorio.cancelarCita(3, "motivo")

            // Assert
            assertTrue(resultado.isFailure)
            assertEquals(exception.message, resultado.exceptionOrNull()?.message)
        }
    }
}