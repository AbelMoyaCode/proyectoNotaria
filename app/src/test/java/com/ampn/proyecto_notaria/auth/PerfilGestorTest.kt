package com.ampn.proyecto_notaria.auth

import android.content.Context
import android.content.SharedPreferences
import com.ampn.proyecto_notaria.api.modelos.Usuario
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import com.google.gson.Gson
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
 * Pruebas unitarias para la gestión del perfil de usuario en GestorSesion.
 *
 * HU-04: Gestión de perfil (guardado).
 */
@RunWith(MockitoJUnitRunner::class)
class PerfilGestorTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var gestorSesion: GestorSesion
    private val gson = Gson()

    @Before
    fun setUp() {
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)

        // Entrenar al editor para que permita llamadas encadenadas
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)

        gestorSesion = GestorSesion(mockContext)
    }

    /**
     * PRUEBA UNITARIA HU-04
     * GIVEN un objeto de Usuario válido
     * WHEN se llama al método guardarUsuario()
     * THEN se debe serializar el usuario a JSON y guardarlo en SharedPreferences,
     *      y el estado de login debe ser 'true'.
     */
    @Test
    fun `guardarUsuario debe serializar y persistir el perfil del usuario`() {
        // Arrange: Crear un usuario de ejemplo
        val usuarioDePrueba = Usuario(
            id = 1,
            nroDocumento = "12345678",
            nombres = "Abel",
            apellidoPaterno = "Moya",
            apellidoMaterno = "Napa",
            correo = "abel.moya@test.com",
            telefono = "987654321",
            direccion = "Av. Siempre Viva 123"
        )
        val usuarioJson = gson.toJson(usuarioDePrueba) // Convertir a JSON

        // Act: Ejecutar la función a probar
        gestorSesion.guardarUsuario(usuarioDePrueba)

        // Verify: Comprobar que se llamó a los métodos correctos
        // 1. ¿Se intentó guardar el string JSON con la clave "usuario"?
        verify(mockEditor).putString("usuario", usuarioJson)

        // 2. ¿Se intentó establecer el estado de login a 'true'?
        verify(mockEditor).putBoolean("is_logged_in", true)

        // 3. ¿Se aplicaron los cambios al final?
        verify(mockEditor).apply()
    }
}
