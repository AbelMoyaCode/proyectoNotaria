package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import com.google.android.material.textfield.TextInputEditText

/**
 * HU-04: Gestión de Perfil del Cliente
 * Permite ver y editar los datos del usuario
 * Permite cerrar sesión
 *
 * PRUEBAS:
 * - Ver todos los datos del usuario registrado
 * - Editar dirección y teléfono
 * - No poder editar DNI, nombre, apellidos, fecha nacimiento, email
 * - Cerrar sesión correctamente
 */
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
    private lateinit var buttonEditar: ImageButton

    private var modoEdicion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        gestorSesion = GestorSesion(this)

        // Verificar autenticación
        if (!gestorSesion.estaAutenticado()) {
            redirigirALogin()
            return
        }

        inicializarVistas()
        cargarDatosUsuario()
        configurarBotones()

        // PRUEBA HU-04: Verificar carga de datos
        android.util.Log.d("PRUEBA_HU04", "✅ Perfil cargado correctamente")
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
        buttonEditar = findViewById(R.id.buttonEditar)

        findViewById<ImageButton>(R.id.buttonVolver).setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosUsuario() {
        val usuario = gestorSesion.obtenerUsuario()

        usuario?.let {
            // Nombre completo en header
            textViewNombreCompleto.text = it.nombreCompleto()
            textViewEmail.text = it.correo

            // Datos NO EDITABLES
            textViewDni.text = it.nroDocumento
            textViewNombre.text = it.nombres
            textViewApellidos.text = "${it.apellidoPaterno} ${it.apellidoMaterno}".trim()
            textViewFechaNacimiento.text = it.fechaRegistro ?: "No especificada"
            textViewEmailDetalle.text = it.correo

            // Datos EDITABLES (inicialmente deshabilitados)
            editTextDireccion.setText(it.direccion ?: "")
            editTextTelefono.setText(it.telefono ?: "")

            // PRUEBA HU-04: Mostrar datos cargados
            android.util.Log.d("PRUEBA_HU04", "📋 Datos del usuario:")
            android.util.Log.d("PRUEBA_HU04", "   DNI: ${it.nroDocumento}")
            android.util.Log.d("PRUEBA_HU04", "   Nombre: ${it.nombres}")
            android.util.Log.d("PRUEBA_HU04", "   Apellidos: ${it.apellidoPaterno} ${it.apellidoMaterno}")
            android.util.Log.d("PRUEBA_HU04", "   Fecha Registro: ${it.fechaRegistro}")
            android.util.Log.d("PRUEBA_HU04", "   Email: ${it.correo}")
            android.util.Log.d("PRUEBA_HU04", "   Dirección: ${it.direccion}")
            android.util.Log.d("PRUEBA_HU04", "   Teléfono: ${it.telefono}")
        }
    }

    private fun configurarBotones() {
        // Botón Editar
        buttonEditar.setOnClickListener {
            toggleModoEdicion()
        }

        // Guardar cambios (solo dirección y teléfono)
        buttonGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        // HU-03: Cerrar Sesión
        buttonCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        // Opciones de menú con diálogos informativos
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

    private fun toggleModoEdicion() {
        modoEdicion = !modoEdicion

        // Habilitar/deshabilitar campos editables
        editTextDireccion.isEnabled = modoEdicion
        editTextTelefono.isEnabled = modoEdicion

        // Mostrar/ocultar botón guardar
        buttonGuardarCambios.visibility = if (modoEdicion) View.VISIBLE else View.GONE

        // Cambiar icono del botón editar
        buttonEditar.setImageResource(
            if (modoEdicion) android.R.drawable.ic_menu_close_clear_cancel
            else android.R.drawable.ic_menu_edit
        )

        // PRUEBA HU-04: Modo edición
        android.util.Log.d("PRUEBA_HU04", "✏️ Modo edición: ${if (modoEdicion) "ACTIVADO" else "DESACTIVADO"}")

        Toast.makeText(
            this,
            if (modoEdicion) "✏️ Modo edición activado" else "✅ Modo edición desactivado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun guardarCambios() {
        val nuevaDireccion = editTextDireccion.text.toString().trim()
        val nuevoTelefono = editTextTelefono.text.toString().trim()

        // Validación básica
        if (nuevoTelefono.isEmpty()) {
            editTextTelefono.error = "El teléfono es obligatorio"
            android.util.Log.w("PRUEBA_HU04", "⚠️ Error: Teléfono vacío")
            return
        }

        val usuario = gestorSesion.obtenerUsuario()
        usuario?.let {
            // Crear nuevo usuario con los datos actualizados
            val usuarioActualizado = it.copy(
                direccion = nuevaDireccion.ifEmpty { null },
                telefono = nuevoTelefono
            )

            // Guardar en sesión
            gestorSesion.guardarUsuario(usuarioActualizado)

            Toast.makeText(
                this,
                "✅ Datos actualizados correctamente",
                Toast.LENGTH_SHORT
            ).show()

            // PRUEBA HU-04: Datos guardados
            android.util.Log.d("PRUEBA_HU04", "💾 Datos actualizados:")
            android.util.Log.d("PRUEBA_HU04", "   Nueva Dirección: $nuevaDireccion")
            android.util.Log.d("PRUEBA_HU04", "   Nuevo Teléfono: $nuevoTelefono")

            // Desactivar modo edición
            toggleModoEdicion()
        }
    }

    private fun mostrarDialogoNotificaciones() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Preferencias de notificaciones")
            .setMessage("Aquí podrás configurar qué notificaciones deseas recibir:\n\n" +
                    "• Confirmación de citas\n" +
                    "• Recordatorios 24h antes\n" +
                    "• Cambios en trámites\n" +
                    "• Mensajes de la notaría\n\n" +
                    "Esta funcionalidad estará disponible próximamente.")
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun mostrarDialogoTerminos() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Términos y Condiciones")
            .setMessage("Al usar esta aplicación, aceptas:\n\n" +
                    "1. Uso responsable del sistema de citas\n" +
                    "2. Veracidad de la información proporcionada\n" +
                    "3. Cumplimiento de horarios agendados\n" +
                    "4. Notificación anticipada en caso de cancelación\n\n" +
                    "Última actualización: 01/11/2025")
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarDialogoPrivacidad() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Política de Privacidad")
            .setMessage("Tus datos están protegidos:\n\n" +
                    "• No compartimos tu información con terceros\n" +
                    "• Usamos encriptación para datos sensibles\n" +
                    "• Solo accedemos a datos necesarios para el servicio\n" +
                    "• Puedes solicitar eliminación de tu cuenta\n\n" +
                    "Para más información: privacidad@notaria.com")
            .setPositiveButton("Entendido", null)
            .show()
    }

    /**
     * HU-03: Implementar cierre de sesión
     * Limpia la sesión y redirige al login
     */
    private fun cerrarSesion() {
        android.util.Log.d("PRUEBA_HU03", "🔓 Cerrando sesión...")

        // Limpiar sesión
        gestorSesion.cerrarSesion()

        Toast.makeText(
            this,
            "✅ Sesión cerrada correctamente",
            Toast.LENGTH_SHORT
        ).show()

        android.util.Log.d("PRUEBA_HU03", "✅ Sesión cerrada, redirigiendo a MainActivity")

        // Redirigir a MainActivity
        redirigirALogin()
    }

    private fun redirigirALogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
