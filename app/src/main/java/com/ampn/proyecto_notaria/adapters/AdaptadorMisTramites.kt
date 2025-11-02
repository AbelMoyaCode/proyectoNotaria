package com.ampn.proyecto_notaria.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para la lista de "Mis Trámites" del usuario
 * HU-11: Gestión de "Mis Trámites"
 */
class AdaptadorMisTramites(
    private var tramites: List<CitaResponse>,
    private val onTramiteClick: (CitaResponse) -> Unit
) : RecyclerView.Adapter<AdaptadorMisTramites.MiTramiteViewHolder>() {

    private var tramitesFiltrados: List<CitaResponse> = tramites

    class MiTramiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewIndicadorEstado: View = itemView.findViewById(R.id.viewIndicadorEstado)
        val textViewNombreTramite: TextView = itemView.findViewById(R.id.textViewNombreTramite)
        val textViewFecha: TextView = itemView.findViewById(R.id.textViewFecha)
        val textViewEstado: TextView = itemView.findViewById(R.id.textViewEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiTramiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mi_tramite, parent, false)
        return MiTramiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: MiTramiteViewHolder, position: Int) {
        val tramite = tramitesFiltrados[position]

        // Nombre del trámite
        holder.textViewNombreTramite.text = tramite.tramiteNombre

        // Fecha formateada
        holder.textViewFecha.text = formatearFecha(tramite.fecha)

        // Estado con color
        val (estadoTexto, estadoColor) = obtenerEstadoYColor(tramite.estado)
        holder.textViewEstado.text = "• $estadoTexto"
        holder.textViewEstado.setTextColor(estadoColor)
        holder.viewIndicadorEstado.backgroundTintList =
            android.content.res.ColorStateList.valueOf(estadoColor)

        // Click para ver detalle (HU-12)
        holder.itemView.setOnClickListener {
            onTramiteClick(tramite)
        }
    }

    override fun getItemCount(): Int = tramitesFiltrados.size

    /**
     * Formatear fecha de YYYY-MM-DD a "dd de MMMM de yyyy"
     */
    private fun formatearFecha(fecha: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            val date = formatoEntrada.parse(fecha)
            date?.let { formatoSalida.format(it) } ?: fecha
        } catch (e: Exception) {
            fecha
        }
    }

    /**
     * Obtener texto y color según el estado del trámite
     * Basado en wireframe: Verde = Finalizado, Naranja = Pendiente, Rojo = Cancelado
     */
    private fun obtenerEstadoYColor(estado: String): Pair<String, Int> {
        return when (estado.uppercase()) {
            "FINALIZADO", "COMPLETADO" -> Pair("Finalizado", Color.parseColor("#2ECC71")) // Verde
            "AGENDADO", "PENDIENTE" -> Pair("Pendiente", Color.parseColor("#F39C12")) // Naranja
            "EN_PROCESO" -> Pair("En proceso", Color.parseColor("#3498DB")) // Azul
            "CANCELADO" -> Pair("Cancelada", Color.parseColor("#E74C3C")) // Rojo
            else -> Pair(estado, Color.parseColor("#95A5A6")) // Gris por defecto
        }
    }

    /**
     * Filtrar trámites por búsqueda
     */
    fun filtrar(query: String) {
        tramitesFiltrados = if (query.isEmpty()) {
            tramites
        } else {
            tramites.filter { tramite ->
                tramite.tramiteNombre.contains(query, ignoreCase = true) ||
                tramite.fecha.contains(query, ignoreCase = true) ||
                tramite.estado.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * Actualizar lista completa de trámites
     */
    fun actualizarTramites(nuevosTramites: List<CitaResponse>) {
        tramites = nuevosTramites
        tramitesFiltrados = nuevosTramites
        notifyDataSetChanged()
    }
}

