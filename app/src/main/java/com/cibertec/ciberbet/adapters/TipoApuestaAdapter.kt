package com.cibertec.ciberbet.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ciberbet.databinding.ItemTipoApuestaBinding
import com.cibertec.ciberbet.models.TipoApuesta

class TipoApuestaAdapter (

    private val listaTipos: List<TipoApuesta>,
    private val onEditClick: (TipoApuesta) -> Unit,
    private val onDeleteClick: (TipoApuesta) -> Unit
) : RecyclerView.Adapter<TipoApuestaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTipoApuestaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTipoApuestaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tipo = listaTipos[position]

        holder.binding.apply {
            tvNombreTipoApuesta.text = tipo.nombre
            tvDescripcionTipoApuesta.text = tipo.descripcion

            btnEditarTipoApuesta.setOnClickListener {
                onEditClick(tipo)
            }

            btnEliminarTipoApuesta.setOnClickListener {
                onDeleteClick(tipo)
            }
        }
    }

    override fun getItemCount() = listaTipos.size
}