package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import com.google.android.material.textfield.TextInputEditText

class PerfilActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion
    private lateinit var textViewNombreCompleto: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewDni: TextView
    private lateinit var textViewNombre: TextView
    private lateinit var textViewApellidos: TextView
    private lateinit var textViewFechaNacimiento: TextView
    private lateinit var textViewEmailDetalle: TextView
    private lateinit var editTextDireccion: TextInputEditText
    private lateinit var editTextTelefono: TextInputEditText
    private lateinit var buttonGuardarCambios: Button
    private lateinit var buttonCerrarSesion: Button

    private var modoEdicion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        gestorSesion = GestorSesion(this)

        if (!gestorSesion.estaAutenticado()) {
            redirigirALogin()
            return
        }

        inicializarVistas()
        cargarDatosUsuario()
        configurarBotones()
    }

    private fun inicializarVistas() {
        textViewNombreCompleto = findViewById(R.id.textViewNombreCompleto)
        textViewEmail = findViewById(R.id.textViewEmail)
        textViewDni = findViewById(R.id.textViewDni)
        textViewNombre = findViewById(R.id.textViewNombre)
        textViewApellidos = findViewById(R.id.textViewApellidos)
        textViewFechaNacimiento = findViewById(R.id.textViewFechaNacimiento)
        textViewEmailDetalle = findViewById(R.id.textViewEmailDetalle)
        editTextDireccion = findViewById(R.id.editTextDireccion)
        editTextTelefono = findViewById(R.id.editTextTelefono)
        buttonGuardarCambios = findViewById(R.id.buttonGuardarCambios)
        buttonCerrarSesion = findViewById(R.id.buttonCerrarSesion)

        // El botón Editar ya no existe, la lógica de edición se elimina
        // El botón Volver ya no existe, se usa el del sistema
    }

    private fun cargarDatosUsuario() {
        val usuario = gestorSesion.obtenerUsuario()

        usuario?.let {
            textViewNombreCompleto.text = it.nombreCompleto()
            textViewEmail.text = it.correo
            textViewDni.text = it.nroDocumento
            textViewNombre.text = it.nombres
            textViewApellidos.text = "${it.apellidoPaterno} ${it.apellidoMaterno}".trim()
            textViewFechaNacimiento.text = it.fechaRegistro ?: "No especificada"
            textViewEmailDetalle.text = it.correo
            editTextDireccion.setText(it.direccion ?: "")
            editTextTelefono.setText(it.telefono ?: "")
        }
    }

    private fun configurarBotones() {
        // La lógica del botón Editar se ha eliminado

        buttonGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        buttonCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        findViewById<View>(R.id.layoutNotificaciones).setOnClickListener {
            mostrarDialogoNotificaciones()
        }

        findViewById<View>(R.id.layoutTerminos).setOnClickListener {
            mostrarDialogoTerminos()
        }

        findViewById<View>(R.id.layoutPrivacidad).setOnClickListener {
            mostrarDialogoPrivacidad()
        }
    }

    // La función toggleModoEdicion() se ha eliminado

    private fun guardarCambios() {
        val nuevaDireccion = editTextDireccion.text.toString().trim()
        val nuevoTelefono = editTextTelefono.text.toString().trim()

        if (nuevoTelefono.isEmpty()) {
            editTextTelefono.error = "El teléfono es obligatorio"
            return
        }

        val usuario = gestorSesion.obtenerUsuario()
        usuario?.let {
            val usuarioActualizado = it.copy(
                direccion = nuevaDireccion.ifEmpty { null },
                telefono = nuevoTelefono
            )

            gestorSesion.guardarUsuario(usuarioActualizado)

            Toast.makeText(this, "✅ Datos actualizados correctamente", Toast.LENGTH_SHORT).show()

            // Al guardar, ya no se sale del modo edición porque no existe
        }
    }

    private fun mostrarDialogoNotificaciones() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Preferencias de notificaciones")
            .setMessage("Aquí podrás configurar qué notificaciones deseas recibir:")
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun mostrarDialogoTerminos() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Términos y Condiciones")
            .setMessage("Al usar esta aplicación, aceptas nuestros términos de servicio.")
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarDialogoPrivacidad() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Política de Privacidad")
            .setMessage("Nos tomamos en serio tu privacidad. Lee nuestra política completa en nuestra web.")
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun cerrarSesion() {
        gestorSesion.cerrarSesion()
        Toast.makeText(this, "✅ Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
        redirigirALogin()
    }

    private fun redirigirALogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
