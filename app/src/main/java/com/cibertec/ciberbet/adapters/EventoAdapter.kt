package com.cibertec.ciberbet.adapters

import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.view.LayoutInflater

import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ciberbet.databinding.ItemEventoBinding
import com.cibertec.ciberbet.models.Evento

class EventoAdapter(
    private val listaEventos: List<Evento>,
    private val deportesMap: Map<String, String>,
    private val equiposMap: Map<String, String>,
    private val onEditClick: (Evento) -> Unit,
    private val onDeleteClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemEventoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evento = listaEventos[position]

        holder.binding.apply {
            // Nombre del deporte
            val nombreDeporte = deportesMap[evento.idDeporte] ?: "Deporte desconocido"
            tvDeporteEvento.text = nombreDeporte

            // Nombres de los equipos
            val nombreLocal = equiposMap[evento.equipoLocal] ?: "Equipo desconocido"
            val nombreVisitante = equiposMap[evento.equipoVisitante] ?: "Equipo desconocido"
            tvEquipoLocal.text = nombreLocal
            tvEquipoVisitante.text = nombreVisitante

            // Fecha, hora y ubicación
            tvFechaHora.text = "${evento.fecha_hora}"
            tvUbicacion.text = "${evento.ubicacion}"

            // Estado del evento
            tvEstadoEvento.text = evento.estadoEvento
            when (evento.estadoEvento) {
                "Programado" -> tvEstadoEvento.setTextColor(Color.parseColor("#4CAF50"))
                "En Vivo" -> tvEstadoEvento.setTextColor(Color.parseColor("#FF5722"))
                "Finalizado" -> tvEstadoEvento.setTextColor(Color.parseColor("#9E9E9E"))
            }

            // Mostrar resultados si el evento está finalizado o en vivo
            if (evento.estadoEvento == "Finalizado" || evento.estadoEvento == "En Vivo") {
                tvResultadoLocal.text = evento.resultadoLocal.toString()
                tvResultadoVisitante.text = evento.resultadoVisitante.toString()
                tvResultadoLocal.visibility = View.VISIBLE
                tvResultadoVisitante.visibility = View.VISIBLE
            } else {
                tvResultadoLocal.visibility = View.GONE
                tvResultadoVisitante.visibility = View.GONE
            }

            btnEditarEvento.setOnClickListener {
                onEditClick(evento)
            }

            btnEliminarEvento.setOnClickListener {
                onDeleteClick(evento)
            }
        }
    }

    override fun getItemCount() = listaEventos.size
}
