package com.ampn.proyecto_notaria.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampn.proyecto_notaria.api.modelos.Usuario
import com.ampn.proyecto_notaria.api.utils.GestorSesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el estado y lógica de usuarios/perfil
 * Usado en: PerfilActivity
 */
class UsuariosViewModel(private val gestorSesion: GestorSesion) : ViewModel() {

    // Estados observables
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _sesionActiva = MutableStateFlow(false)
    val sesionActiva: StateFlow<Boolean> = _sesionActiva

    init {
        cargarUsuarioActual()
    }

    /**
     * Cargar datos del usuario actual desde la sesión
     */
    fun cargarUsuarioActual() {
        viewModelScope.launch {
            val usuarioActual = gestorSesion.obtenerUsuario()
            _usuario.value = usuarioActual
            _sesionActiva.value = gestorSesion.estaAutenticado()
        }
    }

    /**
     * Actualizar datos del usuario
     */
    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            gestorSesion.guardarUsuario(usuario)
            _usuario.value = usuario
        }
    }

    /**
     * Cerrar sesión
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            gestorSesion.cerrarSesion()
            _usuario.value = null
            _sesionActiva.value = false
        }
    }

    /**
     * Obtener ID del usuario actual
     */
    fun obtenerUsuarioId(): Int? {
        return _usuario.value?.id
    }

    /**
     * Obtener nombre completo del usuario actual
     */
    fun obtenerNombreUsuario(): String {
        return _usuario.value?.nombreCompleto() ?: "Usuario"
    }
}
