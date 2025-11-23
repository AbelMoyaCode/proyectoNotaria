package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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

class HomeFragment : Fragment() {

    private lateinit var gestorSesion: GestorSesion
    private val citaRepositorio = CitasRepositorio()
    private val notificacionGenerator = NotificacionGenerator() // Instancia del nuevo generador
    private val TAG = "HomeFragment"

    // Vistas de Notificaciones
    private lateinit var recyclerNotificaciones: RecyclerView
    private lateinit var contenedorNotificaciones: View
    private lateinit var textViewSinNotificaciones: TextView
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

        val textoBienvenida = view.findViewById<TextView>(R.id.textViewBienvenida)
        val usuario = gestorSesion.obtenerUsuario()
        textoBienvenida.text = "Bienvenido, ${usuario?.nombres ?: "Usuario"}"

        configurarAccesosRapidos(view)
        configurarNotificaciones(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { v ->
            cargarProximaCita(v)
            cargarNotificaciones()
        }
    }

    private fun cargarProximaCita(view: View) {
        val contenedorCita = view.findViewById<View>(R.id.contenedorProximaCita)
        val textoCita = view.findViewById<TextView>(R.id.textViewProximaCita)
        val btnVerDetalles = view.findViewById<Button>(R.id.btnVerDetallesCita)

        val usuarioId = gestorSesion.obtenerUsuarioId()?.toIntOrNull()
        if (usuarioId == null) {
            contenedorCita.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            try {
                val resultado = citaRepositorio.obtenerCitasUsuario(usuarioId)
                resultado.onSuccess { todasLasCitas ->
                    val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val citasActivas = todasLasCitas.filter {
                        it.estado.uppercase() in listOf("AGENDADO", "CONFIRMADO", "PENDIENTE") && it.fecha >= fechaHoy
                    }
                    val citaProxima = citasActivas.sortedBy { it.fecha }.firstOrNull()

                    if (citaProxima != null) {
                        contenedorCita.visibility = View.VISIBLE
                        val fechaFormateada = "📅 ${formatearFecha(citaProxima.fecha)} a las ${citaProxima.hora}"
                        textoCita.text = fechaFormateada
                        btnVerDetalles.setOnClickListener {
                            val intent = Intent(requireContext(), DetalleMiCitaActivity::class.java)
                            intent.putExtra("CITA_ID", citaProxima.id)
                            startActivity(intent)
                        }
                    } else {
                        contenedorCita.visibility = View.GONE
                    }
                }
                resultado.onFailure {
                    contenedorCita.visibility = View.GONE
                }
            } catch (e: Exception) {
                contenedorCita.visibility = View.GONE
            }
        }
    }

    private fun configurarAccesosRapidos(view: View) {
        view.findViewById<View>(R.id.btnVerTramites).setOnClickListener {
            startActivity(Intent(requireContext(), ListadoTramitesActivity::class.java))
        }
        view.findViewById<View>(R.id.btnMisCitas).setOnClickListener {
            startActivity(Intent(requireContext(), MisCitasActivity::class.java))
        }
        view.findViewById<View>(R.id.btnPerfil).setOnClickListener {
            startActivity(Intent(requireContext(), PerfilActivity::class.java))
        }
    }

    private fun configurarNotificaciones(view: View) {
        contenedorNotificaciones = view.findViewById(R.id.contenedorNotificaciones)
        recyclerNotificaciones = view.findViewById(R.id.recyclerViewNotificaciones)
        textViewSinNotificaciones = view.findViewById(R.id.textViewSinNotificaciones)

        recyclerNotificaciones.layoutManager = LinearLayoutManager(requireContext())
        adaptadorNotificaciones = AdaptadorNotificaciones(emptyList()) { notificacion ->
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
                val usuarioId = gestorSesion.obtenerUsuarioId()?.toIntOrNull() ?: return@launch
                val resultado = citaRepositorio.obtenerCitasUsuario(usuarioId)

                resultado.onSuccess { citas ->
                    // Ahora la lógica se delega a la nueva clase
                    val notificaciones = notificacionGenerator.generar(citas)
                    contenedorNotificaciones.visibility = View.VISIBLE

                    if (notificaciones.isNotEmpty()) {
                        recyclerNotificaciones.visibility = View.VISIBLE
                        textViewSinNotificaciones.visibility = View.GONE
                        adaptadorNotificaciones.actualizarNotificaciones(notificaciones)
                        Log.d(TAG, "✅ Notificaciones cargadas: ${notificaciones.size}")
                    } else {
                        recyclerNotificaciones.visibility = View.GONE
                        textViewSinNotificaciones.visibility = View.VISIBLE
                        Log.d(TAG, "No hay notificaciones para mostrar")
                    }
                }

                resultado.onFailure {
                    contenedorNotificaciones.visibility = View.GONE
                    Log.e(TAG, "Error al cargar citas para notificaciones: ${it.message}")
                }
            } catch (e: Exception) {
                contenedorNotificaciones.visibility = View.GONE
                Log.e(TAG, "Error al cargar notificaciones: ${e.message}")
            }
        }
    }

    // La función generarNotificacionesDesdeCitas() ha sido eliminada de aquí

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
