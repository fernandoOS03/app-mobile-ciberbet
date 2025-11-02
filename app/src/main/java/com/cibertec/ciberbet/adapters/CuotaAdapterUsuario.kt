package com.cibertec.ciberbet.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ciberbet.databinding.ItemCuotaUsuarioBinding
import com.cibertec.ciberbet.models.Cuota

class CuotaAdapterUsuario(
    private val lista: MutableList<Cuota>,
    private val onItemClick: (Cuota) -> Unit
) : RecyclerView.Adapter<CuotaAdapterUsuario.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCuotaUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCuotaUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cuota = lista[position]
        holder.binding.apply {
            tvDescripcion.text = cuota.descripcion
            tvTipo.text = cuota.tipoApuesta
            tvValor.text = "x${cuota.valorCuota}"
        }

        holder.itemView.setOnClickListener {
            onItemClick(cuota)
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Cuota>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}