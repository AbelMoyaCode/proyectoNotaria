package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.repositorios.AutenticacionRepositorio
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val repositorio = AutenticacionRepositorio()
    private lateinit var gestorSesion: GestorSesion

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d(TAG, "onCreate - LoginActivity iniciado")

        gestorSesion = GestorSesion(this)


        // Verificar si ya está autenticado
        if (gestorSesion.estaAutenticado()) {
            Log.d(TAG, "Usuario ya autenticado, redirigiendo...")
            irAPantallaPrincipal()
            return
        }

        val btnIniciarSesion = findViewById<Button>(R.id.buttonIniciarSesion)

        btnIniciarSesion.setOnClickListener {
            Log.d(TAG, "Botón Iniciar Sesión clickeado")

            val correo = findViewById<EditText>(R.id.editTextNroDni).text.toString().trim()
            val contrasena = findViewById<EditText>(R.id.editTextContrasena).text.toString()

            Log.d(TAG, "Correo ingresado: $correo")

            // Validaciones
            if (correo.isBlank() || contrasena.isBlank()) {
                Log.w(TAG, "Validación fallida: campos vacíos")
                Toast.makeText(this, "Por favor, ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Deshabilitar botón mientras se procesa
            btnIniciarSesion.isEnabled = false
            btnIniciarSesion.text = "Iniciando..."

            Log.d(TAG, "Iniciando petición de login al backend...")

            // Llamar a la API
            lifecycleScope.launch {
                try {
                    val resultado = repositorio.login(correo, contrasena)

                    resultado.onSuccess { loginResponse ->
                        Log.d(TAG, "Login exitoso: ${loginResponse.usuario.nombres}")

                        // Guardar token y usuario
                        loginResponse.token?.let {
                            gestorSesion.guardarToken(it)
                            Log.d(TAG, "Token guardado")
                        }
                        gestorSesion.guardarUsuario(loginResponse.usuario)
                        Log.d(TAG, "Usuario guardado en sesión")

                        Toast.makeText(
                            this@LoginActivity,
                            "¡Bienvenido ${loginResponse.usuario.nombres}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        irAPantallaPrincipal()
                    }

                    resultado.onFailure { error ->
                        Log.e(TAG, "Error en login: ${error.message}", error)
                        btnIniciarSesion.isEnabled = true
                        btnIniciarSesion.text = "Iniciar sesión"

                        Toast.makeText(
                            this@LoginActivity,
                            "Error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Excepción durante login: ${e.message}", e)
                    btnIniciarSesion.isEnabled = true
                    btnIniciarSesion.text = "Iniciar sesión"

                    Toast.makeText(
                        this@LoginActivity,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun irAPantallaPrincipal() {
        Log.d(TAG, "Navegando a MainActivity")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Manejar el botón atrás del sistema
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Volver a MainActivity sin cerrar la app
        super.onBackPressed()
    }
}