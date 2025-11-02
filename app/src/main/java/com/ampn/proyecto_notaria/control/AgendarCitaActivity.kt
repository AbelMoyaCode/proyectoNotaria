package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.adapters.AdaptadorHorarios
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity para agendar una cita notarial.
 * Permite seleccionar fecha y horario disponible.
 */
class AgendarCitaActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerViewHorarios: RecyclerView
    private lateinit var textViewFechaSeleccionada: TextView
    private lateinit var textViewHorarioSeleccionado: TextView
    private lateinit var textViewSinHorarios: TextView
    private lateinit var buttonConfirmar: Button

    private var tramiteCodigo: String? = null
    private var tramiteNombre: String? = null
    private var tramiteDescripcion: String? = null
    private var tramiteRequisitos: String? = null
    private var tramitePrecio: Double = 0.0

    private var fechaSeleccionada: String? = null
    private var horarioSeleccionado: String? = null
    private val citasRepositorio = CitasRepositorio()

    // Horarios disponibles (8:00 AM - 6:00 PM)
    private val horariosDisponibles = listOf(
        "08:00", "08:30", "09:00", "09:30",
        "10:00", "10:30", "11:00", "11:30",
        "12:00", "12:30", "13:00", "13:30",
        "14:00", "14:30", "15:00", "15:30",
        "16:00", "16:30", "17:00", "17:30", "18:00"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_agendar_cita)

            gestorSesion = GestorSesion(this)

            // Verificar autenticación
            if (!gestorSesion.estaAutenticado()) {
                Toast.makeText(this, "Debe iniciar sesión", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Recibir datos del trámite (OPCIONALES ahora)
            tramiteCodigo = intent.getStringExtra("TRAMITE_CODIGO")
            tramiteNombre = intent.getStringExtra("TRAMITE_NOMBRE")
            tramiteDescripcion = intent.getStringExtra("TRAMITE_DESCRIPCION")
            tramiteRequisitos = intent.getStringExtra("TRAMITE_REQUISITOS")
            tramitePrecio = intent.getDoubleExtra("TRAMITE_PRECIO", 0.0)

            // Si NO hay trámite seleccionado, redirigir a lista de trámites
            if (tramiteCodigo == null || tramiteNombre == null) {
                Toast.makeText(this, "Seleccione un trámite para agendar", Toast.LENGTH_SHORT).show()
                // Redirigir a la lista de trámites
                val intent = Intent(this, ListadoTramitesActivity::class.java)
                startActivity(intent)
                finish()
                return
            }

            inicializarVistas()
            configurarCalendario()
            configurarBotones()
            mostrarInformacionTramite()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun inicializarVistas() {
        try {
            calendarView = findViewById(R.id.calendarView)
            recyclerViewHorarios = findViewById(R.id.recyclerViewHorarios)
            textViewFechaSeleccionada = findViewById(R.id.textViewFechaSeleccionada)
            textViewHorarioSeleccionado = findViewById(R.id.textViewHorarioSeleccionado)
            textViewSinHorarios = findViewById(R.id.textViewSinHorarios)
            buttonConfirmar = findViewById(R.id.buttonConfirmar)

            val buttonVolver = findViewById<ImageButton>(R.id.buttonVolver)
            val buttonCancelar = findViewById<Button>(R.id.buttonCancelar)

            // NO cerrar la app, solo volver a la pantalla anterior
            buttonVolver?.setOnClickListener { finish() }
            buttonCancelar?.setOnClickListener { finish() }

            // Configurar RecyclerView de horarios
            recyclerViewHorarios?.layoutManager = GridLayoutManager(this, 3)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun mostrarInformacionTramite() {
        try {
            findViewById<TextView>(R.id.textViewNombreTramite)?.text = tramiteNombre

            val formatoPrecio = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
            findViewById<TextView>(R.id.textViewPrecioTramite)?.text = formatoPrecio.format(tramitePrecio)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun configurarCalendario() {
        try {
            // Establecer fecha mínima (mañana)
            val calendario = Calendar.getInstance()
            calendario.add(Calendar.DAY_OF_MONTH, 1)
            calendarView?.minDate = calendario.timeInMillis

            // Establecer fecha máxima (2 meses adelante)
            calendario.add(Calendar.MONTH, 2)
            calendarView?.maxDate = calendario.timeInMillis

            // Listener de cambio de fecha
            calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
                val fecha = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                fechaSeleccionada = fecha

                // Formatear fecha para mostrar
                val formatoMostrar = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)

                textViewFechaSeleccionada?.text = "Fecha: ${formatoMostrar.format(cal.time)}"
                textViewFechaSeleccionada?.visibility = View.VISIBLE

                // Cargar horarios disponibles
                cargarHorarios()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar calendario: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * Carga los horarios disponibles para la fecha seleccionada
     * VALIDACIÓN: Muestra solo horarios del rango 8:00 - 18:00
     */
    private fun cargarHorarios() {
        try {
            android.util.Log.d("AgendarCita", "✅ VALIDACIÓN: Cargando horarios disponibles para fecha: $fechaSeleccionada")

            // Ocultar mensaje de "sin horarios" y mostrar RecyclerView
            textViewSinHorarios?.visibility = View.GONE
            recyclerViewHorarios?.visibility = View.VISIBLE

            android.util.Log.d("AgendarCita", "📋 Mostrando ${horariosDisponibles.size} horarios disponibles (8:00 - 18:00)")

            val adapter = AdaptadorHorarios(horariosDisponibles) { horario ->
                android.util.Log.d("AgendarCita", "✓ Usuario seleccionó horario: $horario para fecha: $fechaSeleccionada")
                horarioSeleccionado = horario
                textViewHorarioSeleccionado?.text = "Horario: $horario"
                textViewHorarioSeleccionado?.visibility = View.VISIBLE
                buttonConfirmar?.isEnabled = true

                // Mostrar Toast al seleccionar horario
                Toast.makeText(
                    this,
                    "✓ Horario seleccionado: $horario",
                    Toast.LENGTH_SHORT
                ).show()
            }

            recyclerViewHorarios?.adapter = adapter
            android.util.Log.d("AgendarCita", "✅ Adaptador de horarios configurado correctamente")

        } catch (e: Exception) {
            android.util.Log.e("AgendarCita", "❌ Error al cargar horarios: ${e.message}", e)
            Toast.makeText(this, "Error al cargar horarios: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            // Si falla el adaptador, mostrar mensaje
            textViewSinHorarios?.visibility = View.VISIBLE
            textViewSinHorarios?.text = "No se pudieron cargar los horarios"
            recyclerViewHorarios?.visibility = View.GONE
        }
    }

    private fun configurarBotones() {
        buttonConfirmar.isEnabled = false

        buttonConfirmar.setOnClickListener {
            if (fechaSeleccionada == null || horarioSeleccionado == null) {
                Toast.makeText(this, "⚠️ Debe seleccionar fecha y horario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            android.util.Log.d("AgendarCita", "🔄 Iniciando proceso de agendamiento...")
            android.util.Log.d("AgendarCita", "   Fecha: $fechaSeleccionada")
            android.util.Log.d("AgendarCita", "   Hora: $horarioSeleccionado")
            android.util.Log.d("AgendarCita", "   Trámite: $tramiteNombre")

            // Guardar la cita directamente en la base de datos
            agendarCitaEnBaseDatos()
        }
    }

    /**
     * Guarda la cita en la base de datos usando la API
     * IMPLEMENTACIÓN: Agendamiento con selección de fecha/hora
     */
    private fun agendarCitaEnBaseDatos() {
        if (fechaSeleccionada == null || horarioSeleccionado == null || tramiteCodigo == null) {
            Toast.makeText(this, "❌ Faltan datos para agendar la cita", Toast.LENGTH_SHORT).show()
            return
        }

        android.util.Log.d("AgendarCita", "🔍 VALIDACIÓN: Verificando disponibilidad antes de agendar...")

        // Validar que no se agenden más de una cita por día
        validarDisponibilidadYAgendar()
    }

    /**
     * VALIDACIÓN DE DISPONIBILIDAD:
     * Valida que el usuario no tenga otra cita el mismo día antes de agendar
     * Esta es una validación de negocio: 1 cita por día máximo
     */
    private fun validarDisponibilidadYAgendar() {
        // Deshabilitar botón mientras se procesa
        buttonConfirmar.isEnabled = false
        buttonConfirmar.text = "Validando..."

        android.util.Log.d("AgendarCita", "🔎 Consultando citas existentes del usuario...")

        lifecycleScope.launch {
            try {
                val usuarioId = gestorSesion.obtenerUsuarioId()
                if (usuarioId == null) {
                    Toast.makeText(
                        this@AgendarCitaActivity,
                        "❌ No se pudo identificar al usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                    buttonConfirmar.isEnabled = true
                    buttonConfirmar.text = "Confirmar Cita"
                    return@launch
                }

                android.util.Log.d("AgendarCita", "👤 Usuario ID: $usuarioId")

                // Obtener todas las citas del usuario
                val resultadoCitas = citasRepositorio.obtenerCitasUsuario(usuarioId.toInt())

                resultadoCitas.onSuccess { citas ->
                    android.util.Log.d("AgendarCita", "📊 Usuario tiene ${citas.size} citas registradas")

                    // Verificar si ya tiene una cita para la fecha seleccionada
                    val tieneCitaEnFecha = citas.any { cita ->
                        cita.fecha == fechaSeleccionada &&
                        cita.estado in listOf("AGENDADO", "EN_PROCESO")
                    }

                    if (tieneCitaEnFecha) {
                        android.util.Log.w("AgendarCita", "⚠️ VALIDACIÓN FALLIDA: Usuario ya tiene cita para $fechaSeleccionada")

                        Toast.makeText(
                            this@AgendarCitaActivity,
                            "⚠️ Ya tiene una cita agendada para esta fecha.\n📅 Solo se permite una cita por día.",
                            Toast.LENGTH_LONG
                        ).show()
                        buttonConfirmar.isEnabled = true
                        buttonConfirmar.text = "Confirmar Cita"
                        return@launch
                    }

                    android.util.Log.d("AgendarCita", "✅ VALIDACIÓN OK: No hay conflictos de fecha")

                    // Si no tiene cita, proceder a crear la nueva
                    crearNuevaCita(usuarioId.toInt())
                }

                resultadoCitas.onFailure { error ->
                    // Si falla la validación, permitir crear la cita de todos modos
                    android.util.Log.w("AgendarCita", "⚠️ No se pudo validar citas existentes: ${error.message}")
                    val usuarioId = gestorSesion.obtenerUsuarioId()?.toInt()
                    if (usuarioId != null) {
                        crearNuevaCita(usuarioId)
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("AgendarCita", "❌ Error al validar disponibilidad: ${e.message}", e)

                Toast.makeText(
                    this@AgendarCitaActivity,
                    "❌ Error al validar disponibilidad: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                buttonConfirmar.isEnabled = true
                buttonConfirmar.text = "Confirmar Cita"
            }
        }
    }

    /**
     * PRUEBA DE RESERVA:
     * Crea una nueva cita en la base de datos
     * Valida: fecha, hora, usuario, trámite
     */
    private suspend fun crearNuevaCita(usuarioId: Int) {
        buttonConfirmar.text = "Guardando..."

        android.util.Log.d("AgendarCita", "💾 Creando cita en la base de datos...")
        android.util.Log.d("AgendarCita", "   Usuario: $usuarioId")
        android.util.Log.d("AgendarCita", "   Trámite: $tramiteCodigo - $tramiteNombre")
        android.util.Log.d("AgendarCita", "   Fecha: $fechaSeleccionada")
        android.util.Log.d("AgendarCita", "   Hora: $horarioSeleccionado")

        try {
            val resultado = citasRepositorio.crearCita(
                usuarioId = usuarioId,
                tramiteCodigo = tramiteCodigo!!,
                fecha = fechaSeleccionada!!,
                hora = horarioSeleccionado!!
            )

            resultado.onSuccess { citaResponse ->
                android.util.Log.d("AgendarCita", "✅ CITA CREADA EXITOSAMENTE")
                android.util.Log.d("AgendarCita", "   ID Cita: ${citaResponse.id}")
                android.util.Log.d("AgendarCita", "   Estado: ${citaResponse.estado}")

                // Formatear fecha y hora para el mensaje
                val formatoFecha = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
                val cal = Calendar.getInstance()
                val fechaParts = fechaSeleccionada!!.split("-")
                cal.set(fechaParts[0].toInt(), fechaParts[1].toInt() - 1, fechaParts[2].toInt())
                val fechaFormateada = formatoFecha.format(cal.time)

                Toast.makeText(
                    this@AgendarCitaActivity,
                    "✅ Cita Registrada Exitosamente\n📅 Fecha: $fechaFormateada\n🕐 Horario: $horarioSeleccionado",
                    Toast.LENGTH_LONG
                ).show()

                android.util.Log.d("AgendarCita", "📲 Navegando a confirmación de cita...")

                // Navegar a la confirmación
                val intent = Intent(this@AgendarCitaActivity, ConfirmacionCitaActivity::class.java)
                intent.putExtra("TRAMITE_NOMBRE", tramiteNombre)
                intent.putExtra("FECHA", fechaSeleccionada)
                intent.putExtra("HORA", horarioSeleccionado)
                intent.putExtra("PRECIO", tramitePrecio)
                startActivity(intent)
                finish()
            }

            resultado.onFailure { error ->
                android.util.Log.e("AgendarCita", "❌ ERROR AL CREAR CITA: ${error.message}")

                Toast.makeText(
                    this@AgendarCitaActivity,
                    "❌ Error al crear cita: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                buttonConfirmar.isEnabled = true
                buttonConfirmar.text = "Confirmar Cita"
            }

        } catch (e: Exception) {
            android.util.Log.e("AgendarCita", "❌ EXCEPCIÓN al crear cita: ${e.message}", e)

            Toast.makeText(
                this@AgendarCitaActivity,
                "❌ Error inesperado: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            buttonConfirmar.isEnabled = true
            buttonConfirmar.text = "Confirmar Cita"
        }
    }

    // Manejar el botón atrás del sistema
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // Solo cierra esta pantalla, no toda la app
    }
}
