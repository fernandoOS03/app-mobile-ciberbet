package com.cibertec.ciberbet.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.ciberbet.R
import com.cibertec.ciberbet.adapters.EventoAdapter
import com.cibertec.ciberbet.databinding.AdminFragmentEventosBinding
import com.cibertec.ciberbet.models.Deporte
import com.cibertec.ciberbet.models.Equipo
import com.cibertec.ciberbet.models.Evento
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventosFragment : Fragment() {
    private var _binding: AdminFragmentEventosBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var deportesDatabase: DatabaseReference
    private lateinit var equiposDatabase: DatabaseReference
    private lateinit var eventoAdapter: EventoAdapter

    private val listaEventos = mutableListOf<Evento>()
    private val listaDeportes = mutableListOf<Deporte>()
    private val listaEquipos = mutableListOf<Equipo>()
    private val equiposFiltrados = mutableListOf<Equipo>()

    private val deportesMap = mutableMapOf<String, String>() // id -> nombre
    private val equiposMap = mutableMapOf<String, String>() // id -> nombre

    private var eventoEditando: Evento? = null
    private val estadosPosibles = listOf("Programado", "En Vivo", "Finalizado")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AdminFragmentEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("eventos_deportivos")
        deportesDatabase = FirebaseDatabase.getInstance().getReference("deportes")
        equiposDatabase = FirebaseDatabase.getInstance().getReference("equipos")

        setupRecyclerView()
        setupSpinnerEstado()
        cargarDeportes()
        cargarEquipos()
        setupListeners()
    }

    private fun setupRecyclerView() {
        eventoAdapter = EventoAdapter(
            listaEventos,
            deportesMap,
            equiposMap,
            onEditClick = { evento -> iniciarEdicion(evento) },
            onDeleteClick = { evento -> confirmarEliminar(evento) }
        )

        binding.recyclerEventos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventoAdapter
        }
    }

    private fun setupSpinnerEstado() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            estadosPosibles
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEstado.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnGuardarEvento.setOnClickListener {
            if (eventoEditando != null) {
                actualizarEvento()
            } else {
                agregarEvento()
            }
        }

        binding.btnCancelar.setOnClickListener {
            cancelarEdicion()
        }

        // Cuando se selecciona un deporte, filtrar los equipos
        binding.spinnerDeporte.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaDeportes.isNotEmpty()) {
                    val deporteSeleccionado = listaDeportes[position]
                    filtrarEquiposPorDeporte(deporteSeleccionado.idDeporte)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun cargarDeportes() {
        deportesDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                    Toast.makeText(context, "Primero debes crear deportes", Toast.LENGTH_LONG).show()
                } else {
                    setupSpinnerDeportes()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar deportes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarEquipos() {
        equiposDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaEquipos.clear()
                equiposMap.clear()

                for (data in snapshot.children) {
                    val equipo = data.getValue(Equipo::class.java)
                    equipo?.let {
                        listaEquipos.add(it)
                        equiposMap[it.idEquipo] = it.nombre
                    }
                }

                if (listaEquipos.isEmpty()) {
                    Toast.makeText(context, "Primero debes crear equipos", Toast.LENGTH_LONG).show()
                } else {
                    cargarEventos()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar equipos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSpinnerDeportes() {
        val deportesNombres = listaDeportes.map { it.nombre }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            deportesNombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDeporte.adapter = adapter
    }

    private fun filtrarEquiposPorDeporte(idDeporte: String) {
        equiposFiltrados.clear()
        equiposFiltrados.addAll(listaEquipos.filter { it.idDeporte == idDeporte })

        if (equiposFiltrados.isEmpty()) {
            Toast.makeText(context, "No hay equipos para este deporte", Toast.LENGTH_SHORT).show()
        }

        setupSpinnersEquipos()
    }

    private fun setupSpinnersEquipos() {
        val equiposNombres = equiposFiltrados.map { it.nombre }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            equiposNombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerEquipoLocal.adapter = adapter
        binding.spinnerEquipoVisitante.adapter = adapter
    }

    private fun cargarEventos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaEventos.clear()
                for (data in snapshot.children) {
                    val evento = data.getValue(Evento::class.java)
                    evento?.let { listaEventos.add(it) }
                }
                eventoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar eventos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun agregarEvento() {
        val fechaHora = binding.etFechaHora.text.toString().trim()
        val ubicacion = binding.etUbicacion.text.toString().trim()

        if (fechaHora.isEmpty()) {
            binding.tilFechaHora.error = "La fecha y hora son obligatorias"
            return
        }

        if (ubicacion.isEmpty()) {
            binding.tilUbicacion.error = "La ubicación es obligatoria"
            return
        }

        if (equiposFiltrados.isEmpty()) {
            Toast.makeText(context, "No hay equipos disponibles para este deporte", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.spinnerEquipoLocal.selectedItemPosition == binding.spinnerEquipoVisitante.selectedItemPosition) {
            Toast.makeText(context, "Los equipos deben ser diferentes", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tilFechaHora.error = null
        binding.tilUbicacion.error = null

        val deporteSeleccionado = listaDeportes[binding.spinnerDeporte.selectedItemPosition]
        val equipoLocal = equiposFiltrados[binding.spinnerEquipoLocal.selectedItemPosition]
        val equipoVisitante = equiposFiltrados[binding.spinnerEquipoVisitante.selectedItemPosition]
        val estado = estadosPosibles[binding.spinnerEstado.selectedItemPosition]

        val id = database.push().key ?: return

        val evento = Evento(
            idEvento = id,
            idDeporte = deporteSeleccionado.idDeporte,
            equipoLocal = equipoLocal.idEquipo,
            equipoVisitante = equipoVisitante.idEquipo,
            fecha_hora = fechaHora,
            ubicacion = ubicacion,
            estadoEvento = estado,
            resultadoLocal = 0,
            resultadoVisitante = 0
        )

        database.child(id).setValue(evento)
            .addOnSuccessListener {
                Toast.makeText(context, "Evento creado correctamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al crear: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun iniciarEdicion(evento: Evento) {
        eventoEditando = evento

        // Seleccionar deporte
        val posDeporte = listaDeportes.indexOfFirst { it.idDeporte == evento.idDeporte }
        if (posDeporte >= 0) {
            binding.spinnerDeporte.setSelection(posDeporte)

            // Filtrar equipos y esperar a que el adaptador se actualice
            filtrarEquiposPorDeporte(evento.idDeporte)

            // Esperamos un poco para asegurar que los equipos filtrados ya se cargaron
            binding.spinnerEquipoLocal.postDelayed({
                val posLocal = equiposFiltrados.indexOfFirst { it.idEquipo == evento.equipoLocal }
                val posVisitante = equiposFiltrados.indexOfFirst { it.idEquipo == evento.equipoVisitante }

                if (posLocal >= 0) binding.spinnerEquipoLocal.setSelection(posLocal)
                if (posVisitante >= 0) binding.spinnerEquipoVisitante.setSelection(posVisitante)
            }, 200) // 200ms suele bastar, pero puedes ajustar si hace falta
        }

        binding.etFechaHora.setText(evento.fecha_hora)
        binding.etUbicacion.setText(evento.ubicacion)

        val posEstado = estadosPosibles.indexOf(evento.estadoEvento)
        if (posEstado >= 0) binding.spinnerEstado.setSelection(posEstado)

        binding.btnGuardarEvento.text = "Actualizar"
        binding.btnCancelar.visibility = View.VISIBLE

        binding.root.scrollTo(0, 0)
    }


    /*
    private fun iniciarEdicion(evento: Evento) {
        eventoEditando = evento

        // Seleccionar deporte
        val posDeporte = listaDeportes.indexOfFirst { it.idDeporte == evento.idDeporte }
        if (posDeporte >= 0) {
            binding.spinnerDeporte.setSelection(posDeporte)

            // Filtrar equipos del deporte
            filtrarEquiposPorDeporte(evento.idDeporte)

            // Seleccionar equipos
            val posLocal = equiposFiltrados.indexOfFirst { it.idEquipo == evento.equipoLocal }
            val posVisitante = equiposFiltrados.indexOfFirst { it.idEquipo == evento.equipoVisitante }

            if (posLocal >= 0) binding.spinnerEquipoLocal.setSelection(posLocal)
            if (posVisitante >= 0) binding.spinnerEquipoVisitante.setSelection(posVisitante)
        }

        binding.etFechaHora.setText(evento.fecha_hora)
        binding.etUbicacion.setText(evento.ubicacion)

        val posEstado = estadosPosibles.indexOf(evento.estadoEvento)
        if (posEstado >= 0) binding.spinnerEstado.setSelection(posEstado)

        binding.btnGuardarEvento.text = "Actualizar"
        binding.btnCancelar.visibility = View.VISIBLE

        binding.root.scrollTo(0, 0)
    }
    */

    private fun actualizarEvento() {
        val fechaHora = binding.etFechaHora.text.toString().trim()
        val ubicacion = binding.etUbicacion.text.toString().trim()

        if (fechaHora.isEmpty()) {
            binding.tilFechaHora.error = "La fecha y hora son obligatorias"
            return
        }

        if (ubicacion.isEmpty()) {
            binding.tilUbicacion.error = "La ubicación es obligatoria"
            return
        }

        if (binding.spinnerEquipoLocal.selectedItemPosition == binding.spinnerEquipoVisitante.selectedItemPosition) {
            Toast.makeText(context, "Los equipos deben ser diferentes", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tilFechaHora.error = null
        binding.tilUbicacion.error = null

        eventoEditando?.let { evento ->
            val deporteSeleccionado = listaDeportes[binding.spinnerDeporte.selectedItemPosition]
            val equipoLocal = equiposFiltrados[binding.spinnerEquipoLocal.selectedItemPosition]
            val equipoVisitante = equiposFiltrados[binding.spinnerEquipoVisitante.selectedItemPosition]
            val estado = estadosPosibles[binding.spinnerEstado.selectedItemPosition]

            val eventoActualizado = evento.copy(
                idDeporte = deporteSeleccionado.idDeporte,
                equipoLocal = equipoLocal.idEquipo,
                equipoVisitante = equipoVisitante.idEquipo,
                fecha_hora = fechaHora,
                ubicacion = ubicacion,
                estadoEvento = estado
            )

            database.child(evento.idEvento).setValue(eventoActualizado)
                .addOnSuccessListener {
                    Toast.makeText(context, "Evento actualizado correctamente", Toast.LENGTH_SHORT).show()
                    cancelarEdicion()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(context, "Error al actualizar: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun confirmarEliminar(evento: Evento) {
        val nombreLocal = equiposMap[evento.equipoLocal] ?: "?"
        val nombreVisitante = equiposMap[evento.equipoVisitante] ?: "?"

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Evento")
            .setMessage("¿Estás seguro de eliminar el evento:\n$nombreLocal vs $nombreVisitante?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarEvento(evento)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarEvento(evento: Evento) {
        database.child(evento.idEvento).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Evento eliminado correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al eliminar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cancelarEdicion() {
        eventoEditando = null
        limpiarCampos()
        binding.btnGuardarEvento.text = "Agregar"
        binding.btnCancelar.visibility = View.GONE
    }

    private fun limpiarCampos() {
        binding.etFechaHora.text?.clear()
        binding.etUbicacion.text?.clear()
        binding.spinnerDeporte.setSelection(0)
        binding.spinnerEstado.setSelection(0)
        binding.tilFechaHora.error = null
        binding.tilUbicacion.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}