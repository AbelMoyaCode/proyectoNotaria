package com.ampn.proyecto_notaria.auth

import android.content.Context
import android.content.SharedPreferences
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

/**
 * Pruebas unitarias para la clase GestorSesion.
 *
 * Objetivo: Verificar la lógica de negocio del gestor de sesión de forma aislada,
 * sin depender del framework de Android.
 *
 * HU-03: Cierre de sesión.
 */
@RunWith(MockitoJUnitRunner::class)
class GestorSesionTest {

    // --- Mocks ---
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    // --- Clase Bajo Prueba ---
    private lateinit var gestorSesion: GestorSesion

    @Before
    fun setUp() {
        // --- Configuración de Mocks ---
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)

        // --- ¡AQUÍ ESTÁ LA SOLUCIÓN! ---
        // Le enseñamos al editor a devolverse a sí mismo para permitir el encadenamiento de llamadas.
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        
        // Se crea la instancia real de GestorSesion, pero inyectándole los mocks.
        gestorSesion = GestorSesion(mockContext)
    }

    /**
     * GIVEN un usuario ha iniciado sesión
     * WHEN se llama al método cerrarSesion()
     * THEN se deben eliminar las claves de sesión ('token', 'usuario') y
     *      actualizar el estado de login a 'false'.
     */
    @Test
    fun `cerrarSesion debe eliminar token, usuario y estado de login`() {
        // --- 1. Act (Ejecutar la acción) ---
        gestorSesion.cerrarSesion()

        // --- 2. Verify (Verificar las interacciones) ---
        // Se verifica que se llamó a 'remove' para las claves correctas.
        verify(mockEditor).remove("token")
        verify(mockEditor).remove("usuario")

        // Se verifica que se actualizó el estado de login.
        verify(mockEditor).putBoolean("is_logged_in", false)

        // Se verifica que se aplicaron los cambios al final.
        verify(mockEditor).apply()
    }
}