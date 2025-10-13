package com.ampn.proyecto_notaria.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ampn.proyecto_notaria.R

class AdaptadorHorarios(
    private val horarios: List<String>,
    private val onHorarioClick: (String) -> Unit
) : RecyclerView.Adapter<AdaptadorHorarios.HorarioViewHolder>() {

    private var horarioSeleccionado: String? = null

    class HorarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewHorario: TextView = itemView.findViewById(R.id.textViewHorario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horario, parent, false)
        return HorarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val horario = horarios[position]
        holder.textViewHorario.text = horario

        // Cambiar color si está seleccionado
        if (horario == horarioSeleccionado) {
            holder.textViewHorario.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_light)
            )
            holder.textViewHorario.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )
        } else {
            holder.textViewHorario.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )
            holder.textViewHorario.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.black)
            )
        }

        holder.itemView.setOnClickListener {
            horarioSeleccionado = horario
            notifyDataSetChanged()
            onHorarioClick(horario)
        }
    }

    override fun getItemCount(): Int = horarios.size
}

