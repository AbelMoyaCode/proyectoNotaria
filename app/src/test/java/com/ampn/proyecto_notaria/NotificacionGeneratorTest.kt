package com.ampn.proyecto_notaria

import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import com.ampn.proyecto_notaria.control.NotificacionGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificacionGeneratorTest {

    private lateinit var generator: NotificacionGenerator

    @Before
    fun setUp() {
        generator = NotificacionGenerator()
    }

    private fun crearCitaDePrueba(id: Int, estado: String): CitaResponse {
        return CitaResponse(
            id = id, tramiteNombre = "Trámite de Prueba", estado = estado, 
            fecha = "2025-01-01", hora = "10:00", creadaEn = "2025-01-01T10:00:00Z",
            tramiteCodigo = "T001", precio = 0.0, tramiteUsuarioId = 1, 
            tramiteDescripcion = null, tramiteRequisitos = null, observaciones = null
        )
    }

    @Test
    fun `debe crear notificacion para estado AGENDADO`() {
        val citas = listOf(crearCitaDePrueba(1, "AGENDADO"))
        val notificaciones = generator.generar(citas)

        assertEquals(1, notificaciones.size)
        assertEquals("Cita Agendada", notificaciones[0].titulo)
    }

    @Test
    fun `debe crear notificacion para estado EN PROCESO`() {
        val citas = listOf(crearCitaDePrueba(1, "EN PROCESO"))
        val notificaciones = generator.generar(citas)

        assertEquals(1, notificaciones.size)
        assertEquals("Trámite Actualizado", notificaciones[0].titulo)
    }

    @Test
    fun `debe crear notificacion para estado REPROGRAMADO`() {
        val citas = listOf(crearCitaDePrueba(1, "REPROGRAMADO"))
        val notificaciones = generator.generar(citas)

        assertEquals(1, notificaciones.size)
        assertEquals("Cita Reprogramada", notificaciones[0].titulo)
    }

    @Test
    fun `NO debe crear notificacion para otros estados`() {
        val citas = listOf(
            crearCitaDePrueba(1, "FINALIZADO"),
            crearCitaDePrueba(2, "CANCELADO")
        )
        val notificaciones = generator.generar(citas)

        assertTrue("No se deben generar notificaciones para estados finalizados o cancelados", notificaciones.isEmpty())
    }

    @Test
    fun `debe devolver una lista vacia si no hay citas`() {
        val notificaciones = generator.generar(emptyList())
        assertTrue("La lista de notificaciones debería estar vacía si no hay citas", notificaciones.isEmpty())
    }
}
