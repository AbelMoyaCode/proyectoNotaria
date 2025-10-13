package com.ampn.proyecto_notaria.control

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.api.modelos.Tramite

/**
 * Adaptador para mostrar la lista de trámites disponibles
 */
class TramitesAdapter(
    private var listaTramites: List<Tramite>,
    private val alClickear: (Tramite) -> Unit
) : RecyclerView.Adapter<TramitesAdapter.TramiteViewHolder>() {

    inner class TramiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoria: TextView = itemView.findViewById(R.id.textViewCategoria)
        val nombre: TextView = itemView.findViewById(R.id.textViewNombreTramite)
        val descripcion: TextView = itemView.findViewById(R.id.textViewDescripcionBreve)
        val precio: TextView = itemView.findViewById(R.id.textViewPrecioTramite)
        val duracion: TextView = itemView.findViewById(R.id.textViewDuracion)
        val indicadorColor: View = itemView.findViewById(R.id.indicadorColor)

        fun vincular(tramite: Tramite) {
            categoria.text = tramite.categoria ?: "General"
            nombre.text = tramite.nombre
            descripcion.text = tramite.descripcion
            precio.text = "S/ ${String.format("%.2f", tramite.precio)}"
            duracion.text = tramite.duracion_estimada ?: "Consultar"

            // Aplicar color según categoría
            val colorCategoria = obtenerColorCategoria(tramite.categoria ?: "General")
            indicadorColor.setBackgroundColor(Color.parseColor(colorCategoria))
            categoria.setTextColor(Color.parseColor(colorCategoria))

            itemView.setOnClickListener {
                alClickear(tramite)
            }
        }

        private fun obtenerColorCategoria(categoria: String): String {
            return when (categoria.lowercase()) {
                "general" -> "#4CAF50"          // Verde
                "notarial" -> "#2196F3"         // Azul
                "legal" -> "#FF9800"            // Naranja
                "documentación" -> "#9C27B0"    // Morado
                "certificación" -> "#F44336"    // Rojo
                "escrituras" -> "#00BCD4"       // Cyan
                "testamentos" -> "#795548"      // Marrón
                "poderes" -> "#607D8B"          // Gris azulado
                else -> "#4CAF50"               // Verde por defecto
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TramiteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tramite, parent, false)
        return TramiteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: TramiteViewHolder, position: Int) {
        holder.vincular(listaTramites[position])
    }

    override fun getItemCount(): Int = listaTramites.size

    /**
     * Actualiza la lista de trámites y notifica al RecyclerView
     */
    fun actualizarTramites(nuevosTramites: List<Tramite>) {
        listaTramites = nuevosTramites
        notifyDataSetChanged()
    }
}
