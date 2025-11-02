package com.ampn.proyecto_notaria.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.CitaResponse
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para la lista de "Mis Citas"
 * HU-10: Seguimiento y Cancelación de Cita
 */
class AdaptadorCitas(
    private var citas: List<CitaResponse>,
    private val onReprogramarClick: (CitaResponse) -> Unit,
    private val onCancelarClick: (CitaResponse) -> Unit,
    private val onEliminarClick: ((CitaResponse) -> Unit)? = null // ✅ NUEVO: callback para eliminar
) : RecyclerView.Adapter<AdaptadorCitas.CitaViewHolder>() {

    class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNombreTramite: TextView = itemView.findViewById(R.id.textViewNombreTramite)
        val textViewFechaHora: TextView = itemView.findViewById(R.id.textViewFechaHora)
        val textViewEstado: TextView = itemView.findViewById(R.id.textViewEstado)
        val buttonReprogramar: Button = itemView.findViewById(R.id.buttonReprogramar)
        val buttonCancelar: Button = itemView.findViewById(R.id.buttonCancelar)
        val buttonEliminar: Button? = itemView.findViewById(R.id.buttonEliminar) // ✅ NUEVO
        val layoutBotones: View = itemView.findViewById(R.id.layoutBotones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = citas[position]

        // Nombre del trámite
        holder.textViewNombreTramite.text = cita.tramiteNombre

        // Fecha y hora formateadas
        holder.textViewFechaHora.text = formatearFechaHora(cita.fecha, cita.hora)

        // Estado con color
        val (estadoTexto, colorFondo, colorTexto) = obtenerEstadoYColores(cita.estado)
        holder.textViewEstado.text = estadoTexto
        holder.textViewEstado.setTextColor(colorTexto)
        holder.textViewEstado.setBackgroundColor(colorFondo)

        // Configurar botones según el estado de la cita
        when (cita.estado.uppercase()) {
            "AGENDADO", "EN_PROCESO" -> {
                // Cita ACTIVA: mostrar botones de reprogramar y cancelar
                holder.layoutBotones.visibility = View.VISIBLE
                holder.buttonReprogramar.visibility = View.VISIBLE
                holder.buttonCancelar.visibility = View.VISIBLE
                holder.buttonEliminar?.visibility = View.GONE

                holder.buttonReprogramar.setOnClickListener {
                    onReprogramarClick(cita)
                }

                holder.buttonCancelar.setOnClickListener {
                    onCancelarClick(cita)
                }
            }
            "CANCELADO", "FINALIZADO" -> {
                // Cita PASADA/CANCELADA: mostrar solo botón de eliminar
                holder.layoutBotones.visibility = View.VISIBLE
                holder.buttonReprogramar.visibility = View.GONE
                holder.buttonCancelar.visibility = View.GONE
                holder.buttonEliminar?.visibility = View.VISIBLE

                holder.buttonEliminar?.setOnClickListener {
                    onEliminarClick?.invoke(cita)
                }
            }
            else -> {
                holder.layoutBotones.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = citas.size

    /**
     * Formatear fecha y hora
     */
    private fun formatearFechaHora(fecha: String, hora: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale.forLanguageTag("es-ES"))
            val date = formatoEntrada.parse(fecha)
            val fechaFormateada = date?.let {
                SimpleDateFormat("dd 'de' MMMM", Locale.forLanguageTag("es-ES")).format(it)
            } ?: fecha
            "$fechaFormateada, $hora"
        } catch (e: Exception) {
            "$fecha, $hora"
        }
    }

    /**
     * Obtener texto y colores según el estado
     */
    private fun obtenerEstadoYColores(estado: String): Triple<String, Int, Int> {
        return when (estado.uppercase()) {
            "AGENDADO" -> Triple(
                "Confirmada",
                Color.parseColor("#E8F8F5"), // Fondo verde claro
                Color.parseColor("#1ABC9C")  // Texto verde
            )
            "EN_PROCESO" -> Triple(
                "En proceso",
                Color.parseColor("#EBF5FB"), // Fondo azul claro
                Color.parseColor("#3498DB")  // Texto azul
            )
            "REPROGRAMADA" -> Triple(
                "Reprogramada",
                Color.parseColor("#FEF5E7"), // Fondo naranja claro
                Color.parseColor("#F39C12")  // Texto naranja
            )
            "CANCELADO" -> Triple(
                "Cancelada",
                Color.parseColor("#FADBD8"), // Fondo rojo claro
                Color.parseColor("#E74C3C")  // Texto rojo
            )
            "FINALIZADO" -> Triple(
                "Finalizada",
                Color.parseColor("#E8F8F5"), // Fondo verde claro
                Color.parseColor("#2ECC71")  // Texto verde oscuro
            )
            else -> Triple(
                estado,
                Color.parseColor("#F2F3F4"), // Fondo gris
                Color.parseColor("#95A5A6")  // Texto gris
            )
        }
    }

    /**
     * Actualizar lista de citas
     */
    fun actualizarCitas(nuevasCitas: List<CitaResponse>) {
        citas = nuevasCitas
        notifyDataSetChanged()
    }
}
