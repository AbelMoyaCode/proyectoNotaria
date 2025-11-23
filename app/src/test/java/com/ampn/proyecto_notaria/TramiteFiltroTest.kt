package com.ampn.proyecto_notaria

import com.ampn.proyecto_notaria.api.modelos.Tramite
import com.ampn.proyecto_notaria.control.TramiteFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TramiteFiltroTest {

    private lateinit var tramiteFilter: TramiteFilter
    private lateinit var listaCompletaDeTramites: List<Tramite>

    @Before
    fun setUp() {
        // 1. Preparación General: se ejecuta antes de cada prueba
        tramiteFilter = TramiteFilter()

        listaCompletaDeTramites = listOf(
            Tramite(id = 1, codigo = "T001", nombre = "Poder General Amplio", descripcion = "", requisitos = "", precio = 150.0),
            Tramite(id = 2, codigo = "T002", nombre = "Constitución de Empresa", descripcion = "", requisitos = "", precio = 500.0),
            Tramite(id = 3, codigo = "T003", nombre = "Sucesión Intestada", descripcion = "", requisitos = "", precio = 800.0),
            Tramite(id = 4, codigo = "T004", nombre = "Poder por Escritura Pública", descripcion = "", requisitos = "", precio = 200.0)
        )
    }

    @Test
    fun `al buscar con texto, debe devolver tramites que coinciden`() {
        // 2. Acción (Act)
        val resultado = tramiteFilter.filter(listaCompletaDeTramites, "Poder")

        // 3. Verificación (Assert)
        assertEquals("Deberían encontrarse 2 trámites con la palabra 'Poder'", 2, resultado.size)
        assertTrue("Todos los resultados deben contener la palabra 'Poder'", resultado.all { it.nombre.contains("Poder", ignoreCase = true) })
    }

    @Test
    fun `al buscar con texto que no coincide, debe devolver una lista vacia`() {
        // Acción
        val resultado = tramiteFilter.filter(listaCompletaDeTramites, "xyz123")

        // Verificación
        assertTrue("La lista debería estar vacía para una búsqueda sin resultados", resultado.isEmpty())
    }

    @Test
    fun `la busqueda debe ser insensible a mayusculas y minusculas`() {
        // Acción
        val resultado = tramiteFilter.filter(listaCompletaDeTramites, "empresa") // en minúsculas

        // Verificación
        assertEquals("Debería encontrar 1 resultado aunque la búsqueda sea en minúsculas", 1, resultado.size)
        assertEquals("Constitución de Empresa", resultado.first().nombre)
    }

    @Test
    fun `al buscar con texto vacio, debe devolver la lista completa`() {
        // Acción
        val resultado = tramiteFilter.filter(listaCompletaDeTramites, "   ") // string con espacios

        // Verificación
        assertEquals("Una búsqueda en blanco debe devolver la lista original", listaCompletaDeTramites.size, resultado.size)
    }

    @Test
    fun `la busqueda debe funcionar con coincidencias parciales`() {
        // Acción
        val resultado = tramiteFilter.filter(listaCompletaDeTramites, "General")

        // Verificación
        assertEquals("Debería encontrar 1 resultado para una coincidencia parcial", 1, resultado.size)
        assertEquals("Poder General Amplio", resultado.first().nombre)
    }
}