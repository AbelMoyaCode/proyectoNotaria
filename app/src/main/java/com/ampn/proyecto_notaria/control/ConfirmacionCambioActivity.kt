package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ConfirmacionCambioActivity : AppCompatActivity() {

    private lateinit var citasRepositorio: CitasRepositorio

    private var citaId: Int = -1
    private var nuevaFecha: String? = null
    private var nuevoHorario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_cambio)

        citasRepositorio = CitasRepositorio()

        // Recibir datos
        citaId = intent.getIntExtra("CITA_ID_REPROGRAMAR", -1)
        nuevaFecha = intent.getStringExtra("NUEVA_FECHA")
        nuevoHorario = intent.getStringExtra("NUEVO_HORARIO")

        if (citaId == -1 || nuevaFecha == null || nuevoHorario == null) {
            Toast.makeText(this, "Error: Faltan datos para la reprogramación", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        inicializarVistas()
    }

    private fun inicializarVistas() {
        val textViewNuevaFecha = findViewById<TextView>(R.id.textViewNuevaFecha)
        val buttonConfirmar = findViewById<Button>(R.id.buttonConfirmarCambio)
        val buttonCancelar = findViewById<Button>(R.id.buttonCancelarCambio)

        textViewNuevaFecha.text = "${formatarFecha(nuevaFecha!!)} a las $nuevoHorario"

        buttonConfirmar.setOnClickListener {
            reprogramarCita()
        }

        buttonCancelar.setOnClickListener {
            finish()
        }
    }

    private fun reprogramarCita() {
        lifecycleScope.launch {
            try {
                val resultado = citasRepositorio.reprogramarCita(citaId, nuevaFecha!!, nuevoHorario!!)

                resultado.onSuccess { 
                    Toast.makeText(this@ConfirmacionCambioActivity, "¡Cita reprogramada con éxito!", Toast.LENGTH_LONG).show()

                    // Navegar de vuelta a la lista de citas, limpiando el stack
                    val intent = Intent(this@ConfirmacionCambioActivity, MisCitasActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }

                resultado.onFailure { error ->
                    Toast.makeText(this@ConfirmacionCambioActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ConfirmacionCambioActivity, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun formatarFecha(fechaInput: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fecha = formatoEntrada.parse(fechaInput)
            if (fecha != null) formatoSalida.format(fecha) else fechaInput
        } catch (e: Exception) {
            fechaInput // Si falla, devolver la fecha original
        }
    }
}
