package com.ampn.proyecto_notaria.control

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Patterns
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

        val editTextNroDocumento = findViewById<EditText>(R.id.editTextNroDocumento)
        val editTextNombre = findViewById<EditText>(R.id.editTextNombre)
        val editTextApellidoPaterno = findViewById<EditText>(R.id.editTextApellidoPaterno)
        val editTextApellidoMaterno = findViewById<EditText>(R.id.editTextApellidoMaterno)
        val editTextFechaNacimiento = findViewById<EditText>(R.id.editTextFechaNacimiento)
        val editTextCorreo = findViewById<EditText>(R.id.editTextCorreo)
        val editTextDireccion = findViewById<EditText>(R.id.editTextDireccion)
        val editTextContrasena = findViewById<EditText>(R.id.editTextContrasena)
        val editTextRepetirContrasena = findViewById<EditText>(R.id.editTextRepetirContrasena)
        val btnRegistrar = findViewById<Button>(R.id.buttonRegistrar)

        // Configurar DNI: solo números y máximo 8 dígitos
        editTextNroDocumento.inputType = InputType.TYPE_CLASS_NUMBER
        editTextNroDocumento.filters = arrayOf(InputFilter.LengthFilter(8))

        // Listener para mostrar Toast cuando se exceda el límite
        editTextNroDocumento.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s?.length == 8) {
                    Toast.makeText(
                        this@RegistroActivity,
                        "⚠️ El DNI debe tener exactamente 8 dígitos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        btnRegistrar.setOnClickListener {
            val tipoDoc = "DNI"
            val nroDocumento = editTextNroDocumento.text.toString().trim()
            val nombre = editTextNombre.text.toString().trim()
            val apellidoPaterno = editTextApellidoPaterno.text.toString().trim()
            val apellidoMaterno = editTextApellidoMaterno.text.toString().trim()
            val fechaNacimiento = editTextFechaNacimiento.text.toString().trim()
            val correo = editTextCorreo.text.toString().trim()
            val direccion = editTextDireccion.text.toString().trim()
            val contrasena = editTextContrasena.text.toString()
            val repetirContrasena = editTextRepetirContrasena.text.toString()

            // ========== VALIDACIONES ==========

            // Validar campos vacíos
            if (listOf(nroDocumento, nombre, apellidoPaterno, apellidoMaterno, fechaNacimiento, correo, contrasena).any { it.isBlank() }) {
                Toast.makeText(this, "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar DNI: exactamente 8 dígitos
            if (nroDocumento.length != 8) {
                Toast.makeText(this, "⚠️ El DNI debe tener exactamente 8 dígitos", Toast.LENGTH_LONG).show()
                editTextNroDocumento.requestFocus()
                return@setOnClickListener
            }

            // Validar que DNI contenga solo números
            if (!nroDocumento.matches(Regex("^\\d{8}$"))) {
                Toast.makeText(this, "⚠️ El DNI debe contener solo números", Toast.LENGTH_SHORT).show()
                editTextNroDocumento.requestFocus()
                return@setOnClickListener
            }

            // Validar que el nombre contenga solo letras
            if (!nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$"))) {
                Toast.makeText(this, "⚠️ El nombre solo debe contener letras", Toast.LENGTH_SHORT).show()
                editTextNombre.requestFocus()
                return@setOnClickListener
            }

            // Validar apellidos
            if (!apellidoPaterno.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$"))) {
                Toast.makeText(this, "⚠️ El apellido paterno solo debe contener letras", Toast.LENGTH_SHORT).show()
                editTextApellidoPaterno.requestFocus()
                return@setOnClickListener
            }

            if (!apellidoMaterno.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$"))) {
                Toast.makeText(this, "⚠️ El apellido materno solo debe contener letras", Toast.LENGTH_SHORT).show()
                editTextApellidoMaterno.requestFocus()
                return@setOnClickListener
            }

            // Validar formato de correo electrónico
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "⚠️ Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show()
                editTextCorreo.requestFocus()
                return@setOnClickListener
            }

            // Validar contraseñas
            if (contrasena != repetirContrasena) {
                Toast.makeText(this, "⚠️ Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                editTextRepetirContrasena.requestFocus()
                return@setOnClickListener
            }

            if (contrasena.length < 6) {
                Toast.makeText(this, "⚠️ La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                editTextContrasena.requestFocus()
                return@setOnClickListener
            }

            // ========== FIN VALIDACIONES ==========

            // Crear solicitud de registro con los parámetros correctos
            val request = RegistroUsuarioRequest(
                nroDocumento = nroDocumento,
                nombre = nombre,
                apellidoPaterno = apellidoPaterno,
                apellidoMaterno = apellidoMaterno,
                fechaNacimiento = fechaNacimiento,
                correo = correo,
                contrasena = contrasena,
                direccion = direccion.ifBlank { null },
                telefono = null
            )

            // Deshabilitar botón mientras se procesa
            btnRegistrar.isEnabled = false

            // Llamar a la API
            lifecycleScope.launch {
                val resultado = repositorio.registrarUsuario(request)

                resultado.onSuccess { usuario ->
                    Toast.makeText(
                        this@RegistroActivity,
                        "✅ ¡Registro exitoso! Bienvenido ${usuario.nombres}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

                resultado.onFailure { error ->
                    btnRegistrar.isEnabled = true
                    val mensaje = when {
                        error.message?.contains("correo", ignoreCase = true) == true ->
                            "❌ El correo ya está registrado"
                        error.message?.contains("documento", ignoreCase = true) == true ->
                            "❌ El número de documento ya está registrado"
                        else -> "❌ Error en el registro: ${error.message}"
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