package com.cibertec.ciberbet.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ciberbet.R
import com.cibertec.ciberbet.databinding.ItemMatchBinding
import com.cibertec.ciberbet.models.Match

class MatchAdapter(
    private val matchList: List<Match>,
    private val onItemClick: (Match) -> Unit
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    // Esta clase interna representa una sola fila (un solo item_match.xml)
    inner class MatchViewHolder(val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root)

    // Se llama para crear una nueva tarjeta vacía
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    // Se llama para rellenar una tarjeta con los datos de un partido
    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matchList[position]
        holder.binding.tvTeam1.text = match.team1Name
        holder.binding.tvTeam2.text = match.team2Name
        holder.binding.tvScore.text = match.score
        holder.binding.tvTime.text = match.time
        holder.binding.tvLeague.text = match.league

        // Aquí está la magia: configura el clic para toda la tarjeta
        holder.itemView.setOnClickListener {
            onItemClick(match)
        }
    }

    // Devuelve cuántos elementos hay en la lista
    override fun getItemCount(): Int {
        return matchList.size
    }
}
