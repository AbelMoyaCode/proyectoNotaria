package com.ampn.proyecto_notaria.control

import java.util.Calendar
import java.util.Date

class ReprogramValidator {

    /**
     * Verifica si una cita se puede reprogramar.
     * La regla es: la cita debe ser al menos 2 días en el futuro (después de mañana).
     */
    fun esReprogramable(fechaCita: Date): Boolean {
        val calendarioCita = Calendar.getInstance().apply {
            time = fechaCita
        }

        // Apuntar a mañana
        val calendarioManana = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }

        // La cita debe ser estrictamente posterior a mañana
        return calendarioCita.after(calendarioManana)
    }
}
