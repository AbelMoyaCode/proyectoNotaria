package com.ampn.proyecto_notaria.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R
import com.ampn.proyecto_notaria.modelos.Tramite
import java.text.NumberFormat
import java.util.Locale

/**
 * Adaptador para mostrar la lista de trámites disponibles.
 * Permite filtrar por nombre y navegar al detalle.
 */
class AdaptadorTramites(
    private var tramites: List<Tramite>,
    private val alClickear: (Tramite) -> Unit
) : RecyclerView.Adapter<AdaptadorTramites.TramiteViewHolder>() {

    private var tramitesFiltrados: List<Tramite> = tramites

    inner class TramiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombreTramite)
        val textViewDescripcion: TextView = itemView.findViewById(R.id.textViewDescripcionBreve)
        val textViewPrecio: TextView = itemView.findViewById(R.id.textViewPrecioTramite)

        fun vincular(tramite: Tramite) {
            textViewNombre.text = tramite.nombre
            textViewDescripcion.text = tramite.descripcion

            @Suppress("DEPRECATION")
            val formatoPrecio = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
            textViewPrecio.text = formatoPrecio.format(tramite.precio)

            itemView.setOnClickListener {
                alClickear(tramite)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TramiteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tramite, parent, false)
        return TramiteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: TramiteViewHolder, position: Int) {
        holder.vincular(tramitesFiltrados[position])
    }

    override fun getItemCount(): Int = tramitesFiltrados.size

    /**
     * Actualiza la lista completa de trámites
     */
    fun actualizarTramites(nuevosTramites: List<Tramite>) {
        tramites = nuevosTramites
        tramitesFiltrados = nuevosTramites
        notifyDataSetChanged()
    }

    /**
     * Filtra los trámites por nombre o descripción
     */
    fun filtrar(query: String) {
        tramitesFiltrados = if (query.isEmpty()) {
            tramites
        } else {
            tramites.filter { tramite ->
                tramite.nombre.contains(query, ignoreCase = true) ||
                tramite.descripcion.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}
