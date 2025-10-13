package com.ampn.proyecto_notaria.control

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.RegistroUsuarioRequest
import com.ampn.proyecto_notaria.api.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private val repositorio = AutenticacionRepositorio()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val btnRegistrar = findViewById<Button>(R.id.buttonRegistrar)

        btnRegistrar.setOnClickListener {
            val tipoDoc = "DNI" // Puedes agregar un Spinner si necesitas otros tipos
            val nroDocumento = findViewById<EditText>(R.id.editTextNroDocumento).text.toString()
            val nombre = findViewById<EditText>(R.id.editTextNombre).text.toString()
            val apellidoPaterno = findViewById<EditText>(R.id.editTextApellidoPaterno).text.toString()
            val apellidoMaterno = findViewById<EditText>(R.id.editTextApellidoMaterno).text.toString()
            val correo = findViewById<EditText>(R.id.editTextCorreo).text.toString()
            val direccion = findViewById<EditText>(R.id.editTextDireccion).text.toString()
            val contrasena = findViewById<EditText>(R.id.editTextContrasena).text.toString()
            val repetirContrasena = findViewById<EditText>(R.id.editTextRepetirContrasena).text.toString()

            // Validaciones
            if (listOf(nroDocumento, nombre, apellidoPaterno, apellidoMaterno, correo, contrasena).any { it.isBlank() }) {
                Toast.makeText(this, "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena != repetirContrasena) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear solicitud de registro
            val nombres = "$nombre"
            val apellidos = "$apellidoPaterno $apellidoMaterno".trim()

            val request = RegistroUsuarioRequest(
                tipoDocumento = tipoDoc,
                numeroDocumento = nroDocumento,
                nombres = nombres,
                apellidos = apellidos,
                correo = correo,
                password = contrasena,
                direccion = direccion.ifBlank { null },
                telefono = null // Puedes agregar campo de teléfono si lo necesitas
            )

            // Deshabilitar botón mientras se procesa
            btnRegistrar.isEnabled = false

            // Llamar a la API
            lifecycleScope.launch {
                val resultado = repositorio.registrarUsuario(request)

                resultado.onSuccess { usuario ->
                    Toast.makeText(
                        this@RegistroActivity,
                        "¡Registro exitoso! Bienvenido ${usuario.nombres}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

                resultado.onFailure { error ->
                    btnRegistrar.isEnabled = true
                    val mensaje = when {
                        error.message?.contains("correo", ignoreCase = true) == true ->
                            "El correo ya está registrado"
                        error.message?.contains("documento", ignoreCase = true) == true ->
                            "El número de documento ya está registrado"
                        else -> "Error en el registro: ${error.message}"
                    }
                    Toast.makeText(this@RegistroActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Manejar el botón atrás del sistema
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Volver a la pantalla anterior sin cerrar la app
        super.onBackPressed()
    }
}