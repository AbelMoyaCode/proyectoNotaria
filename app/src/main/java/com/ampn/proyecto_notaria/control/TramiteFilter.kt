package com.ampn.proyecto_notaria.control

import com.ampn.proyecto_notaria.api.modelos.Tramite

class TramiteFilter {

    fun filter(tramites: List<Tramite>, query: String): List<Tramite> {
        return if (query.isBlank()) {
            tramites
        } else {
            tramites.filter { tramite ->
                tramite.nombre.contains(query, ignoreCase = true)
            }
        }
    }
}
