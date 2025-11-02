package com.ampn.proyecto_notaria.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el estado y lógica de las citas
 * Usado en: AgendarCitaActivity, MisCitasActivity
 */
class CitasViewModel : ViewModel() {

    private val repositorio = CitasRepositorio()

    // Estados observables
    private val _citas = MutableStateFlow<List<CitaResponse>>(emptyList())
    val citas: StateFlow<List<CitaResponse>> = _citas

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Crear una nueva cita
     */
    fun crearCita(
        usuarioId: Int,
        tramiteCodigo: String,
        fecha: String,
        hora: String,
        onSuccess: (CitaResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            val resultado = repositorio.crearCita(usuarioId, tramiteCodigo, fecha, hora)

            _cargando.value = false

            resultado.onSuccess { cita ->
                onSuccess(cita)
            }.onFailure { excepcion ->
                val mensaje = excepcion.message ?: "Error al crear la cita"
                _error.value = mensaje
                onError(mensaje)
            }
        }
    }

    /**
     * Obtener citas del usuario
     */
    fun cargarCitas(usuarioId: Int) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            val resultado = repositorio.obtenerCitasUsuario(usuarioId)

            _cargando.value = false

            resultado.onSuccess { listaCitas ->
                _citas.value = listaCitas
            }.onFailure { excepcion ->
                _error.value = excepcion.message
            }
        }
    }

    /**
     * Cancelar una cita
     */
    fun cancelarCita(
        citaId: Int,
        motivo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _cargando.value = true

            val resultado = repositorio.cancelarCita(citaId, motivo)

            _cargando.value = false

            resultado.onSuccess {
                onSuccess()
            }.onFailure { excepcion ->
                onError(excepcion.message ?: "Error al cancelar la cita")
            }
        }
    }

    /**
     * Limpiar error
     */
    fun limpiarError() {
        _error.value = null
    }
}
