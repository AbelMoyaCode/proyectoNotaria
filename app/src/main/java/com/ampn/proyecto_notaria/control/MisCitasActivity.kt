package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
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

/**
 * HU-10: Seguimiento y Cancelación de Cita
 * Muestra las citas del usuario (próximas y pasadas)
 * Permite reprogramar y cancelar citas
 * Permite eliminar citas pasadas
 * Permite vaciar todas las citas
 *
 * PRUEBAS:
 * - Visualizar citas ordenadas por fecha (más reciente primero)
 * - Filtrar citas por estado (próximas/pasadas)
 * - Cancelar citas individualmente
 * - Eliminar citas pasadas
 * - Vaciar todas las citas
 */
class MisCitasActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion
    private lateinit var citasRepositorio: CitasRepositorio
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: AdaptadorCitas
    private lateinit var tabLayout: TabLayout
    private lateinit var layoutSinCitas: View
    private lateinit var buttonOpciones: ImageButton

    private var todasLasCitas: List<CitaResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas)

        gestorSesion = GestorSesion(this)
        citasRepositorio = CitasRepositorio()

        // Verificar autenticación
        if (!gestorSesion.estaAutenticado()) {
            redirigirALogin()
            return
        }

        inicializarVistas()
        configurarRecyclerView()
        configurarTabs()
        cargarMisCitas()

        // PRUEBA HU-10: Inicio de actividad
        android.util.Log.d("PRUEBA_HU10", "✅ MisCitasActivity iniciada correctamente")
    }

    private fun inicializarVistas() {
        recyclerView = findViewById(R.id.recyclerViewCitas)
        tabLayout = findViewById(R.id.tabLayout)
        layoutSinCitas = findViewById(R.id.layoutSinCitas)
        buttonOpciones = findViewById(R.id.buttonOpciones)

        findViewById<ImageButton>(R.id.buttonVolver).setOnClickListener {
            finish()
        }

        // Botón de opciones (menú de acciones)
        buttonOpciones.setOnClickListener {
            mostrarMenuOpciones()
        }
    }

    private fun configurarRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorCitas(
            citas = emptyList(),
            onReprogramarClick = { cita -> reprogramarCita(cita) },
            onCancelarClick = { cita -> mostrarDialogoCancelar(cita) },
            onEliminarClick = { cita -> mostrarDialogoEliminar(cita) } // ✅ NUEVO
        )
        recyclerView.adapter = adaptador
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

        android.util.Log.d("PRUEBA_HU10", "📅 Cargando citas del usuario: $usuarioId")

        lifecycleScope.launch {
            try {
                val resultado = citasRepositorio.obtenerCitasUsuario(usuarioId.toInt())

                resultado.onSuccess { citas ->
                    // ORDENAR POR FECHA (MÁS RECIENTE PRIMERO)
                    todasLasCitas = citas.sortedByDescending { it.fecha }

                    android.util.Log.d("PRUEBA_HU10", "✅ Citas obtenidas y ordenadas: ${citas.size}")

                    if (citas.isEmpty()) {
                        mostrarMensajeSinCitas()
                    } else {
                        // Mostrar citas próximas por defecto
                        filtrarCitasPorTab(0)
                    }
                }

                resultado.onFailure { error ->
                    android.util.Log.e("PRUEBA_HU10", "❌ Error al cargar citas: ${error.message}")
                    Toast.makeText(
                        this@MisCitasActivity,
                        "Error al cargar citas: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    mostrarMensajeSinCitas()
                }

            } catch (e: Exception) {
                android.util.Log.e("PRUEBA_HU10", "❌ Excepción al cargar citas: ${e.message}", e)
                Toast.makeText(
                    this@MisCitasActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                mostrarMensajeSinCitas()
            }
        }
    }

    private fun filtrarCitasPorTab(tabPosition: Int) {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val citasFiltradas = when (tabPosition) {
            0 -> { // Próximas (ordenadas por fecha, más reciente primero)
                todasLasCitas.filter { cita ->
                    cita.fecha >= fechaHoy && cita.estado !in listOf("CANCELADO", "FINALIZADO")
                }
            }
            1 -> { // Pasadas (ordenadas por fecha, más reciente primero)
                todasLasCitas.filter { cita ->
                    cita.fecha < fechaHoy || cita.estado in listOf("CANCELADO", "FINALIZADO")
                }
            }
            else -> todasLasCitas
        }

        android.util.Log.d("PRUEBA_HU10", "🔍 Filtro aplicado. Tab: $tabPosition, Citas mostradas: ${citasFiltradas.size}")

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

    /**
     * Mostrar menú de opciones con acciones sobre las citas
     */
    private fun mostrarMenuOpciones() {
        val opciones = arrayOf(
            "🗑️ Eliminar citas pasadas",
            "⚠️ Vaciar todas las citas"
        )

        AlertDialog.Builder(this)
            .setTitle("Opciones de Citas")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> confirmarEliminarCitasPasadas()
                    1 -> confirmarVaciarTodasLasCitas()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Confirmar eliminación de citas pasadas
     */
    private fun confirmarEliminarCitasPasadas() {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val citasPasadas = todasLasCitas.filter { cita ->
            cita.fecha < fechaHoy || cita.estado in listOf("CANCELADO", "FINALIZADO")
        }

        if (citasPasadas.isEmpty()) {
            Toast.makeText(this, "No hay citas pasadas para eliminar", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar Citas Pasadas")
            .setMessage("¿Deseas eliminar ${citasPasadas.size} citas pasadas?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                eliminarCitasPasadas(citasPasadas)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Eliminar citas pasadas
     */
    private fun eliminarCitasPasadas(citasPasadas: List<CitaResponse>) {
        android.util.Log.d("PRUEBA_HU10", "🗑️ Eliminando ${citasPasadas.size} citas pasadas")

        lifecycleScope.launch {
            var exitosas = 0
            var fallidas = 0

            citasPasadas.forEach { cita ->
                try {
                    val resultado = citasRepositorio.cancelarCita(cita.id, "Eliminado automáticamente")
                    resultado.onSuccess { exitosas++ }
                    resultado.onFailure { fallidas++ }
                } catch (e: Exception) {
                    fallidas++
                }
            }

            android.util.Log.d("PRUEBA_HU10", "✅ Eliminación completada. Exitosas: $exitosas, Fallidas: $fallidas")

            Toast.makeText(
                this@MisCitasActivity,
                "✅ Eliminadas $exitosas citas pasadas",
                Toast.LENGTH_LONG
            ).show()

            // Recargar citas
            cargarMisCitas()
        }
    }

    /**
     * Confirmar vaciar todas las citas
     */
    private fun confirmarVaciarTodasLasCitas() {
        if (todasLasCitas.isEmpty()) {
            Toast.makeText(this, "No hay citas para eliminar", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("⚠️ Vaciar Todas las Citas")
            .setMessage("¿Estás SEGURO de que deseas eliminar TODAS las ${todasLasCitas.size} citas?\n\n⚠️ Esta acción NO se puede deshacer.")
            .setPositiveButton("Sí, vaciar todo") { _, _ ->
                vaciarTodasLasCitas()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Vaciar todas las citas
     */
    private fun vaciarTodasLasCitas() {
        android.util.Log.d("PRUEBA_HU10", "⚠️ VACIANDO TODAS LAS CITAS (${todasLasCitas.size})")

        lifecycleScope.launch {
            var exitosas = 0
            var fallidas = 0

            todasLasCitas.forEach { cita ->
                try {
                    val resultado = citasRepositorio.cancelarCita(cita.id, "Vaciado completo")
                    resultado.onSuccess { exitosas++ }
                    resultado.onFailure { fallidas++ }
                } catch (e: Exception) {
                    fallidas++
                }
            }

            android.util.Log.d("PRUEBA_HU10", "✅ Vaciado completado. Exitosas: $exitosas, Fallidas: $fallidas")

            Toast.makeText(
                this@MisCitasActivity,
                "✅ Eliminadas todas las citas ($exitosas)",
                Toast.LENGTH_LONG
            ).show()

            // Recargar citas
            cargarMisCitas()
        }
    }

    /**
     * Reprogramar una cita
     */
    private fun reprogramarCita(cita: CitaResponse) {
        android.util.Log.d("PRUEBA_HU10", "📅 Reprogramando cita: ${cita.id}")

        Toast.makeText(
            this,
            "Funcionalidad de reprogramación en desarrollo",
            Toast.LENGTH_SHORT
        ).show()

        // TODO: Implementar diálogo para seleccionar nueva fecha/hora
        // y llamar a citasRepositorio.reprogramarCita()
    }

    /**
     * HU-10: Mostrar diálogo de confirmación para cancelar cita
     */
    private fun mostrarDialogoCancelar(cita: CitaResponse) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Estás seguro de que deseas cancelar la cita de \"${cita.tramiteNombre}\" programada para el ${cita.fecha} a las ${cita.hora}?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                cancelarCita(cita)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * HU-10: Mostrar diálogo de confirmación para eliminar cita
     */
    private fun mostrarDialogoEliminar(cita: CitaResponse) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Cita")
            .setMessage("¿Estás seguro de que deseas eliminar la cita de \"${cita.tramiteNombre}\" programada para el ${cita.fecha} a las ${cita.hora}?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                eliminarCita(cita)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * HU-10: Eliminar una cita
     * Llama al repositorio para eliminar FÍSICAMENTE la cita del backend
     * Y actualiza la lista INMEDIATAMENTE sin esperar recarga del servidor
     */
    private fun eliminarCita(cita: CitaResponse) {
        android.util.Log.d("PRUEBA_HU10", "🗑️ Eliminando cita ID: ${cita.id}")

        lifecycleScope.launch {
            try {
                val resultado = citasRepositorio.eliminarCita(citaId = cita.id)

                resultado.onSuccess {
                    android.util.Log.d("PRUEBA_HU10", "✅ Cita ID ${cita.id} eliminada exitosamente")

                    // ✅ ELIMINAR INMEDIATAMENTE DE LA LISTA LOCAL (sin esperar recarga)
                    todasLasCitas = todasLasCitas.filter { it.id != cita.id }

                    // Actualizar la vista según el tab actual
                    val tabActual = tabLayout.selectedTabPosition
                    filtrarCitasPorTab(tabActual)

                    Toast.makeText(
                        this@MisCitasActivity,
                        "✅ Cita eliminada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    android.util.Log.d("PRUEBA_HU10", "✅ Cita eliminada de la lista local. Citas restantes: ${todasLasCitas.size}")
                }

                resultado.onFailure { error ->
                    android.util.Log.e("PRUEBA_HU10", "❌ Error al eliminar cita: ${error.message}")

                    Toast.makeText(
                        this@MisCitasActivity,
                        "❌ ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                android.util.Log.e("PRUEBA_HU10", "❌ Excepción al eliminar: ${e.message}", e)

                Toast.makeText(
                    this@MisCitasActivity,
                    "❌ Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * HU-10: Cancelar una cita
     * Llama al repositorio para cancelar la cita en el backend
     */
    private fun cancelarCita(cita: CitaResponse) {
        android.util.Log.d("PRUEBA_HU10", "🚫 Cancelando cita: ${cita.id}")

        lifecycleScope.launch {
            try {
                val resultado = citasRepositorio.cancelarCita(
                    citaId = cita.id,
                    motivo = "Cancelado por el usuario"
                )

                resultado.onSuccess {
                    android.util.Log.d("PRUEBA_HU10", "✅ Cita cancelada exitosamente")

                    Toast.makeText(
                        this@MisCitasActivity,
                        "✅ Cita cancelada correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    // Recargar citas
                    cargarMisCitas()
                }

                resultado.onFailure { error ->
                    android.util.Log.e("PRUEBA_HU10", "❌ Error al cancelar cita: ${error.message}")

                    Toast.makeText(
                        this@MisCitasActivity,
                        "❌ Error al cancelar: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                android.util.Log.e("PRUEBA_HU10", "❌ Excepción al cancelar: ${e.message}", e)

                Toast.makeText(
                    this@MisCitasActivity,
                    "❌ Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
