package com.ampn.proyecto_notaria.viewmodels

import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.reflect.Field

@ExperimentalCoroutinesApi
class CitasViewModelTest {

    // Mocks
    private lateinit var repositorioMock: CitasRepositorio
    private lateinit var viewModel: CitasViewModel

    // Dispatcher para coroutines
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Configurar Dispatcher
        Dispatchers.setMain(testDispatcher)

        // Inicializar mocks
        repositorioMock = mock()
        
        // Inicializar ViewModel
        viewModel = CitasViewModel()
        
        // Inyectar mock en el campo privado 'repositorio' usando reflexión
        injectMockRepository(viewModel, repositorioMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Función auxiliar para inyectar el mock usando reflexión
    private fun injectMockRepository(viewModel: CitasViewModel, mockRepo: CitasRepositorio) {
        try {
            val field: Field = CitasViewModel::class.java.getDeclaredField("repositorio")
            field.isAccessible = true
            field.set(viewModel, mockRepo)
        } catch (e: Exception) {
            println("Error inyectando mock: ${e.message}")
        }
    }

    @Test
    fun `crearCita deberia llamar a onSuccess cuando el repositorio responde correctamente`() = runTest {
        // Arrange (Preparación)
        val usuarioId = 1
        val tramiteCodigo = "T001"
        val fecha = "2024-12-25"
        val hora = "10:00"
        
        // CORREGIDO: Ajustado a los campos reales de CitaResponse
        val citaEsperada = CitaResponse(
            id = 100,
            tramiteUsuarioId = 500, // Campo que sí existe (opcional)
            tramiteCodigo = tramiteCodigo,
            fecha = fecha,
            hora = hora,
            estado = "PENDIENTE",
            tramiteNombre = "Test Tramite",
            precio = 150.00, // Campo obligatorio
            tramiteDescripcion = "Descripción de prueba", // Añadido para completar
            tramiteRequisitos = "Requisitos de prueba"
        )

        // Simulamos respuesta exitosa del repositorio
        whenever(repositorioMock.crearCita(usuarioId, tramiteCodigo, fecha, hora))
            .thenReturn(Result.success(citaEsperada))

        // Act (Ejecución)
        var resultadoCita: CitaResponse? = null
        var errorMensaje: String? = null
        
        viewModel.crearCita(
            usuarioId, 
            tramiteCodigo, 
            fecha, 
            hora,
            onSuccess = { resultadoCita = it },
            onError = { errorMensaje = it }
        )
        
        // Avanzar coroutine hasta que se completen las tareas pendientes
        advanceUntilIdle()

        // Assert (Verificación)
        assertEquals("La cita devuelta debe coincidir con la esperada", citaEsperada, resultadoCita)
        assertNull("No debería haber mensaje de error", errorMensaje)
        assertEquals("El estado de carga debería ser false", false, viewModel.cargando.value)
    }

    @Test
    fun `crearCita deberia retornar error cuando el repositorio falla por conflicto de horario`() = runTest {
        // Arrange
        val usuarioId = 1
        val tramiteCodigo = "T001"
        val fecha = "2024-12-25"
        val hora = "10:00"
        val mensajeError = "Horario no disponible"

        // Simulamos fallo del repositorio (ej. ya existe una cita en ese horario)
        whenever(repositorioMock.crearCita(usuarioId, tramiteCodigo, fecha, hora))
            .thenReturn(Result.failure(Exception(mensajeError)))

        // Act
        var resultadoCita: CitaResponse? = null
        var errorCapturado: String? = null
        
        viewModel.crearCita(
            usuarioId, 
            tramiteCodigo, 
            fecha, 
            hora,
            onSuccess = { resultadoCita = it },
            onError = { errorCapturado = it }
        )
        
        // Avanzar coroutine
        advanceUntilIdle()

        // Assert
        assertNull("No debería devolver una cita", resultadoCita)
        assertEquals("El mensaje de error debe coincidir", mensajeError, errorCapturado)
        assertEquals("El estado de error del ViewModel debe actualizarse", mensajeError, viewModel.error.value)
        assertEquals("El estado de carga debería ser false", false, viewModel.cargando.value)
    }
    
    @Test
    fun `cancelarCita deberia actualizar estado cuando es exitoso`() = runTest {
        // Arrange
        val citaId = 100
        val motivo = "Cambio de planes"
        
        // CORREGIDO: Ajustado a los campos reales de CitaResponse
        val citaCancelada = CitaResponse(
            id = citaId,
            tramiteUsuarioId = 500,
            tramiteCodigo = "T001",
            fecha = "2024-12-25",
            hora = "10:00",
            estado = "CANCELADA",
            tramiteNombre = "Test Tramite",
            precio = 150.00, // Campo obligatorio
            tramiteDescripcion = "Descripción de prueba",
            tramiteRequisitos = "Requisitos de prueba"
        )
        
        whenever(repositorioMock.cancelarCita(citaId, motivo))
            .thenReturn(Result.success(citaCancelada))
            
        // Act
        var exito = false
        var errorCapturado: String? = null
        
        viewModel.cancelarCita(
            citaId,
            motivo,
            onSuccess = { exito = true },
            onError = { errorCapturado = it }
        )
        
        advanceUntilIdle()
        
        // Assert
        assertEquals("La cancelación debería ser exitosa", true, exito)
        assertNull("No debería haber errores", errorCapturado)
    }
}
