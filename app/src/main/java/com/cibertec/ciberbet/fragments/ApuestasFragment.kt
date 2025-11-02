package com.cibertec.ciberbet.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.ciberbet.databinding.FragmentApuestasBinding
import com.cibertec.ciberbet.models.Cuota
import com.google.firebase.database.*

class ApuestasFragment : Fragment() {

    private var _binding: FragmentApuestasBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private var listaCuotas = mutableListOf<Cuota>()
    private var cuotaSeleccionada: Cuota? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApuestasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance().reference

        // Obtener datos del Bundle
        val idEvento = arguments?.getString("idEvento")
        val equipoLocal = arguments?.getString("equipoLocal")
        val equipoVisitante = arguments?.getString("equipoVisitante")
        val fechaHora = arguments?.getString("fecha_hora")
        val estadoEvento = arguments?.getString("estadoEvento")

        // Mostrar datos del evento
        binding.tvEquipoLocal.text = equipoLocal ?: "Equipo Local"
        binding.tvEquipoVisitante.text = equipoVisitante ?: "Equipo Visitante"
        binding.tvHoraEvento.text = fechaHora ?: "Hora no disponible"

        // Validar si se puede apostar
        if (estadoEvento != "Programado") {
            binding.btnRealizarApuesta.isEnabled = false
            binding.btnConfirmar.isEnabled = false
            binding.etMonto.isEnabled = false
            binding.spinnerCuotas.isEnabled = false
            binding.spinnerCuotas.alpha = 0.4f
            binding.btnRealizarApuesta.alpha = 0.6f
            binding.btnConfirmar.alpha = 0.6f

            Toast.makeText(
                requireContext(),
                "Las apuestas están cerradas para este evento ($estadoEvento)",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // Cargar cuotas solo si el evento sigue programado
            if (idEvento != null) {
                cargarCuotas(idEvento)
            } else {
                Toast.makeText(requireContext(), "Error: ID de evento no válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para el monto
        binding.etMonto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarGanancia()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener para el spinner de cuotas
        binding.spinnerCuotas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaCuotas.isNotEmpty()) {
                    cuotaSeleccionada = listaCuotas[position]
                    actualizarGanancia()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                cuotaSeleccionada = null
            }
        }

        binding.btnRealizarApuesta.setOnClickListener {
            realizarApuesta()
        }

        binding.btnConfirmar.setOnClickListener {
            confirmarApuesta()
        }

        binding.btnCancelar.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun cargarCuotas(eventoId: String) {
        val cuotasRef = database.child("cuotas")

        cuotasRef.orderByChild("idEvento").equalTo(eventoId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaCuotas.clear()
                    for (data in snapshot.children) {
                        val cuota = data.getValue(Cuota::class.java)
                        cuota?.let {
                            if (it.estado == "activa") {
                                listaCuotas.add(it)
                            }
                        }
                    }

                    if (listaCuotas.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "No hay cuotas disponibles para este evento",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        setupSpinnerCuotas()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error al cargar cuotas: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupSpinnerCuotas() {
        // Crear lista de textos para el spinner
        val cuotasTexto = listaCuotas.map { cuota ->
            "${cuota.descripcion} (${cuota.tipoApuesta}) - x${cuota.valorCuota}"
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cuotasTexto
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCuotas.adapter = adapter
    }

    private fun actualizarGanancia() {
        val monto = binding.etMonto.text.toString().toDoubleOrNull() ?: 0.0
        val ganancia = if (cuotaSeleccionada != null) {
            monto * cuotaSeleccionada!!.valorCuota
        } else {
            0.0
        }
        binding.etGanancia.setText("S/. %.2f".format(ganancia))

        // Actualizar saldo después (simulado)
        val saldoActual = 250.0 // Esto deberías obtenerlo de SharedPreferences o Firebase
        val saldoDespues = saldoActual - monto
        binding.tvSaldoActual.text = "Saldo actual: S/. %.2f".format(saldoActual)
        binding.tvSaldoDespues.text = "Saldo después: S/. %.2f".format(saldoDespues)
    }

    private fun realizarApuesta() {
        val monto = binding.etMonto.text.toString().toDoubleOrNull()

        if (monto == null || monto <= 0) {
            Toast.makeText(requireContext(), "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (cuotaSeleccionada == null) {
            Toast.makeText(requireContext(), "Selecciona una cuota primero", Toast.LENGTH_SHORT).show()
            return
        }

        val ganancia = monto * cuotaSeleccionada!!.valorCuota

        Toast.makeText(
            requireContext(),
            "Apuesta preparada:\n${cuotaSeleccionada!!.descripcion}\nMonto: S/. $monto\nGanancia potencial: S/. %.2f\n\nPresiona 'Confirmar' para finalizar".format(ganancia),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun confirmarApuesta() {
        val monto = binding.etMonto.text.toString().toDoubleOrNull()

        if (monto == null || monto <= 0) {
            Toast.makeText(requireContext(), "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (cuotaSeleccionada == null) {
            Toast.makeText(requireContext(), "Selecciona una cuota primero", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener ID del usuario (deberías obtenerlo Firebase Auth)
        val prefs = requireActivity().getSharedPreferences("SesionUsuario", android.content.Context.MODE_PRIVATE)
        val idUsuario = prefs.getString("usuario_id", "") ?: ""

        if (idUsuario.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        val idEvento = arguments?.getString("idEvento") ?: ""
        val ganancia = monto * cuotaSeleccionada!!.valorCuota
        val fechaActual = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

        // Crear la apuesta
        val idApuesta = database.child("apuestas").push().key ?: return
        val apuesta = com.cibertec.ciberbet.models.Apuesta(
            idApuesta = idApuesta,
            idUsuario = idUsuario,
            idEvento = idEvento,
            equipoApostado = cuotaSeleccionada!!.descripcion,
            montoApostado = monto,
            cuotaAplicada = cuotaSeleccionada!!.valorCuota,
            gananciaPotencial = ganancia,
            estado = "pendiente",
            fecha = fechaActual
        )

        // Guardar en Firebase
        database.child("apuestas").child(idApuesta).setValue(apuesta)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Apuesta confirmada exitosamente!\n${cuotaSeleccionada!!.descripcion}\nMonto: S/. $monto\nGanancia potencial: S/. %.2f".format(ganancia),
                    Toast.LENGTH_LONG
                ).show()

                // Limpiar campos
                binding.etMonto.text?.clear()
                binding.spinnerCuotas.setSelection(0)

                // volver al fragment anterior
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Error al guardar la apuesta: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}