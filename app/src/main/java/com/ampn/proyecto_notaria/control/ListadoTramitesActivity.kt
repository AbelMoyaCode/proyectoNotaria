package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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

class ListadoTramitesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tramiteAdapter: AdaptadorTramites
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewSinResultados: TextView
    private lateinit var editTextBuscar: EditText

    private val repositorio = TramitesRepositorio()
    private val tramiteFilter = TramiteFilter() // Instancia de la nueva clase
    private var todosLosTramites = listOf<Tramite>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado_tramites)

        try {
            recyclerView = findViewById(R.id.recyclerViewTramites)
            progressBar = findViewById(R.id.progressBar)
            textViewSinResultados = findViewById(R.id.textViewSinResultados)
            editTextBuscar = findViewById(R.id.editTextBuscar)

            recyclerView.layoutManager = LinearLayoutManager(this)
            tramiteAdapter = AdaptadorTramites(emptyList()) { tramite ->
                navegarADetalle(tramite)
            }
            recyclerView.adapter = tramiteAdapter

            configurarBusqueda()
            cargarTramites()

            if (intent.getBooleanExtra("CITA_CREADA", false)) {
                val tramiteNombre = intent.getStringExtra("TRAMITE_NOMBRE") ?: "su trámite"
                Toast.makeText(this, "Cita del trámite \"$tramiteNombre\" agendada exitosamente", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la pantalla: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

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
                    Toast.makeText(this@ListadoTramitesActivity, "Error al cargar trámites: ${error.message}", Toast.LENGTH_LONG).show()
                    mostrarSinResultados()
                }
            } catch (e: Exception) {
                mostrarCargando(false)
                Toast.makeText(this@ListadoTramitesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarTramites(tramites: List<Tramite>) {
        if (tramites.isEmpty()) {
            mostrarSinResultados()
        } else {
            recyclerView.visibility = View.VISIBLE
            textViewSinResultados.visibility = View.GONE
            tramiteAdapter.actualizarTramites(tramites)
        }
    }

    private fun configurarBusqueda() {
        editTextBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Ahora la lógica de filtrado se delega a la nueva clase
                val tramitesFiltrados = tramiteFilter.filter(todosLosTramites, s.toString())
                mostrarTramites(tramitesFiltrados)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun navegarADetalle(tramite: Tramite) {
        val intent = Intent(this, DetalleTramiteActivity::class.java).apply {
            putExtra("TRAMITE_CODIGO", tramite.codigo)
            putExtra("TRAMITE_NOMBRE", tramite.nombre)
            putExtra("TRAMITE_DESCRIPCION", tramite.descripcion)
            putExtra("TRAMITE_REQUISITOS", tramite.requisitos)
            putExtra("TRAMITE_PRECIO", tramite.precio)
            putExtra("TRAMITE_DURACION", tramite.duracionEstimada)
            putExtra("TRAMITE_CATEGORIA", tramite.categoria)
        }
        startActivity(intent)
    }

    private fun mostrarCargando(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        recyclerView.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    private fun mostrarSinResultados() {
        recyclerView.visibility = View.GONE
        textViewSinResultados.visibility = View.VISIBLE
    }
}
