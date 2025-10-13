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
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.repositorios.CitaRepositorio
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
    private val citaRepositorio = CitaRepositorio()
    private val TAG = "HomeFragment"

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

        // Cargar próxima cita
        cargarProximaCita(view)

        // Configurar botones de acceso rápido
        configurarAccesosRapidos(view)
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

                resultado.onSuccess { citas ->
                    if (citas.isEmpty()) {
                        contenedorCita?.visibility = View.GONE
                        Log.d(TAG, "No hay citas agendadas para el usuario")
                        return@onSuccess
                    }

                    // Buscar próxima cita agendada
                    val citaProxima = citas
                        .filter { it.estado.equals("AGENDADO", ignoreCase = true) }
                        .minByOrNull { it.creadaEn ?: "" }

                    if (citaProxima != null && textoCita != null) {
                        contenedorCita?.visibility = View.VISIBLE
                        textoCita.text = "Cita agendada para el ${formatearFecha(citaProxima.creadaEn ?: "")}"

                        btnVerDetalles?.setOnClickListener {
                            // Navegar a detalle de cita
                            Toast.makeText(requireContext(), "Ver detalles de cita", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        contenedorCita?.visibility = View.GONE
                        Log.d(TAG, "No se encontró cita próxima agendada")
                    }
                }

                resultado.onFailure { error ->
                    Log.d(TAG, "No se pudieron cargar citas (puede ser normal si no hay citas): ${error.message}")
                    contenedorCita?.visibility = View.GONE
                    // No mostrar error al usuario si simplemente no hay citas
                }
            } catch (e: Exception) {
                Log.d(TAG, "Excepción al cargar próxima cita (puede ser normal): ${e.message}")
                e.printStackTrace()
                contenedorCita?.visibility = View.GONE
                // No mostrar error al usuario
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
            Toast.makeText(requireContext(), "Mis citas - Próximamente", Toast.LENGTH_SHORT).show()
        }

        // Botón Perfil
        view.findViewById<View>(R.id.btnPerfil).setOnClickListener {
            Toast.makeText(requireContext(), "Perfil - Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = formatoEntrada.parse(fecha)
            formatoSalida.format(date ?: Date())
        } catch (e: Exception) {
            fecha
        }
    }
}
