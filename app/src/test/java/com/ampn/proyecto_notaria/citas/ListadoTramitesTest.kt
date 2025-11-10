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
 * Pruebas unitarias para el listado de trámites (citas) en CitasRepositorio.
 *
 * HU-11: Listado con estados de trámites.
 */
@RunWith(MockitoJUnitRunner::class)
class ListadoTramitesTest {

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
     * GIVEN un ID de usuario
     * WHEN se llama a obtenerCitasUsuario y el servidor responde con una lista de citas
     * THEN el repositorio debe devolver un resultado exitoso con la lista de citas.
     */
    @Test
    fun `obtenerCitasUsuario con respuesta exitosa debe devolver una lista de citas`() {
        runBlocking {
            // Arrange: Preparamos una respuesta exitosa con una lista de 2 citas
            val userId = 1
            val listaCitasMock = listOf(
                CitaResponse(id = 1, estado = "AGENDADO", tramiteNombre = "Trámite 1", fecha = "", hora = "", precio = 100.0, tramiteRequisitos = null, observaciones = null),
                CitaResponse(id = 2, estado = "FINALIZADO", tramiteNombre = "Trámite 2", fecha = "", hora = "", precio = 200.0, tramiteRequisitos = null, observaciones = null)
            )
            val mockApiResponse = ApiResponse(success = true, data = listaCitasMock, mensaje = "OK")
            whenever(mockCitasService.obtenerMisCitas(any())).thenReturn(Response.success(mockApiResponse))

            // Act: Ejecutamos la función a probar
            val resultado = citasRepositorio.obtenerCitasUsuario(userId)

            // Assert: Verificamos que el resultado fue un éxito y contiene los datos correctos
            assertTrue(resultado.isSuccess)
            assertEquals(2, resultado.getOrNull()?.size)
            assertEquals("Trámite 1", resultado.getOrNull()?.get(0)?.tramiteNombre)
            verify(mockCitasService).obtenerMisCitas(userId.toString())
        }
    }

    /**
     * PRUEBA 2: Respuesta Vacía
     * GIVEN un ID de usuario sin citas registradas
     * WHEN se llama a obtenerCitasUsuario y el servidor responde con una lista vacía
     * THEN el repositorio debe devolver un resultado exitoso con una lista vacía.
     */
    @Test
    fun `obtenerCitasUsuario con respuesta vacía debe devolver una lista vacía exitosamente`() {
        runBlocking {
            // Arrange: Preparamos una respuesta exitosa pero con una lista vacía
            val userId = 2
            val listaVacia = emptyList<CitaResponse>()
            val mockApiResponse = ApiResponse(success = true, data = listaVacia, mensaje = "Sin citas")
            whenever(mockCitasService.obtenerMisCitas(any())).thenReturn(Response.success(mockApiResponse))

            // Act
            val resultado = citasRepositorio.obtenerCitasUsuario(userId)

            // Assert
            assertTrue(resultado.isSuccess)
            assertTrue(resultado.getOrNull()?.isEmpty() == true)
            verify(mockCitasService).obtenerMisCitas(userId.toString())
        }
    }
}