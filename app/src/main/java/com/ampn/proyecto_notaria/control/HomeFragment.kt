package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.adapters.AdaptadorNotificaciones
import com.ampn.proyecto_notaria.api.modelos.Notificacion
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment principal (Home) después del login
 * Muestra: próxima cita, accesos rápidos y notificaciones recientes
 */
class HomeFragment : Fragment() {

    private lateinit var gestorSesion: GestorSesion
    private val citaRepositorio = CitasRepositorio()
    private val TAG = "HomeFragment"

    // Notificaciones
    private lateinit var recyclerNotificaciones: RecyclerView
    private lateinit var contenedorNotificaciones: View
    private lateinit var adaptadorNotificaciones: AdaptadorNotificaciones

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gestorSesion = GestorSesion(requireContext())

        // Configurar saludo
        val textoBienvenida = view.findViewById<TextView>(R.id.textViewBienvenida)
        val usuario = gestorSesion.obtenerUsuario()
        textoBienvenida.text = "Bienvenido, ${usuario?.nombres ?: "Usuario"}"

        // Configurar botones de acceso rápido
        configurarAccesosRapidos(view)

        // Configurar notificaciones
        configurarNotificaciones(view)
    }

    override fun onResume() {
        super.onResume()
        // ACTUALIZAR automáticamente al volver a la pantalla
        view?.let { v ->
            cargarProximaCita(v)
            cargarNotificaciones()
        }
    }

    private fun cargarProximaCita(view: View) {
        val contenedorCita = view.findViewById<View>(R.id.contenedorProximaCita)
        val textoCita = view.findViewById<TextView>(R.id.textViewProximaCita)
        val btnVerDetalles = view.findViewById<Button>(R.id.btnVerDetallesCita)

        val usuarioId = gestorSesion.obtenerUsuario()?.id

        if (usuarioId == null) {
            contenedorCita?.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            try {
                val resultado = citaRepositorio.obtenerCitasUsuario(usuarioId)

                resultado.onSuccess { todasLasCitas ->
                    // Obtener SOLO citas futuras y activas (no canceladas ni finalizadas)
                    val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    val citasActivas = todasLasCitas.filter { cita ->
                        cita.estado.uppercase() in listOf("AGENDADO", "CONFIRMADO", "PENDIENTE") &&
                        cita.fecha >= fechaHoy
                    }

                    if (citasActivas.isEmpty()) {
                        // NO hay citas próximas - Ocultar completamente la sección
                        contenedorCita?.visibility = View.GONE
                        Log.d(TAG, "❌ No hay citas activas para el usuario")
                        return@onSuccess
                    }

                    // Buscar la cita MÁS PRÓXIMA (ordenar por fecha y tomar la primera)
                    val citaProxima = citasActivas
                        .sortedBy { it.fecha } // Ordenar por fecha ascendente
                        .firstOrNull()

                    if (citaProxima != null && textoCita != null) {
                        contenedorCita?.visibility = View.VISIBLE

                        // Formatear la fecha y hora de la cita
                        val fechaFormateada = "📅 ${formatearFecha(citaProxima.fecha)} a las ${citaProxima.hora}"

                        textoCita.text = fechaFormateada

                        Log.d(TAG, "✅ Próxima cita: ${citaProxima.tramiteNombre} - ${citaProxima.fecha} ${citaProxima.hora}")

                        // Configurar botón Ver Detalles para navegar a MisCitasActivity
                        btnVerDetalles?.setOnClickListener {
                            Log.d(TAG, "📄 Navegando a detalles de cita ID: ${citaProxima.id}")
                            val intent = Intent(requireContext(), MisCitasActivity::class.java)
                            intent.putExtra("CITA_ID", citaProxima.id)
                            startActivity(intent)
                        }
                    } else {
                        contenedorCita?.visibility = View.GONE
                        Log.d(TAG, "❌ No se pudo obtener la próxima cita")
                    }
                }

                resultado.onFailure { error ->
                    Log.e(TAG, "❌ Error al cargar citas: ${error.message}")
                    contenedorCita?.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al cargar próxima cita: ${e.message}", e)
                contenedorCita?.visibility = View.GONE
            }
        }
    }

    private fun configurarAccesosRapidos(view: View) {
        // Botón Ver Trámites
        view.findViewById<View>(R.id.btnVerTramites).setOnClickListener {
            startActivity(Intent(requireContext(), ListadoTramitesActivity::class.java))
        }

        // Botón Mis Citas
        view.findViewById<View>(R.id.btnMisCitas).setOnClickListener {
            startActivity(Intent(requireContext(), MisCitasActivity::class.java))
        }

        // Botón Perfil - HU-04: Navegar a la pantalla de perfil
        view.findViewById<View>(R.id.btnPerfil).setOnClickListener {
            startActivity(Intent(requireContext(), PerfilActivity::class.java))
        }
    }

    private fun configurarNotificaciones(view: View) {
        contenedorNotificaciones = view.findViewById(R.id.contenedorNotificaciones)
        recyclerNotificaciones = view.findViewById(R.id.recyclerViewNotificaciones)

        recyclerNotificaciones.layoutManager = LinearLayoutManager(requireContext())
        adaptadorNotificaciones = AdaptadorNotificaciones(emptyList()) { notificacion ->
            // Al hacer clic en una notificación
            if (notificacion.citaId != null) {
                val intent = Intent(requireContext(), MisCitasActivity::class.java)
                intent.putExtra("CITA_ID", notificacion.citaId)
                startActivity(intent)
            }
        }
        recyclerNotificaciones.adapter = adaptadorNotificaciones
    }

    private fun cargarNotificaciones() {
        lifecycleScope.launch {
            try {
                val usuarioId = gestorSesion.obtenerUsuario()?.id ?: return@launch

                // Obtener citas del usuario para generar notificaciones
                val resultado = citaRepositorio.obtenerCitasUsuario(usuarioId)

                resultado.onSuccess { citas ->
                    val notificaciones = generarNotificacionesDesdeCitas(citas)

                    if (notificaciones.isNotEmpty()) {
                        contenedorNotificaciones.visibility = View.VISIBLE
                        adaptadorNotificaciones.actualizarNotificaciones(notificaciones)
                        Log.d(TAG, "✅ Notificaciones cargadas: ${notificaciones.size}")
                    } else {
                        contenedorNotificaciones.visibility = View.GONE
                        Log.d(TAG, "No hay notificaciones para mostrar")
                    }
                }

                resultado.onFailure {
                    contenedorNotificaciones.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar notificaciones: ${e.message}")
                contenedorNotificaciones.visibility = View.GONE
            }
        }
    }

    /**
     * Genera notificaciones basadas en las citas del usuario
     * NO incluye citas canceladas - solo se muestra Toast al cancelar
     */
    private fun generarNotificacionesDesdeCitas(citas: List<com.ampn.proyecto_notaria.api.modelos.CitaResponse>): List<Notificacion> {
        val notificaciones = mutableListOf<Notificacion>()
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        citas.take(5).forEachIndexed { index, cita ->
            when (cita.estado.uppercase()) {
                "AGENDADO" -> {
                    // Notificación de confirmación
                    notificaciones.add(
                        Notificacion(
                            id = index + 1,
                            tipo = Notificacion.TIPO_CONFIRMACION,
                            titulo = "Confirmación de cita",
                            mensaje = "Tu cita para ${cita.tramiteNombre} ha sido confirmada",
                            fecha = cita.creadaEn ?: fechaActual,
                            leida = false,
                            citaId = cita.id
                        )
                    )
                }
                "CONFIRMADO" -> {
                    notificaciones.add(
                        Notificacion(
                            id = index + 1,
                            tipo = Notificacion.TIPO_CONFIRMACION,
                            titulo = "Cita confirmada",
                            mensaje = "Tu cita para ${cita.tramiteNombre} está confirmada",
                            fecha = cita.creadaEn ?: fechaActual,
                            leida = false,
                            citaId = cita.id
                        )
                    )
                }
                "REPROGRAMADO" -> {
                    notificaciones.add(
                        Notificacion(
                            id = index + 1,
                            tipo = Notificacion.TIPO_REPROGRAMACION,
                            titulo = "Cita reprogramada",
                            mensaje = "Tu cita para ${cita.tramiteNombre} ha sido reprogramada",
                            fecha = cita.creadaEn ?: fechaActual,
                            leida = false,
                            citaId = cita.id
                        )
                    )
                }
                // CANCELADO ya NO genera notificación - solo Toast al momento de cancelar
            }
        }

        return notificaciones.sortedByDescending { it.fecha }.take(3) // Mostrar solo las 3 más recientes
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = formatoEntrada.parse(fecha)
            formatoSalida.format(date ?: Date())
        } catch (e: Exception) {
            fecha
        }
    }
}
