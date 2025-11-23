package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.adapters.AdaptadorCitas
import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MisCitasActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion
    private lateinit var citasRepositorio: CitasRepositorio
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: AdaptadorCitas
    private lateinit var tabLayout: TabLayout
    private lateinit var layoutSinCitas: View

    private var todasLasCitas: List<CitaResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas)

        gestorSesion = GestorSesion(this)
        citasRepositorio = CitasRepositorio()

        if (!gestorSesion.estaAutenticado()) {
            redirigirALogin()
            return
        }

        inicializarVistas()
        configurarRecyclerView()
        configurarTabs()
    }

    override fun onResume() {
        super.onResume()
        // Cargar (o recargar) las citas cada vez que la pantalla se muestra
        cargarMisCitas()
    }

    private fun inicializarVistas() {
        recyclerView = findViewById(R.id.recyclerViewCitas)
        tabLayout = findViewById(R.id.tabLayout)
        layoutSinCitas = findViewById(R.id.layoutSinCitas)
    }

    private fun configurarRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorCitas(
            citas = emptyList(),
            onVerDetallesClick = { cita -> verDetalleMiCita(cita) },
            onCancelarClick = { cita -> mostrarDialogoCancelar(cita) }
        )
        recyclerView.adapter = adaptador
    }

    private fun verDetalleMiCita(cita: CitaResponse) {
        val intent = Intent(this, DetalleMiCitaActivity::class.java).apply {
            putExtra("CITA_ID", cita.id)
        }
        startActivity(intent)
    }

    private fun configurarTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                filtrarCitasPorTab(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun cargarMisCitas() {
        val usuarioId = gestorSesion.obtenerUsuarioId()

        if (usuarioId == null) {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val resultado = citasRepositorio.obtenerCitasUsuario(usuarioId.toInt())
                resultado.onSuccess { citas ->
                    todasLasCitas = citas.sortedByDescending { it.fecha }
                    if (citas.isEmpty()) {
                        mostrarMensajeSinCitas()
                    } else {
                        filtrarCitasPorTab(tabLayout.selectedTabPosition)
                    }
                }
                resultado.onFailure { error ->
                    Toast.makeText(this@MisCitasActivity, "Error al cargar citas: ${error.message}", Toast.LENGTH_LONG).show()
                    mostrarMensajeSinCitas()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MisCitasActivity, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                mostrarMensajeSinCitas()
            }
        }
    }

    private fun filtrarCitasPorTab(tabPosition: Int) {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val citasFiltradas = when (tabPosition) {
            0 -> todasLasCitas.filter { it.fecha >= fechaHoy && it.estado !in listOf("CANCELADO", "FINALIZADO") }
            1 -> todasLasCitas.filter { it.fecha < fechaHoy || it.estado in listOf("CANCELADO", "FINALIZADO") }
            else -> todasLasCitas
        }

        if (citasFiltradas.isEmpty()) {
            mostrarMensajeSinCitas()
        } else {
            layoutSinCitas.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adaptador.actualizarCitas(citasFiltradas)
        }
    }

    private fun mostrarMensajeSinCitas() {
        layoutSinCitas.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun mostrarDialogoCancelar(cita: CitaResponse) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Estás seguro de que deseas cancelar la cita de \"${cita.tramiteNombre}\" programada para el ${cita.fecha} a las ${cita.hora}?")
            .setPositiveButton("Sí, cancelar") { _, _ -> cancelarCita(cita) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelarCita(cita: CitaResponse) {
        lifecycleScope.launch {
            try {
                val resultado = citasRepositorio.cancelarCita(cita.id, "Cancelado por el usuario")
                resultado.onSuccess {
                    Toast.makeText(this@MisCitasActivity, "✅ Cita cancelada correctamente", Toast.LENGTH_LONG).show()
                    cargarMisCitas()
                }
                resultado.onFailure { error ->
                    Toast.makeText(this@MisCitasActivity, "❌ Error al cancelar: ${error.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MisCitasActivity, "❌ Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun redirigirALogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
