package com.ampn.proyecto_notaria.control

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import kotlinx.coroutines.launch

class DetalleMiCitaActivity : AppCompatActivity() {

    private lateinit var citasRepositorio: CitasRepositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mi_cita)

        citasRepositorio = CitasRepositorio()

        // Recibir el ID de la cita
        val citaId = intent.getIntExtra("CITA_ID", -1)

        if (citaId == -1) {
            Toast.makeText(this, "Error: ID de cita no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar los detalles desde el backend
        cargarDetallesCita(citaId)
    }

    private fun cargarDetallesCita(citaId: Int) {
        lifecycleScope.launch {
            try {
                // Esta es la llamada al backend que implementamos
                val resultado = citasRepositorio.obtenerDetalleCita(citaId)

                resultado.onSuccess { cita ->
                    // Cuando el backend responde, mostramos la info
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
        // Encontrar las vistas en el layout
        val textViewTitulo = findViewById<TextView>(R.id.textViewTituloTramite)
        val textViewDescripcion = findViewById<TextView>(R.id.textViewDescripcion)
        val textViewRequisitos = findViewById<TextView>(R.id.textViewRequisitos)
        val textViewEstado = findViewById<TextView>(R.id.textViewEstado)
        val textViewObservaciones = findViewById<TextView>(R.id.textViewObservaciones)
        val buttonVolver = findViewById<ImageButton>(R.id.buttonVolver)

        // Asignar el texto a cada vista
        textViewTitulo.text = cita.tramiteNombre
        textViewDescripcion.text = cita.tramiteDescripcion ?: "Sin descripción."
        textViewEstado.text = cita.estado ?: "No definido"
        
        // ¡CORREGIDO! Ahora sí usamos los datos que vienen del backend.
        // Asumimos que CitaResponse ahora incluye tramiteRequisitos y observaciones.
        textViewRequisitos.text = formatearRequisitos(cita.tramiteRequisitos ?: "No se especificaron requisitos.")
        textViewObservaciones.text = cita.observaciones ?: "Sin observaciones."

        // Configurar el botón para volver
        buttonVolver.setOnClickListener {
            finish()
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