package com.ampn.proyecto_notaria.control

import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import com.ampn.proyecto_notaria.api.modelos.Notificacion
import java.text.SimpleDateFormat
import java.util.*

class NotificacionGenerator {

    fun generar(citas: List<CitaResponse>): List<Notificacion> {
        val notificaciones = mutableListOf<Notificacion>()
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        citas.take(5).forEachIndexed { index, cita ->
            when (cita.estado.uppercase()) {
                "AGENDADO" -> {
                    notificaciones.add(Notificacion(
                        id = index + 1, tipo = Notificacion.TIPO_CONFIRMACION, titulo = "Cita Agendada",
                        mensaje = "Tu cita para '${cita.tramiteNombre}' ha sido agendada.",
                        fecha = cita.creadaEn ?: fechaActual, leida = false, citaId = cita.id
                    ))
                }
                "EN PROCESO" -> {
                    notificaciones.add(Notificacion(
                        id = index + 1, tipo = Notificacion.TIPO_ACTUALIZACION,
                        titulo = "Trámite Actualizado",
                        mensaje = "Tu trámite '${cita.tramiteNombre}' ahora está 'En Proceso'.",
                        fecha = cita.creadaEn ?: fechaActual, leida = false, citaId = cita.id
                    ))
                }
                "REPROGRAMADO" -> {
                    notificaciones.add(Notificacion(
                        id = index + 1, tipo = Notificacion.TIPO_REPROGRAMACION, titulo = "Cita Reprogramada",
                        mensaje = "Tu cita para '${cita.tramiteNombre}' ha sido reprogramada.",
                        fecha = cita.creadaEn ?: fechaActual, leida = false, citaId = cita.id
                    ))
                }
            }
        }
        return notificaciones.sortedByDescending { it.fecha }.take(3)
    }
}
