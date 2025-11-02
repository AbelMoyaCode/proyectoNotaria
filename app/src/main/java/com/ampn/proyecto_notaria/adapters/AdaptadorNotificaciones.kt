package com.ampn.proyecto_notaria.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.Notificacion
import java.text.SimpleDateFormat
import java.util.*

class AdaptadorNotificaciones(
    private var notificaciones: List<Notificacion>,
    private val onNotificacionClick: (Notificacion) -> Unit
) : RecyclerView.Adapter<AdaptadorNotificaciones.NotificacionViewHolder>() {

    class NotificacionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardNotificacion: CardView = view.findViewById(R.id.cardNotificacion)
        val textIcono: TextView = view.findViewById(R.id.textIcono)
        val textTitulo: TextView = view.findViewById(R.id.textTitulo)
        val textMensaje: TextView = view.findViewById(R.id.textMensaje)
        val textFecha: TextView = view.findViewById(R.id.textFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notificacion = notificaciones[position]

        holder.textIcono.text = notificacion.getIcono()
        holder.textTitulo.text = notificacion.titulo
        holder.textMensaje.text = notificacion.mensaje
        holder.textFecha.text = formatearFecha(notificacion.fecha)

        // Cambiar opacidad si ya fue leída
        holder.cardNotificacion.alpha = if (notificacion.leida) 0.6f else 1.0f

        holder.cardNotificacion.setOnClickListener {
            onNotificacionClick(notificacion)
        }
    }

    override fun getItemCount() = notificaciones.size

    fun actualizarNotificaciones(nuevasNotificaciones: List<Notificacion>) {
        notificaciones = nuevasNotificaciones
        notifyDataSetChanged()
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = formatoEntrada.parse(fecha)
            formatoSalida.format(date ?: Date())
        } catch (e: Exception) {
            // Si falla, intentar otro formato
            try {
                val formatoEntrada2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = formatoEntrada2.parse(fecha)
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date ?: Date())
            } catch (e2: Exception) {
                fecha
            }
        }
    }
}

