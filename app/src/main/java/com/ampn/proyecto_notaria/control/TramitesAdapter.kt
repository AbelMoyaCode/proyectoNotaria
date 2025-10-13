package com.ampn.proyecto_notaria.control

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
        val nombre: TextView = itemView.findViewById(R.id.textViewNombreTramite)
        val descripcion: TextView = itemView.findViewById(R.id.textViewDescripcionBreve)
        val precio: TextView = itemView.findViewById(R.id.textViewPrecioTramite)

        fun vincular(tramite: Tramite) {
            nombre.text = tramite.nombre
            descripcion.text = tramite.descripcion
            precio.text = "S/ ${String.format("%.2f", tramite.precio)}"

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
