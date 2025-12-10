package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.adapters.AdaptadorHorarios
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgendarCitaActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerViewHorarios: RecyclerView
    private lateinit var textViewFechaSeleccionada: TextView
    private lateinit var textViewHorarioSeleccionado: TextView
    private lateinit var textViewSinHorarios: TextView
    private lateinit var buttonConfirmar: Button

    // Datos del trámite
    private var tramiteCodigo: String? = null
    private var tramiteNombre: String? = null
    private var tramiteDescripcion: String? = null
    private var tramiteRequisitos: String? = null
    private var tramitePrecio: Double = 0.0

    // Selección del usuario
    private var fechaSeleccionada: String? = null
    private var horarioSeleccionado: String? = null

    // Modo de operación
    private var modoReprogramacion = false
    private var citaIdReprogramar: Int = -1
    private var fechaActualCita: String? = null

    private val horariosDisponibles = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
        "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
        "16:00", "16:30", "17:00", "17:30", "18:00"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar_cita)

        gestorSesion = GestorSesion(this)

        if (!gestorSesion.estaAutenticado()) {
            Toast.makeText(this, "Debe iniciar sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Detectar si estamos en modo de reprogramación
        modoReprogramacion = intent.getBooleanExtra("MODO_REPROGRAMACION", false)
        if (modoReprogramacion) {
            citaIdReprogramar = intent.getIntExtra("CITA_ID_REPROGRAMAR", -1)
            fechaActualCita = intent.getStringExtra("FECHA_ACTUAL")
        }

        // Recibir datos del trámite
        tramiteCodigo = intent.getStringExtra("TRAMITE_CODIGO")
        tramiteNombre = intent.getStringExtra("TRAMITE_NOMBRE")
        tramiteDescripcion = intent.getStringExtra("TRAMITE_DESCRIPCION")
        tramiteRequisitos = intent.getStringExtra("TRAMITE_REQUISITOS")
        tramitePrecio = intent.getDoubleExtra("TRAMITE_PRECIO", 0.0)

        if (tramiteCodigo == null) {
            Toast.makeText(this, "Error: No se especificó un trámite.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        inicializarVistas()
        configurarCalendario()
        configurarBotonConfirmar()
        mostrarInformacionTramite()
    }

    private fun inicializarVistas() {
        calendarView = findViewById(R.id.calendarView)
        recyclerViewHorarios = findViewById(R.id.recyclerViewHorarios)
        textViewFechaSeleccionada = findViewById(R.id.textViewFechaSeleccionada)
        textViewHorarioSeleccionado = findViewById(R.id.textViewHorarioSeleccionado)
        textViewSinHorarios = findViewById(R.id.textViewSinHorarios)
        buttonConfirmar = findViewById(R.id.buttonConfirmar)

        findViewById<Button>(R.id.buttonCancelar).setOnClickListener { finish() }

        recyclerViewHorarios.layoutManager = GridLayoutManager(this, 3)

        // Cambiar título y botón si es modo reprogramación
        if (modoReprogramacion) {
            findViewById<TextView>(R.id.textViewTituloPantalla).text = "Reprogramar Cita"
            buttonConfirmar.text = "Confirmar Cambio"
        }
    }

    private fun mostrarInformacionTramite() {
        findViewById<TextView>(R.id.textViewNombreTramite).text = tramiteNombre
        val formatoPrecio = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE"))
        findViewById<TextView>(R.id.textViewPrecioTramite).text = formatoPrecio.format(tramitePrecio)
    }

    private fun configurarCalendario() {
        val calendario = Calendar.getInstance()

        // Si estamos reprogramando, posicionar el calendario en la fecha de la cita
        if (modoReprogramacion && fechaActualCita != null) {
            try {
                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fecha = formatoFecha.parse(fechaActualCita!!)
                if (fecha != null) {
                    calendarView.date = fecha.time
                }
            } catch (e: Exception) {
                // Si falla, no hacer nada, el calendario se mostrará en la fecha de hoy
            }
        }

        calendarView.minDate = calendario.timeInMillis
        calendario.add(Calendar.MONTH, 2)
        calendarView.maxDate = calendario.timeInMillis

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            val formatoMostrar = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
            val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            textViewFechaSeleccionada.text = "Fecha: ${formatoMostrar.format(cal.time)}"
            textViewFechaSeleccionada.visibility = View.VISIBLE

            cargarHorarios()
        }
    }

    private fun cargarHorarios() {
        textViewSinHorarios.visibility = View.GONE
        recyclerViewHorarios.visibility = View.VISIBLE

        val adapter = AdaptadorHorarios(horariosDisponibles) { horario ->
            horarioSeleccionado = horario
            textViewHorarioSeleccionado.text = "Horario: $horario"
            textViewHorarioSeleccionado.visibility = View.VISIBLE
            buttonConfirmar.isEnabled = true
            Toast.makeText(this, "✓ Horario seleccionado: $horario", Toast.LENGTH_SHORT).show()
        }
        recyclerViewHorarios.adapter = adapter
    }

    private fun configurarBotonConfirmar() {
        buttonConfirmar.isEnabled = false
        buttonConfirmar.setOnClickListener {
            if (fechaSeleccionada == null || horarioSeleccionado == null) {
                Toast.makeText(this, "⚠️ Debe seleccionar una nueva fecha y horario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (modoReprogramacion) {
                // NAVEGAR A LA NUEVA PANTALLA DE CONFIRMACIÓN DE CAMBIO
                Log.d("AgendarCita", "Modo Reprogramación: Navegando a ConfirmacionCambioActivity")
                val intent = Intent(this, ConfirmacionCambioActivity::class.java).apply {
                    putExtra("CITA_ID_REPROGRAMAR", citaIdReprogramar)
                    putExtra("NUEVA_FECHA", fechaSeleccionada)
                    putExtra("NUEVO_HORARIO", horarioSeleccionado)
                }
                startActivity(intent)

            } else {
                // FLUJO NORMAL: NAVEGAR A LA CONFIRMACIÓN DE CITA ORIGINAL
                Log.d("AgendarCita", "Modo Normal: Navegando a ConfirmacionCitaActivity")
                DetalleTramiteActivity.activityList.add(this) // Mantener el cierre en cadena
                val intent = Intent(this, ConfirmacionCitaActivity::class.java).apply {
                    putExtra("TRAMITE_CODIGO", tramiteCodigo)
                    putExtra("TRAMITE_NOMBRE", tramiteNombre)
                    putExtra("TRAMITE_DESCRIPCION", tramiteDescripcion)
                    putExtra("TRAMITE_REQUISITOS", tramiteRequisitos)
                    putExtra("TRAMITE_PRECIO", tramitePrecio)
                    putExtra("FECHA", fechaSeleccionada)
                    putExtra("HORARIO", horarioSeleccionado)
                }
                startActivity(intent)
            }
        }
    }
}
