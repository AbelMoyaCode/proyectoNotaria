package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import java.text.NumberFormat
import java.util.Locale

/**
 * Activity para mostrar el detalle completo de un trámite notarial.
 */
class DetalleTramiteActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion
    private var tramiteCodigo: String? = null
    private var tramiteNombre: String? = null
    private var tramiteDescripcion: String? = null
    private var tramiteRequisitos: String? = null
    private var tramitePrecio: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_detalle_tramite)

            gestorSesion = GestorSesion(this)

            // Recibir datos del trámite desde el Intent
            tramiteCodigo = intent.getStringExtra("TRAMITE_CODIGO")
            tramiteNombre = intent.getStringExtra("TRAMITE_NOMBRE")
            tramiteDescripcion = intent.getStringExtra("TRAMITE_DESCRIPCION")
            tramiteRequisitos = intent.getStringExtra("TRAMITE_REQUISITOS")
            tramitePrecio = intent.getDoubleExtra("TRAMITE_PRECIO", 0.0)

            if (tramiteCodigo == null || tramiteNombre == null) {
                Toast.makeText(this, "Error: No se pudo cargar el trámite", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            inicializarVistas()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun inicializarVistas() {
        try {
            // Inicializar vistas de forma segura
            val textViewTitulo = findViewById<TextView>(R.id.textViewTituloTramite)
            val textViewDescripcion = findViewById<TextView>(R.id.textViewDescripcion)
            val textViewRequisitos = findViewById<TextView>(R.id.textViewRequisitos)
            val textViewPrecio = findViewById<TextView>(R.id.textViewPrecio)
            val buttonAgendarCita = findViewById<Button>(R.id.buttonAgendarCita)
            val buttonVolver = findViewById<ImageButton>(R.id.buttonVolver)

            // Mostrar información del trámite
            textViewTitulo?.text = tramiteNombre
            textViewDescripcion?.text = tramiteDescripcion ?: "Sin descripción"
            textViewRequisitos?.text = formatearRequisitos(tramiteRequisitos ?: "No especificado")

            // Formatear precio en soles peruanos
            val formatoPrecio = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
            textViewPrecio?.text = formatoPrecio.format(tramitePrecio)

            // Configurar listeners de forma segura
            buttonAgendarCita?.setOnClickListener {
                agendarCita()
            }

            buttonVolver?.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar información: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    /**
     * Formatea los requisitos para mostrarlos como lista con viñetas
     */
    private fun formatearRequisitos(requisitos: String): String {
        return when {
            requisitos.contains(",") -> {
                requisitos.split(",").joinToString("\n") { "• ${it.trim()}" }
            }
            requisitos.contains(";") -> {
                requisitos.split(";").joinToString("\n") { "• ${it.trim()}" }
            }
            else -> "• $requisitos"
        }
    }

    /**
     * Inicia el proceso para agendar una cita
     */
    private fun agendarCita() {
        // Verificar autenticación
        if (!gestorSesion.estaAutenticado()) {
            Toast.makeText(
                this,
                "Debe iniciar sesión para agendar una cita",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            return
        }

        // Navegar a la pantalla de agendar cita SIN cerrar esta activity
        val intent = Intent(this, AgendarCitaActivity::class.java)
        intent.putExtra("TRAMITE_CODIGO", tramiteCodigo)
        intent.putExtra("TRAMITE_NOMBRE", tramiteNombre)
        intent.putExtra("TRAMITE_DESCRIPCION", tramiteDescripcion)
        intent.putExtra("TRAMITE_REQUISITOS", tramiteRequisitos)
        intent.putExtra("TRAMITE_PRECIO", tramitePrecio)
        startActivity(intent)
        // NO llamar finish() aquí para mantener la activity viva
    }

    // Sobrescribir onBackPressed para manejar el botón atrás del sistema
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Solo cerrar esta activity, no toda la app
        super.onBackPressed()
    }
}
