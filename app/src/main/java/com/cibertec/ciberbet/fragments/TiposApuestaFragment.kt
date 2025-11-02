package com.cibertec.ciberbet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.ciberbet.adapters.TipoApuestaAdapter
import com.cibertec.ciberbet.databinding.FragmentTiposApuestaBinding
import com.cibertec.ciberbet.models.TipoApuesta
import com.google.firebase.database.*

class TiposApuestaFragment : Fragment() {

    // ViewBinding para acceder a los elementos del layout
    private var _binding: FragmentTiposApuestaBinding? = null
    private val binding get() = _binding!!

    // Referencia a la base de datos de Firebase
    private lateinit var database: DatabaseReference

    // Adaptador del RecyclerView
    private lateinit var tipoAdapter: TipoApuestaAdapter

    // Lista en memoria con los tipos de apuesta obtenidos de Firebase
    private val listaTipos = mutableListOf<TipoApuesta>()

    // Variable auxiliar para saber si se está editando un tipo existente
    private var tipoEditando: TipoApuesta? = null

    // Inflamos el layout del fragment con ViewBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTiposApuestaBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Configuración inicial del fragment: Firebase, RecyclerView y listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa la referencia a la colección "tipos_apuesta"
        database = FirebaseDatabase.getInstance().getReference("tipos_apuesta")

        setupRecyclerView()   // Configura el adaptador y el layout
        cargarTipos()         // Carga los tipos desde Firebase
        setupListeners()      // Configura los botones
    }

    // Configura el RecyclerView y los callbacks de edición/eliminación
    private fun setupRecyclerView() {
        tipoAdapter = TipoApuestaAdapter(
            listaTipos,
            onEditClick = { tipo -> iniciarEdicion(tipo) },       // Callback para editar
            onDeleteClick = { tipo -> confirmarEliminar(tipo) }    // Callback para eliminar
        )

        binding.recyclerTipos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tipoAdapter
        }
    }

    // Configura los botones de guardar y cancelar
    private fun setupListeners() {
        binding.btnGuardarTipo.setOnClickListener {
            if (tipoEditando != null) {
                actualizarTipo()   // Si hay un tipo en edición, actualiza
            } else {
                agregarTipo()      // Si no, agrega uno nuevo
            }
        }

        binding.btnCancelar.setOnClickListener {
            cancelarEdicion()      // Cancela el modo edición
        }
    }

    // Escucha los cambios en Firebase y actualiza la lista local
    private fun cargarTipos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaTipos.clear()
                for (data in snapshot.children) {
                    val tipo = data.getValue(TipoApuesta::class.java)
                    tipo?.let { listaTipos.add(it) }
                }
                tipoAdapter.notifyDataSetChanged() // Refresca el RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Error al cargar tipos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Agrega un nuevo tipo de apuesta a Firebase
    private fun agregarTipo() {
        val nombre = binding.etNombreTipo.text.toString().trim()
        val descripcion = binding.etDescripcionTipo.text.toString().trim()

        // Validación del campo nombre
        if (nombre.isEmpty()) {
            binding.tilNombreTipo.error = "El nombre es obligatorio"
            return
        }
        binding.tilNombreTipo.error = null

        // Genera ID único y crea el objeto
        val id = database.push().key ?: return
        val tipo = TipoApuesta(id, nombre, descripcion)

        // Inserta en Firebase
        database.child(id).setValue(tipo)
            .addOnSuccessListener {
                Toast.makeText(context, "Tipo agregado correctamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al agregar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Inicia el modo edición rellenando los campos del formulario
    private fun iniciarEdicion(tipo: TipoApuesta) {
        tipoEditando = tipo

        binding.etNombreTipo.setText(tipo.nombre)
        binding.etDescripcionTipo.setText(tipo.descripcion)
        binding.btnGuardarTipo.text = "Actualizar"
        binding.btnCancelar.visibility = View.VISIBLE

        // Lleva el scroll al inicio del layout (útil si hay lista larga)
        binding.root.scrollTo(0, 0)
    }

    // Actualiza un tipo de apuesta existente en Firebase
    private fun actualizarTipo() {
        val nombre = binding.etNombreTipo.text.toString().trim()
        val descripcion = binding.etDescripcionTipo.text.toString().trim()

        // Validación del campo nombre
        if (nombre.isEmpty()) {
            binding.tilNombreTipo.error = "El nombre es obligatorio"
            return
        }
        binding.tilNombreTipo.error = null

        tipoEditando?.let { tipo ->
            val tipoActualizado = tipo.copy(
                nombre = nombre,
                descripcion = descripcion
            )

            database.child(tipo.idTipoApuesta).setValue(tipoActualizado)
                .addOnSuccessListener {
                    Toast.makeText(context, "Tipo actualizado correctamente", Toast.LENGTH_SHORT).show()
                    cancelarEdicion()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(context, "Error al actualizar: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Muestra un diálogo de confirmación antes de eliminar un tipo
    private fun confirmarEliminar(tipo: TipoApuesta) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tipo")
            .setMessage("¿Estás seguro de eliminar ${tipo.nombre}?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarTipo(tipo) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Elimina el tipo seleccionado de Firebase
    private fun eliminarTipo(tipo: TipoApuesta) {
        database.child(tipo.idTipoApuesta).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Tipo eliminado correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al eliminar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Cancela la edición actual y resetea el formulario
    private fun cancelarEdicion() {
        tipoEditando = null
        limpiarCampos()
        binding.btnGuardarTipo.text = "Agregar"
        binding.btnCancelar.visibility = View.GONE
    }

    // Limpia los campos de texto y errores
    private fun limpiarCampos() {
        binding.etNombreTipo.text?.clear()
        binding.etDescripcionTipo.text?.clear()
        binding.tilNombreTipo.error = null
    }

    // Libera el binding para evitar fugas de memoria
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
