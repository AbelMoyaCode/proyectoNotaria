package com.ampn.proyecto_notaria

import com.ampn.proyecto_notaria.control.ReprogramValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

class ReprogramValidatorTest {

    private lateinit var validator: ReprogramValidator

    @Before
    fun setUp() {
        validator = ReprogramValidator()
    }
    private fun getDate(diasDesdeHoy: Int): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, diasDesdeHoy)
        }.time
    }
    @Test
    fun `una cita con mas de 1 dia de anticipacion DEBE ser reprogramable`() {
        // Cita para pasado mañana (2 días en el futuro)
        val fechaCita = getDate(2)
        assertTrue("Una cita para dentro de 2 días debería ser reprogramable", validator.esReprogramable(fechaCita))
    }
    @Test
    fun `una cita para manana NO debe ser reprogramable`() {
        // Cita para mañana (1 día en el futuro)
        val fechaCita = getDate(1)
        assertFalse("Una cita para mañana no debería ser reprogramable", validator.esReprogramable(fechaCita))
    }
    @Test
    fun `una cita para hoy NO debe ser reprogramable`() {
        // Cita para hoy
        val fechaCita = getDate(0)
        assertFalse("Una cita para hoy no debería ser reprogramable", validator.esReprogramable(fechaCita))
    }
    @Test
    fun `una cita pasada NO debe ser reprogramable`() {
        // Cita para ayer
        val fechaCita = getDate(-1)
        assertFalse("Una cita pasada no debería ser reprogramable", validator.esReprogramable(fechaCita))
    }
}
