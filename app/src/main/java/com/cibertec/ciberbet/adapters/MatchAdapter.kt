package com.cibertec.ciberbet.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ciberbet.databinding.ItemMatchNewBinding
import com.cibertec.ciberbet.models.Deporte
import com.cibertec.ciberbet.models.Equipo
import com.cibertec.ciberbet.models.Evento
import com.squareup.picasso.Picasso

class MatchAdapter(
    private val listaEventos: List<Evento>,
    private val equiposMap: Map<String, Equipo>,
    private val deportesMap: Map<String, Deporte>
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    inner class MatchViewHolder(val binding: ItemMatchNewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val evento = listaEventos[position]
        val equipoLocal = equiposMap[evento.equipoLocal]
        val equipoVisitante = equiposMap[evento.equipoVisitante]
        val deporte = deportesMap[evento.idDeporte]

        with(holder.binding) {
            tvDeporteEvento.text = deporte?.nombre ?: "Desconocido"
            tvEstadoEvento.text = evento.estadoEvento
            tvTeam1.text = equipoLocal?.nombre ?: "Equipo A"
            tvTeam2.text = equipoVisitante?.nombre ?: "Equipo B"
            tvScore.text = "${evento.resultadoLocal ?: "-"} : ${evento.resultadoVisitante ?: "-"}"
            tvFechaHora.text = evento.fecha_hora
            tvUbicacion.text = evento.ubicacion

            /*// Cargar logos si existen
            if (!equipoLocal?.logo.isNullOrEmpty()) {
                Picasso.get().load(equipoLocal!!.logo).into(ivTeam1)
            }
            if (!equipoVisitante?.logo.isNullOrEmpty()) {
                Picasso.get().load(equipoVisitante!!.logo).into(ivTeam2)
            }*/
        }
    }

    override fun getItemCount() = listaEventos.size
}
