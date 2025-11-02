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
import com.cibertec.ciberbet.adapters.CuotaAdapter
import com.cibertec.ciberbet.databinding.FragmentCuotasBinding
import com.cibertec.ciberbet.models.Cuota
import com.cibertec.ciberbet.models.Evento
import com.cibertec.ciberbet.models.TipoApuesta
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CuotasFragment : Fragment() {

    // ViewBinding para acceder a los elementos del layout
    private var _binding: FragmentCuotasBinding? = null
    private val binding get() = _binding!!

    // Referencias a Firebase
    private lateinit var databaseCuotas: DatabaseReference
    private lateinit var databaseEventos: DatabaseReference
    private lateinit var databaseTipos: DatabaseReference
    private lateinit var databaseEquipos: DatabaseReference


    private val equiposMap = mutableMapOf<String, String>() // idEquipo -> nombreEquipo
    private val eventosMap = mutableMapOf<String, String>() // id -> nombre

    // Adaptador del RecyclerView
    private lateinit var cuotaAdapter: CuotaAdapter

    // Listas en memoria
    private val listaCuotas = mutableListOf<Cuota>()
    private val listaEventos = mutableListOf<Evento>()
    private val listaTipos = mutableListOf<TipoApuesta>()

    // Variable auxiliar para saber si se está editando
    private var cuotaEditando: Cuota? = null

    // Inflamos el layout del fragment con ViewBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuotasBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Configuración inicial del fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa las referencias a Firebase
        databaseCuotas = FirebaseDatabase.getInstance().getReference("cuotas")
        databaseEventos = FirebaseDatabase.getInstance().getReference("eventos_deportivos")
        databaseTipos = FirebaseDatabase.getInstance().getReference("tipos_apuesta")
        databaseEquipos = FirebaseDatabase.getInstance().getReference("equipos")


        setupRecyclerView()
        cargarEquipos()
        cargarTipos()
        cargarCuotas()
        setupSpinnerEstado()
        setupListeners()
    }

    // Configura el RecyclerView
    private fun setupRecyclerView() {
        cuotaAdapter = CuotaAdapter(
            listaCuotas,
            eventosMap,
            onEditClick = { cuota -> iniciarEdicion(cuota) },
            onDeleteClick = { cuota -> confirmarEliminar(cuota) }
        )

        binding.recyclerCuotas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cuotaAdapter
        }
    }

    // Configura los botones
    private fun setupListeners() {
        binding.btnGuardarCuota.setOnClickListener {
            if (cuotaEditando != null) {
                actualizarCuota()
            } else {
                agregarCuota()
            }
        }

        binding.btnCancelar.setOnClickListener {
            cancelarEdicion()
        }
    }

    // Carga los eventos desde Firebase para el Spinner
    private fun cargarEventos() {
        databaseEventos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaEventos.clear()
                eventosMap.clear()
                for (data in snapshot.children) {
                    val evento = data.getValue(Evento::class.java)
                    evento?.let {
                        listaEventos.add(it)
                        val nombreLocal = equiposMap[it.equipoLocal] ?: it.equipoLocal
                        val nombreVisitante = equiposMap[it.equipoVisitante] ?: it.equipoVisitante
                        eventosMap[it.idEvento] = "$nombreLocal vs $nombreVisitante"
                    }
                }
                setupSpinnerEventos()
                cuotaAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Carga los equipos desde Firebase para el Spinner
    private fun cargarEquipos() {
        databaseEquipos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                equiposMap.clear()
                for (data in snapshot.children) {
                    val equipo = data.getValue(com.cibertec.ciberbet.models.Equipo::class.java)
                    equipo?.let {
                        equiposMap[it.idEquipo] = it.nombre
                    }
                }

                cargarEventos()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar equipos", Toast.LENGTH_SHORT).show()
            }
        })
    }



    // Carga los tipos de apuesta desde Firebase para el Spinner
    private fun cargarTipos() {
        databaseTipos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaTipos.clear()
                for (data in snapshot.children) {
                    val tipo = data.getValue(TipoApuesta::class.java)
                    tipo?.let { listaTipos.add(it) }
                }
                setupSpinnerTipos()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar tipos de apuesta", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Configura el Spinner de eventos
    private fun setupSpinnerEventos() {
        val nombresEventos = listaEventos.map {
            val nombreLocal = equiposMap[it.equipoLocal] ?: it.equipoLocal
            val nombreVisitante = equiposMap[it.equipoVisitante] ?: it.equipoVisitante
            "$nombreLocal vs $nombreVisitante"
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresEventos
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEvento.adapter = adapter
    }


    // Configura el Spinner de tipos de apuesta
    private fun setupSpinnerTipos() {
        val nombresTipos = listaTipos.map { it.nombre }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresTipos
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipoApuesta.adapter = adapter
    }

    // Configura el Spinner de Estado de apuesta
    private fun setupSpinnerEstado() {
        val estados = listOf("activa", "cerrada")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            estados
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEstado.adapter = adapter
    }


    // Escucha los cambios en Firebase y actualiza la lista
    private fun cargarCuotas() {
        databaseCuotas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCuotas.clear()
                for (data in snapshot.children) {
                    val cuota = data.getValue(Cuota::class.java)
                    cuota?.let { listaCuotas.add(it) }
                }
                cuotaAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Error al cargar cuotas: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Agrega una nueva cuota a Firebase
    private fun agregarCuota() {
        val descripcion = binding.etDescripcionCuota.text.toString().trim()
        val valorStr = binding.etValorCuota.text.toString().trim()
        val estado = binding.spinnerEstado.selectedItem.toString()


        // Validaciones
        if (descripcion.isEmpty()) {
            binding.tilDescripcionCuota.error = "La descripción es obligatoria"
            return
        }
        binding.tilDescripcionCuota.error = null

        if (valorStr.isEmpty()) {
            binding.tilValorCuota.error = "El valor es obligatorio"
            return
        }

        val valor = valorStr.toDoubleOrNull()
        if (valor == null || valor <= 0) {
            binding.tilValorCuota.error = "Ingrese un valor válido mayor a 0"
            return
        }
        binding.tilValorCuota.error = null

        if (listaEventos.isEmpty()) {
            Toast.makeText(context, "No hay eventos disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaTipos.isEmpty()) {
            Toast.makeText(context, "No hay tipos de apuesta disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener los datos seleccionados
        val eventoSeleccionado = listaEventos[binding.spinnerEvento.selectedItemPosition]
        val tipoSeleccionado = listaTipos[binding.spinnerTipoApuesta.selectedItemPosition]

        // Generar ID único y crear objeto
        val id = databaseCuotas.push().key ?: return
        val cuota = Cuota(
            idCuota = id,
            idEvento = eventoSeleccionado.idEvento,
            tipoApuesta = tipoSeleccionado.nombre,
            descripcion = descripcion,
            valorCuota = valor,
            estado = estado
        )

        // Insertar en Firebase
        databaseCuotas.child(id).setValue(cuota)
            .addOnSuccessListener {
                Toast.makeText(context, "Cuota agregada correctamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al agregar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Inicia el modo edición
    private fun iniciarEdicion(cuota: Cuota) {
        cuotaEditando = cuota

        binding.etDescripcionCuota.setText(cuota.descripcion)
        binding.etValorCuota.setText(cuota.valorCuota.toString())
        val estadoIndex = if (cuota.estado == "cerrada") 1 else 0
        binding.spinnerEstado.setSelection(estadoIndex)


        // Seleccionar el evento en el spinner
        val eventoIndex = listaEventos.indexOfFirst { it.idEvento == cuota.idEvento }
        if (eventoIndex != -1) {
            binding.spinnerEvento.setSelection(eventoIndex)
        }

        // Seleccionar el tipo en el spinner
        val tipoIndex = listaTipos.indexOfFirst { it.nombre == cuota.tipoApuesta }
        if (tipoIndex != -1) {
            binding.spinnerTipoApuesta.setSelection(tipoIndex)
        }

        binding.btnGuardarCuota.text = "Actualizar"
        binding.btnCancelar.visibility = View.VISIBLE

        // Scroll al inicio
        binding.root.scrollTo(0, 0)
    }

    // Actualiza una cuota existente en Firebase
    private fun actualizarCuota() {
        val descripcion = binding.etDescripcionCuota.text.toString().trim()
        val valorStr = binding.etValorCuota.text.toString().trim()
        val estado = binding.spinnerEstado.selectedItem.toString()


        // Validaciones
        if (descripcion.isEmpty()) {
            binding.tilDescripcionCuota.error = "La descripción es obligatoria"
            return
        }
        binding.tilDescripcionCuota.error = null

        if (valorStr.isEmpty()) {
            binding.tilValorCuota.error = "El valor es obligatorio"
            return
        }

        val valor = valorStr.toDoubleOrNull()
        if (valor == null || valor <= 0) {
            binding.tilValorCuota.error = "Ingrese un valor válido mayor a 0"
            return
        }
        binding.tilValorCuota.error = null

        cuotaEditando?.let { cuota ->
            val eventoSeleccionado = listaEventos[binding.spinnerEvento.selectedItemPosition]
            val tipoSeleccionado = listaTipos[binding.spinnerTipoApuesta.selectedItemPosition]

            val cuotaActualizada = Cuota(
                idCuota = cuota.idCuota,
                idEvento = eventoSeleccionado.idEvento,
                tipoApuesta = tipoSeleccionado.nombre,
                descripcion = descripcion,
                valorCuota = valor,
                estado = estado
            )

            databaseCuotas.child(cuota.idCuota).setValue(cuotaActualizada)
                .addOnSuccessListener {
                    Toast.makeText(context, "Cuota actualizada correctamente", Toast.LENGTH_SHORT).show()
                    cancelarEdicion()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(context, "Error al actualizar: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Muestra un diálogo de confirmación antes de eliminar
    private fun confirmarEliminar(cuota: Cuota) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Cuota")
            .setMessage("¿Estás seguro de eliminar la cuota '${cuota.descripcion}'?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarCuota(cuota) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Elimina la cuota seleccionada de Firebase
    private fun eliminarCuota(cuota: Cuota) {
        databaseCuotas.child(cuota.idCuota).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Cuota eliminada correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al eliminar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Cancela la edición actual y resetea el formulario
    private fun cancelarEdicion() {
        cuotaEditando = null
        limpiarCampos()
        binding.btnGuardarCuota.text = "Agregar"
        binding.btnCancelar.visibility = View.GONE
    }

    // Limpia los campos de texto y errores
    private fun limpiarCampos() {
        binding.etDescripcionCuota.text?.clear()
        binding.etValorCuota.text?.clear()
        binding.spinnerEstado.setSelection(0) // activa
        binding.tilDescripcionCuota.error = null
        binding.tilValorCuota.error = null

        // Resetear spinners a primera posición
        if (listaEventos.isNotEmpty()) {
            binding.spinnerEvento.setSelection(0)
        }
        if (listaTipos.isNotEmpty()) {
            binding.spinnerTipoApuesta.setSelection(0)
        }
    }

    // Libera el binding para evitar fugas de memoria
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}