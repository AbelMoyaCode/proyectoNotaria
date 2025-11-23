package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DetalleMiCitaActivity : AppCompatActivity() {

    private lateinit var citasRepositorio: CitasRepositorio
    private val reprogramValidator = ReprogramValidator() // Instancia del validador

    // Vistas
    private lateinit var textViewTitulo: TextView
    private lateinit var textViewDescripcion: TextView
    private lateinit var textViewRequisitos: TextView
    private lateinit var textViewEstado: TextView
    private lateinit var textViewObservaciones: TextView
    private lateinit var buttonReprogramar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mi_cita)

        citasRepositorio = CitasRepositorio()
        inicializarVistas()

        val citaId = intent.getIntExtra("CITA_ID", -1)
        if (citaId == -1) {
            Toast.makeText(this, "Error: ID de cita no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarDetallesCita(citaId)
    }

    private fun inicializarVistas() {
        textViewTitulo = findViewById(R.id.textViewTituloTramite)
        textViewDescripcion = findViewById(R.id.textViewDescripcion)
        textViewRequisitos = findViewById(R.id.textViewRequisitos)
        textViewEstado = findViewById(R.id.textViewEstado)
        textViewObservaciones = findViewById(R.id.textViewObservaciones)
        buttonReprogramar = findViewById(R.id.buttonReprogramarCita)
    }

    private fun cargarDetallesCita(citaId: Int) {
        lifecycleScope.launch {
            try {
                val resultado = citasRepositorio.obtenerDetalleCita(citaId)
                resultado.onSuccess { cita ->
                    mostrarInformacion(cita)
                }
                resultado.onFailure { error ->
                    Toast.makeText(this@DetalleMiCitaActivity, "Error al cargar detalles: ${error.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetalleMiCitaActivity, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun mostrarInformacion(cita: CitaResponse) {
        textViewTitulo.text = cita.tramiteNombre
        textViewDescripcion.text = cita.tramiteDescripcion ?: "Sin descripción."
        textViewEstado.text = cita.estado ?: "No definido"
        textViewRequisitos.text = formatearRequisitos(cita.tramiteRequisitos ?: "No se especificaron requisitos.")
        textViewObservaciones.text = cita.observaciones ?: "Sin observaciones."

        // Lógica para habilitar el botón de reprogramar
        configurarBotonReprogramar(cita)
    }

    private fun configurarBotonReprogramar(cita: CitaResponse) {
        try {
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaCita: Date? = formatoFecha.parse(cita.fecha)

            if (fechaCita != null && reprogramValidator.esReprogramable(fechaCita)) {
                buttonReprogramar.isEnabled = true
                buttonReprogramar.setOnClickListener {
                    val intent = Intent(this, AgendarCitaActivity::class.java).apply {
                        putExtra("MODO_REPROGRAMACION", true)
                        putExtra("CITA_ID_REPROGRAMAR", cita.id)
                        putExtra("TRAMITE_CODIGO", cita.tramiteCodigo)
                        putExtra("TRAMITE_NOMBRE", cita.tramiteNombre)
                        putExtra("TRAMITE_PRECIO", cita.precio)
                        putExtra("FECHA_ACTUAL", cita.fecha)
                    }
                    startActivity(intent)
                }
            } else {
                buttonReprogramar.isEnabled = false
                buttonReprogramar.setOnClickListener {
                    Toast.makeText(this, "Las citas solo se pueden modificar con al menos 1 día de anticipación.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            buttonReprogramar.isEnabled = false
            Toast.makeText(this, "No se pudo verificar la fecha de la cita.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatearRequisitos(requisitos: String): String {
        return when {
            requisitos.contains(",") -> requisitos.split(",").joinToString("\n") { "• ${it.trim()}" }
            requisitos.contains(";") -> requisitos.split(";").joinToString("\n") { "• ${it.trim()}" }
            requisitos.isBlank() -> "No se especificaron requisitos."
            else -> "• $requisitos"
        }
    }
}
