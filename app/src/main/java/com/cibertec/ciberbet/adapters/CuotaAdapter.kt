package com.cibertec.ciberbet.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ciberbet.databinding.ItemCuotaBinding
import com.cibertec.ciberbet.models.Cuota

class CuotaAdapter(
    private val listaCuotas: List<Cuota>,
    private val eventosMap: Map<String, String>, // id_evento -> nombre
    private val onEditClick: (Cuota) -> Unit,
    private val onDeleteClick: (Cuota) -> Unit
) : RecyclerView.Adapter<CuotaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCuotaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCuotaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cuota = listaCuotas[position]

        holder.binding.apply {
            // Información principal de la cuota
            tvDescripcionCuota.text = cuota.descripcion.ifEmpty { "Sin descripción" }
            tvValorCuota.text = String.format("%.2f", cuota.valorCuota)

            // Estado de la cuota con color
            tvEstadoCuota.text = cuota.estado.uppercase()
            when (cuota.estado.lowercase()) {
                "activa" -> {
                    tvEstadoCuota.apply {
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#4CAF50")) // verde
                    }
                }
                "cerrada" -> {
                    tvEstadoCuota.apply {
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#F44336")) // rojo
                    }
                }
                else -> {
                    tvEstadoCuota.apply {
                        setTextColor(Color.BLACK)
                        setBackgroundColor(Color.parseColor("#FFEB3B")) // amarillo
                    }
                }
            }

            // Tipo de apuesta
            tvTipoApuesta.text = "Tipo: ${cuota.tipoApuesta}"

            // Mostrar nombre del evento desde el map
            tvEventoNombre.text = eventosMap[cuota.idEvento] ?: "Evento no encontrado"

            // Listeners para editar y eliminar
            btnEditarCuota.setOnClickListener {
                onEditClick(cuota)
            }

            btnEliminarCuota.setOnClickListener {
                onDeleteClick(cuota)
            }
        }
    }

    override fun getItemCount() = listaCuotas.size
}