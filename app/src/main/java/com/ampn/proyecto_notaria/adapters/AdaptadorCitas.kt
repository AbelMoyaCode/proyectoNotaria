package com.ampn.proyecto_notaria.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.CitaResponse

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

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTramiteNombre: TextView = itemView.findViewById(R.id.textViewTramiteNombre)
        private val textViewFecha: TextView = itemView.findViewById(R.id.textViewFecha)
        private val textViewHora: TextView = itemView.findViewById(R.id.textViewHora)
        private val textViewEstado: TextView = itemView.findViewById(R.id.textViewEstado)
        private val buttonVerDetalles: Button = itemView.findViewById(R.id.buttonVerDetalles)
        private val buttonCancelar: Button = itemView.findViewById(R.id.buttonCancelar)

        fun bind(cita: CitaResponse) {
            textViewTramiteNombre.text = cita.tramiteNombre
            textViewFecha.text = "Fecha: ${cita.fecha}"
            textViewHora.text = "Hora: ${cita.hora}"
            textViewEstado.text = "Estado: ${cita.estado}"

            // Asignar los clicks a los listeners
            buttonVerDetalles.setOnClickListener { onVerDetallesClick(cita) }
            buttonCancelar.setOnClickListener { onCancelarClick(cita) }
        }
    }
}