package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.adapters.AdaptadorTramites
import com.ampn.proyecto_notaria.api.modelos.Tramite
import com.ampn.proyecto_notaria.api.repositorios.TramitesRepositorio
import kotlinx.coroutines.launch

/**
 * Activity que muestra la lista de trámites disponibles en la notaría.
 * Permite buscar, filtrar y navegar al detalle de cada trámite.
 */
class ListadoTramitesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tramiteAdapter: AdaptadorTramites
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewSinResultados: TextView
    private lateinit var editTextBuscar: EditText

    private val repositorio = TramitesRepositorio()
    private var todosLosTramites = listOf<Tramite>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_listado_tramites)

            // Inicializar vistas con validación
            recyclerView = findViewById(R.id.recyclerViewTramites) ?: throw IllegalStateException("RecyclerView no encontrado")
            progressBar = findViewById(R.id.progressBar) ?: throw IllegalStateException("ProgressBar no encontrado")
            textViewSinResultados = findViewById(R.id.textViewSinResultados) ?: throw IllegalStateException("TextView no encontrado")
            editTextBuscar = findViewById(R.id.editTextBuscar) ?: throw IllegalStateException("EditText no encontrado")
            val buttonVolver = findViewById<ImageButton>(R.id.buttonVolver) ?: throw IllegalStateException("Botón volver no encontrado")

            // Configurar RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this)
            tramiteAdapter = AdaptadorTramites(emptyList()) { tramite ->
                navegarADetalle(tramite)
            }
            recyclerView.adapter = tramiteAdapter

            // Configurar búsqueda
            configurarBusqueda()

            // Configurar botón volver - NO cerrar la app, solo esta pantalla
            buttonVolver.setOnClickListener {
                finish() // Cierra solo esta Activity, vuelve a la anterior
            }

            // Cargar trámites
            cargarTramites()

            // Mostrar Toast si viene de una cita confirmada
            if (intent.getBooleanExtra("CITA_CREADA", false)) {
                val tramiteNombre = intent.getStringExtra("TRAMITE_NOMBRE") ?: "su trámite"
                Toast.makeText(
                    this,
                    "Cita del trámite \"$tramiteNombre\" agendada exitosamente",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la pantalla: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * Carga los trámites desde el backend usando la API REST
     */
    private fun cargarTramites() {
        mostrarCargando(true)

        lifecycleScope.launch {
            try {
                val resultado = repositorio.obtenerTramites()

                resultado.onSuccess { tramites ->
                    mostrarCargando(false)
                    todosLosTramites = tramites

                    if (tramites.isNotEmpty()) {
                        mostrarTramites(tramites)
                    } else {
                        mostrarSinResultados()
                    }
                }

                resultado.onFailure { error ->
                    mostrarCargando(false)
                    Toast.makeText(
                        this@ListadoTramitesActivity,
                        "Error al cargar trámites: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    mostrarSinResultados()
                }
            } catch (e: Exception) {
                mostrarCargando(false)
                Toast.makeText(
                    this@ListadoTramitesActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Muestra los trámites en el RecyclerView
     */
    private fun mostrarTramites(tramites: List<Tramite>) {
        if (tramites.isEmpty()) {
            mostrarSinResultados()
        } else {
            recyclerView.visibility = View.VISIBLE
            textViewSinResultados.visibility = View.GONE
            tramiteAdapter.actualizarTramites(tramites)
        }
    }

    /**
     * Configura el campo de búsqueda en tiempo real
     */
    private fun configurarBusqueda() {
        editTextBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarTramites(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Filtra los trámites según la consulta de búsqueda
     */
    private fun filtrarTramites(query: String) {
        val tramitesFiltrados = if (query.isBlank()) {
            todosLosTramites
        } else {
            todosLosTramites.filter { tramite ->
                tramite.nombre.contains(query, ignoreCase = true) ||
                tramite.descripcion.contains(query, ignoreCase = true)
            }
        }
        mostrarTramites(tramitesFiltrados)
    }

    /**
     * Navega a la pantalla de detalle del trámite seleccionado
     */
    private fun navegarADetalle(tramite: Tramite) {
        val intent = Intent(this, DetalleTramiteActivity::class.java)
        intent.putExtra("TRAMITE_CODIGO", tramite.codigo)
        intent.putExtra("TRAMITE_NOMBRE", tramite.nombre)
        intent.putExtra("TRAMITE_DESCRIPCION", tramite.descripcion)
        intent.putExtra("TRAMITE_REQUISITOS", tramite.requisitos)
        intent.putExtra("TRAMITE_PRECIO", tramite.precio)
        intent.putExtra("TRAMITE_DURACION", tramite.duracion_estimada)
        intent.putExtra("TRAMITE_CATEGORIA", tramite.categoria)
        startActivity(intent)
    }

    /**
     * Muestra u oculta el indicador de carga
     */
    private fun mostrarCargando(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        recyclerView.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    /**
     * Muestra mensaje cuando no hay trámites
     */
    private fun mostrarSinResultados() {
        recyclerView.visibility = View.GONE
        textViewSinResultados.visibility = View.VISIBLE
    }

    // Manejar el botón atrás del sistema
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // La app NO se cerrará, solo volverá a la pantalla anterior
    }
}
