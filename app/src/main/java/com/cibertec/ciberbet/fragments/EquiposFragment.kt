package com.cibertec.ciberbet.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.ciberbet.R
import com.cibertec.ciberbet.adapters.EquipoAdapter
import com.cibertec.ciberbet.databinding.AdminFragmentEquiposBinding
import com.cibertec.ciberbet.models.Deporte
import com.cibertec.ciberbet.models.Equipo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EquiposFragment : Fragment() {

    private var _binding: AdminFragmentEquiposBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var deportesDatabase: DatabaseReference
    private lateinit var equipoAdapter: EquipoAdapter

    private val listaEquipos = mutableListOf<Equipo>()
    private val listaDeportes = mutableListOf<Deporte>()
    private val deportesMap = mutableMapOf<String, String>() // id -> nombre

    private var equipoEditando: Equipo? = null

    private var deportesListener: ValueEventListener? = null
    private var equiposListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AdminFragmentEquiposBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("equipos")
        deportesDatabase = FirebaseDatabase.getInstance().getReference("deportes")

        setupRecyclerView()
        cargarDeportes()
        setupListeners()
    }

    private fun setupRecyclerView() {
        equipoAdapter = EquipoAdapter(
            listaEquipos,
            deportesMap,
            onEditClick = { equipo -> iniciarEdicion(equipo) },
            onDeleteClick = { equipo -> confirmarEliminar(equipo) }
        )

        binding.recyclerEquipos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = equipoAdapter
        }
    }

    private fun setupListeners() {
        binding.btnGuardarEquipo.setOnClickListener {
            if (equipoEditando != null) {
                actualizarEquipo()
            } else {
                agregarEquipo()
            }
        }

        binding.btnCancelar.setOnClickListener {
            cancelarEdicion()
        }
    }

    private fun cargarDeportes() {
        deportesDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                listaDeportes.clear()
                deportesMap.clear()

                for (data in snapshot.children) {
                    val deporte = data.getValue(Deporte::class.java)
                    deporte?.let {
                        listaDeportes.add(it)
                        deportesMap[it.idDeporte] = it.nombre
                    }
                }

                if (listaDeportes.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Primero debes crear deportes",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    setupSpinner()
                    cargarEquipos()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Error al cargar deportes: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupSpinner() {
        if (!isAdded) return

        val deportesNombres = listaDeportes.map { it.nombre }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            deportesNombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDeporte.adapter = adapter
    }

    private fun cargarEquipos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                listaEquipos.clear()
                for (data in snapshot.children) {
                    val equipo = data.getValue(Equipo::class.java)
                    equipo?.let { listaEquipos.add(it) }
                }
                equipoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Error al cargar equipos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun agregarEquipo() {
        val nombre = binding.etNombreEquipo.text.toString().trim()
        val pais = binding.etPaisEquipo.text.toString().trim()
        val logoUrl = binding.etLogoUrl.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.tilNombreEquipo.error = "El nombre es obligatorio"
            return
        }

        if (pais.isEmpty()) {
            binding.tilPaisEquipo.error = "El país es obligatorio"
            return
        }

        if (listaDeportes.isEmpty()) {
            Toast.makeText(context, "No hay deportes disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tilNombreEquipo.error = null
        binding.tilPaisEquipo.error = null

        val deporteSeleccionado = listaDeportes[binding.spinnerDeporte.selectedItemPosition]
        val id = database.push().key ?: return

        val equipo = Equipo(
            idEquipo = id,
            nombre = nombre,
            pais = pais,
            idDeporte = deporteSeleccionado.idDeporte,
            logoUrl = logoUrl
        )

        database.child(id).setValue(equipo)
            .addOnSuccessListener {
                Toast.makeText(context, "Equipo agregado correctamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error al agregar: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun iniciarEdicion(equipo: Equipo) {
        equipoEditando = equipo

        binding.etNombreEquipo.setText(equipo.nombre)
        binding.etPaisEquipo.setText(equipo.pais)
        binding.etLogoUrl.setText(equipo.logoUrl)

        // Seleccionar el deporte en el spinner
        val posicion = listaDeportes.indexOfFirst { it.idDeporte == equipo.idDeporte }
        if (posicion >= 0) {
            binding.spinnerDeporte.setSelection(posicion)
        }

        binding.btnGuardarEquipo.text = "Actualizar"
        binding.btnCancelar.visibility = View.VISIBLE

        binding.root.scrollTo(0, 0)
    }

    private fun actualizarEquipo() {
        val nombre = binding.etNombreEquipo.text.toString().trim()
        val pais = binding.etPaisEquipo.text.toString().trim()
        val logoUrl = binding.etLogoUrl.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.tilNombreEquipo.error = "El nombre es obligatorio"
            return
        }

        if (pais.isEmpty()) {
            binding.tilPaisEquipo.error = "El país es obligatorio"
            return
        }

        binding.tilNombreEquipo.error = null
        binding.tilPaisEquipo.error = null

        equipoEditando?.let { equipo ->
            val deporteSeleccionado = listaDeportes[binding.spinnerDeporte.selectedItemPosition]

            val equipoActualizado = equipo.copy(
                nombre = nombre,
                pais = pais,
                idDeporte = deporteSeleccionado.idDeporte,
                logoUrl = logoUrl
            )

            database.child(equipo.idEquipo).setValue(equipoActualizado)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Equipo actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    cancelarEdicion()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(
                        context,
                        "Error al actualizar: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun confirmarEliminar(equipo: Equipo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Equipo")
            .setMessage("¿Estás seguro de eliminar ${equipo.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarEquipo(equipo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarEquipo(equipo: Equipo) {
        database.child(equipo.idEquipo).removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Equipo eliminado correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error al eliminar: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun cancelarEdicion() {
        equipoEditando = null
        limpiarCampos()
        binding.btnGuardarEquipo.text = "Agregar"
        binding.btnCancelar.visibility = View.GONE
    }

    private fun limpiarCampos() {
        binding.etNombreEquipo.text?.clear()
        binding.etPaisEquipo.text?.clear()
        binding.etLogoUrl.text?.clear()
        binding.spinnerDeporte.setSelection(0)
        binding.tilNombreEquipo.error = null
        binding.tilPaisEquipo.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        deportesListener?.let { deportesDatabase.removeEventListener(it) }
        equiposListener?.let { database.removeEventListener(it) }


        _binding = null
    }
}