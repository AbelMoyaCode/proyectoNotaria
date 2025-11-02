package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.adapters.AdaptadorMisTramites
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import kotlinx.coroutines.launch

/**
 * HU-11: Gestión de "Mis Trámites"
 * Muestra todos los trámites del usuario con sus estados
 * Permite buscar y filtrar trámites
 * Navegación a detalle de trámite (HU-12)
 */
class MisTramitesActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion
    private lateinit var citasRepositorio: CitasRepositorio
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: AdaptadorMisTramites
    private lateinit var editTextBuscar: EditText
    private lateinit var layoutSinTramites: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_tramites)

        gestorSesion = GestorSesion(this)
        citasRepositorio = CitasRepositorio()

        // Verificar autenticación
        if (!gestorSesion.estaAutenticado()) {
            redirigirALogin()
            return
        }

        inicializarVistas()
        configurarRecyclerView()
        configurarBusqueda()
        cargarMisTramites()
    }

    private fun inicializarVistas() {
        recyclerView = findViewById(R.id.recyclerViewMisTramites)
        editTextBuscar = findViewById(R.id.editTextBuscar)
        layoutSinTramites = findViewById(R.id.layoutSinTramites)

        findViewById<ImageButton>(R.id.buttonVolver).setOnClickListener {
            finish()
        }
    }

    private fun configurarRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorMisTramites(emptyList()) { tramite ->
            // HU-12: Navegar al detalle del trámite
            abrirDetalleTramite(tramite.tramiteUsuarioId)
        }
        recyclerView.adapter = adaptador
    }

    private fun configurarBusqueda() {
        editTextBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adaptador.filtrar(s.toString())
            }
        })
    }

    private fun cargarMisTramites() {
        val usuarioId = gestorSesion.obtenerUsuarioId()

        if (usuarioId == null) {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
            return
        }

        android.util.Log.d("MisTramites", "📋 Cargando trámites del usuario: $usuarioId")

        lifecycleScope.launch {
            try {
                val resultado = citasRepositorio.obtenerCitasUsuario(usuarioId.toIntOrNull() ?: 0)

                resultado.onSuccess { tramites ->
                    android.util.Log.d("MisTramites", "✅ Trámites obtenidos: ${tramites.size}")

                    if (tramites.isEmpty()) {
                        mostrarMensajeSinTramites()
                    } else {
                        mostrarTramites(tramites)
                    }
                }

                resultado.onFailure { error ->
                    android.util.Log.e("MisTramites", "❌ Error al cargar trámites: ${error.message}")
                    Toast.makeText(
                        this@MisTramitesActivity,
                        "Error al cargar trámites: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    mostrarMensajeSinTramites()
                }

            } catch (e: Exception) {
                android.util.Log.e("MisTramites", "❌ Excepción al cargar trámites: ${e.message}", e)
                Toast.makeText(
                    this@MisTramitesActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                mostrarMensajeSinTramites()
            }
        }
    }

    private fun mostrarTramites(tramites: List<com.ampn.proyecto_notaria.api.modelos.CitaResponse>) {
        layoutSinTramites.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        adaptador.actualizarTramites(tramites)
    }

    private fun mostrarMensajeSinTramites() {
        layoutSinTramites.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    /**
     * HU-12: Navegar al detalle del trámite
     */
    private fun abrirDetalleTramite(tramiteUsuarioId: Int?) {
        android.util.Log.d("MisTramites", "📄 Abriendo detalle del trámite: $tramiteUsuarioId")

        // Por ahora, mostrar mensaje
        Toast.makeText(
            this,
            "Ver detalle del trámite (próximamente)",
            Toast.LENGTH_SHORT
        ).show()

        // TODO: Implementar navegación al detalle cuando esté listo
        // val intent = Intent(this, DetalleMiTramiteActivity::class.java)
        // intent.putExtra("TRAMITE_USUARIO_ID", tramiteUsuarioId)
        // startActivity(intent)
    }

    private fun redirigirALogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
