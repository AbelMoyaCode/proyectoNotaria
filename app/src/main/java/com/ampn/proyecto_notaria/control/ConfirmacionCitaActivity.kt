package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.CrearCitaRequest
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Activity de confirmación de cita agendada.
 */
class ConfirmacionCitaActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion
    private val repositorioCitas = CitasRepositorio()

    private var tramiteCodigo: String? = null
    private var tramiteNombre: String? = null
    private var tramiteDescripcion: String? = null
    private var tramiteRequisitos: String? = null
    private var tramitePrecio: Double = 0.0
    private var fecha: String? = null
    private var horario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_cita)

        gestorSesion = GestorSesion(this)

        // Verificar autenticación
        if (!gestorSesion.estaAutenticado()) {
            Toast.makeText(this, "Debe iniciar sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Recibir datos
        tramiteCodigo = intent.getStringExtra("TRAMITE_CODIGO")
        tramiteNombre = intent.getStringExtra("TRAMITE_NOMBRE")
        tramiteDescripcion = intent.getStringExtra("TRAMITE_DESCRIPCION")
        tramiteRequisitos = intent.getStringExtra("TRAMITE_REQUISITOS")
        tramitePrecio = intent.getDoubleExtra("TRAMITE_PRECIO", 0.0)
        fecha = intent.getStringExtra("FECHA")
        horario = intent.getStringExtra("HORARIO")

        if (tramiteCodigo == null || fecha == null || horario == null) {
            Toast.makeText(this, "Datos incompletos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarVistas()
        configurarBotones()
    }

    private fun inicializarVistas() {
        // Información del trámite
        findViewById<TextView>(R.id.textViewTitulo).text = "Confirmar Cita"
        findViewById<TextView>(R.id.textViewNombreTramite).text = tramiteNombre
        findViewById<TextView>(R.id.textViewDescripcionTramite).text = tramiteDescripcion ?: "Sin descripción"

        val formatoPrecio = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        findViewById<TextView>(R.id.textViewPrecioTramite).text = formatoPrecio.format(tramitePrecio)

        // Formatear fecha para mostrar
        try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
            val fechaDate = formatoEntrada.parse(fecha!!)
            val fechaFormateada = fechaDate?.let { formatoSalida.format(it) } ?: fecha

            findViewById<TextView>(R.id.textViewFechaCita).text = "Fecha: $fechaFormateada"
        } catch (e: Exception) {
            findViewById<TextView>(R.id.textViewFechaCita).text = "Fecha: $fecha"
        }

        findViewById<TextView>(R.id.textViewHorarioCita).text = "Horario: $horario"
        findViewById<TextView>(R.id.textViewRequisitos).text = "Requisitos: ${tramiteRequisitos ?: "No especificado"}"
    }

    private fun configurarBotones() {
        val buttonConfirmar = findViewById<Button>(R.id.buttonConfirmarCita)
        val buttonCancelar = findViewById<Button>(R.id.buttonCancelarCita)

        buttonConfirmar.setOnClickListener {
            confirmarYGuardarCita()
        }

        // Solo volver a la pantalla anterior, NO cerrar la app
        buttonCancelar.setOnClickListener {
            finish()
        }
    }

    private fun confirmarYGuardarCita() {
        val usuario = gestorSesion.obtenerUsuario()
        val usuarioId = usuario?.id

        if (usuario == null || usuarioId.isNullOrBlank()) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Deshabilitar botón mientras se procesa
        val buttonConfirmar = findViewById<Button>(R.id.buttonConfirmarCita)
        buttonConfirmar.isEnabled = false
        buttonConfirmar.text = "Procesando..."

        lifecycleScope.launch {
            try {
                // Crear objeto de solicitud
                val solicitud = CrearCitaRequest(
                    usuarioId = usuarioId,
                    tramiteCodigo = tramiteCodigo!!,
                    fecha = fecha!!,
                    hora = horario!!
                )

                val resultado = repositorioCitas.crearCita(solicitud)

                resultado.onSuccess { citaCreada ->
                    Toast.makeText(
                        this@ConfirmacionCitaActivity,
                        "¡Cita agendada exitosamente!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navegar a MainActivity sin cerrar el stack completo
                    val intent = Intent(this@ConfirmacionCitaActivity, MainActivity::class.java)
                    // NO usar FLAG_ACTIVITY_CLEAR_TOP para no cerrar todo
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    // Cerrar solo esta pantalla de confirmación
                    finish()
                }

                resultado.onFailure { error ->
                    buttonConfirmar.isEnabled = true
                    buttonConfirmar.text = "Confirmar Cita"

                    val mensaje = when {
                        error.message?.contains("ya tiene una cita", ignoreCase = true) == true ->
                            "Ya tienes una cita agendada para este día"
                        error.message?.contains("no disponible", ignoreCase = true) == true ->
                            "Este horario ya no está disponible"
                        else -> "Error al agendar cita: ${error.message}"
                    }

                    Toast.makeText(this@ConfirmacionCitaActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                buttonConfirmar.isEnabled = true
                buttonConfirmar.text = "Confirmar Cita"
                Toast.makeText(
                    this@ConfirmacionCitaActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Manejar el botón atrás del sistema
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // Volver a la pantalla anterior, NO cerrar la app
    }
}
