package com.ampn.proyecto_notaria.control

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.utils.GestorSesion

/**
 * MainActivity - Pantalla principal de la aplicación
 * Si el usuario está autenticado, muestra HomeFragment
 * Si no, muestra botones para Iniciar Sesión o Crear Cuenta
 */
class MainActivity : AppCompatActivity() {

    private lateinit var gestorSesion: GestorSesion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gestorSesion = GestorSesion(this)

        // Si está autenticado, cargar HomeFragment
        if (gestorSesion.estaAutenticado()) {
            setContentView(R.layout.activity_main_autenticado)

            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    replace(R.id.fragmentContainer, HomeFragment())
                }
            }
        } else {
            // Si no está autenticado, mostrar pantalla de bienvenida
            setContentView(R.layout.activity_main)

            val btnIniciarSesion = findViewById<Button>(R.id.buttonIniciarSesion)
            val btnCrearCuenta = findViewById<Button>(R.id.buttonCrearCuenta)

            btnIniciarSesion.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            btnCrearCuenta.setOnClickListener {
                val intent = Intent(this, RegistroActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // Manejo del botón atrás: En MainActivity SÍ cerramos la app
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // En MainActivity, al presionar atrás, cerrar la aplicación
        finishAffinity() // Cierra todas las activities y la app
    }
}