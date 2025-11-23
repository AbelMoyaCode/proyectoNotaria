package com.ampn.proyecto_notaria.adapters

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

class AdaptadorCitas(
    private var citas: List<CitaResponse>,
    private val onVerDetallesClick: (CitaResponse) -> Unit, // Listener para ver detalles
    private val onCancelarClick: (CitaResponse) -> Unit      // Listener para cancelar
) : RecyclerView.Adapter<AdaptadorCitas.CitaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.bind(citas[position])
    }

    override fun getItemCount(): Int = citas.size

    fun actualizarCitas(nuevasCitas: List<CitaResponse>) {
        citas = nuevasCitas
        notifyDataSetChanged()
    }

    // Función para formatear la fecha
    private fun formatarFecha(fechaISO: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fecha = formatoEntrada.parse(fechaISO)
            if (fecha != null) formatoSalida.format(fecha) else fechaISO
        } catch (e: Exception) {
            fechaISO // Si falla, devolver la fecha original
        }
    }

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTramiteNombre: TextView = itemView.findViewById(R.id.textViewTramiteNombre)
        private val textViewFecha: TextView = itemView.findViewById(R.id.textViewFecha)
        private val textViewHora: TextView = itemView.findViewById(R.id.textViewHora)
        private val textViewEstado: TextView = itemView.findViewById(R.id.textViewEstado)
        private val buttonVerDetalles: Button = itemView.findViewById(R.id.buttonVerDetalles)
        private val buttonCancelar: Button = itemView.findViewById(R.id.buttonCancelar)

        fun bind(cita: CitaResponse) {
            textViewTramiteNombre.text = cita.tramiteNombre
            textViewFecha.text = "Fecha: ${formatarFecha(cita.fecha)}" // Usar la nueva función
            textViewHora.text = "Hora: ${cita.hora}"
            textViewEstado.text = "Estado: ${cita.estado}"

            // Asignar los clicks a los listeners
            buttonVerDetalles.setOnClickListener { onVerDetallesClick(cita) }
            buttonCancelar.setOnClickListener { onCancelarClick(cita) }
        }
    }
}