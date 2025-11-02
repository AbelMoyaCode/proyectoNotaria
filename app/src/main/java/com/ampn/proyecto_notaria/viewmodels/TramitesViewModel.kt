package com.ampn.proyecto_notaria.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampn.proyecto_notaria.api.modelos.Tramite
import com.ampn.proyecto_notaria.api.repositorios.TramitesRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el estado y lógica de los trámites
 * Usado en: MisTramitesActivity, DetalleTramiteActivity
 */
class TramitesViewModel : ViewModel() {

    private val repositorio = TramitesRepositorio()

    // Estados observables
    private val _tramites = MutableStateFlow<List<Tramite>>(emptyList())
    val tramites: StateFlow<List<Tramite>> = _tramites

    private val _tramiteDetalle = MutableStateFlow<Tramite?>(null)
    val tramiteDetalle: StateFlow<Tramite?> = _tramiteDetalle

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Cargar todos los trámites disponibles
     */
    fun cargarTramites() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            val resultado = repositorio.obtenerTramites()

            _cargando.value = false

            resultado.onSuccess { listaTramites ->
                _tramites.value = listaTramites
            }.onFailure { excepcion ->
                _error.value = excepcion.message
            }
        }
    }

    /**
     * Buscar trámites por texto
     */
    fun buscarTramites(query: String) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            val resultado = if (query.isBlank()) {
                repositorio.obtenerTramites()
            } else {
                repositorio.buscarTramites(query)
            }

            _cargando.value = false

            resultado.onSuccess { listaTramites ->
                _tramites.value = listaTramites
            }.onFailure { excepcion ->
                _error.value = excepcion.message
            }
        }
    }

    /**
     * Cargar detalle de un trámite específico
     */
    fun cargarDetalleTramite(codigo: String) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            val resultado = repositorio.obtenerDetalleTramite(codigo)

            _cargando.value = false

            resultado.onSuccess { tramite ->
                _tramiteDetalle.value = tramite
            }.onFailure { excepcion ->
                _error.value = excepcion.message
            }
        }
    }

    /**
     * Limpiar error
     */
    fun limpiarError() {
        _error.value = null
    }

    /**
     * Limpiar detalle
     */
    fun limpiarDetalle() {
        _tramiteDetalle.value = null
    }
}

