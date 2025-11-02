package com.cibertec.ciberbet.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cibertec.ciberbet.databinding.FragmentApuestasBinding
import com.cibertec.ciberbet.models.Apuesta
import com.cibertec.ciberbet.models.Cuota
import com.google.firebase.database.*

class ApuestasFragment : Fragment() {

    private var _binding: FragmentApuestasBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private var listaCuotas = mutableListOf<Cuota>()
    private var cuotaSeleccionada: Cuota? = null
    private var saldoActual = 0.0
    private var idUsuario: String = ""

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

        // Obtener idUsuario desde SharedPreferences
        val prefs = requireActivity().getSharedPreferences("SesionUsuario", AppCompatActivity.MODE_PRIVATE)
        idUsuario = prefs.getString("idUsuario", "") ?: ""
        if (idUsuario.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance().reference

        // Mostrar datos del evento desde el Bundle
        val idEvento = arguments?.getString("idEvento")
        val equipoLocal = arguments?.getString("equipoLocal") ?: "Equipo Local"
        val equipoVisitante = arguments?.getString("equipoVisitante") ?: "Equipo Visitante"
        val fechaHora = arguments?.getString("fecha_hora") ?: "Hora no disponible"
        val estadoEvento = arguments?.getString("estadoEvento") ?: "Desconocido"

        binding.tvEquipoLocal.text = equipoLocal
        binding.tvEquipoVisitante.text = equipoVisitante
        binding.tvHoraEvento.text = fechaHora

        // Validar si se puede apostar
        if (estadoEvento != "Programado") {
            binding.btnConfirmar.isEnabled = false
            binding.etMonto.isEnabled = false
            binding.spinnerCuotas.isEnabled = false
            binding.spinnerCuotas.alpha = 0.4f
            binding.btnConfirmar.alpha = 0.6f

            Toast.makeText(
                requireContext(),
                "Las apuestas están cerradas para este evento ($estadoEvento)",
                Toast.LENGTH_LONG
            ).show()
        } else {
            if (idEvento != null) cargarCuotas(idEvento)
            else Toast.makeText(requireContext(), "Error: ID de evento no válido", Toast.LENGTH_SHORT).show()
        }

        // Cargar saldo real del usuario
        cargarSaldoUsuario()

        // Listeners
        binding.etMonto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { actualizarGanancia() }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.spinnerCuotas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaCuotas.isNotEmpty()) {
                    cuotaSeleccionada = listaCuotas[position]
                    actualizarGanancia()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { cuotaSeleccionada = null }
        }
        binding.btnConfirmar.setOnClickListener { confirmarApuesta() }
        binding.btnCancelar.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
    }

    private fun cargarCuotas(eventoId: String) {
        database.child("cuotas").orderByChild("idEvento").equalTo(eventoId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaCuotas.clear()
                    for (data in snapshot.children) {
                        val cuota = data.getValue(Cuota::class.java)
                        cuota?.let { if (it.estado == "activa") listaCuotas.add(it) }
                    }
                    if (listaCuotas.isEmpty()) Toast.makeText(requireContext(), "No hay cuotas disponibles", Toast.LENGTH_SHORT).show()
                    else setupSpinnerCuotas()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error al cargar cuotas: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupSpinnerCuotas() {
        val cuotasTexto = listaCuotas.map { "${it.descripcion} (${it.tipoApuesta}) - x${it.valorCuota}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cuotasTexto)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCuotas.adapter = adapter
    }

    private fun cargarSaldoUsuario() {
        database.child("usuarios").child(idUsuario).child("saldo")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    saldoActual = snapshot.getValue(Double::class.java) ?: 500.0
                    binding.tvSaldoActual.text = "Saldo actual: S/. %.2f".format(saldoActual)
                    actualizarGanancia()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun actualizarGanancia() {
        val monto = binding.etMonto.text.toString().toDoubleOrNull() ?: 0.0
        val ganancia = cuotaSeleccionada?.let { monto * it.valorCuota } ?: 0.0
        binding.etGanancia.setText("S/. %.2f".format(ganancia))

        val saldoDespues = saldoActual - monto
        binding.tvSaldoDespues.setTextColor(if (saldoDespues < 0) Color.RED else Color.BLACK)
        binding.tvSaldoDespues.text = "Saldo después: S/. %.2f".format(saldoDespues)
    }

    private fun confirmarApuesta() {
        val monto = binding.etMonto.text.toString().toDoubleOrNull()
        if (monto == null || monto <= 0 || cuotaSeleccionada == null || monto > saldoActual) return

        val idEvento = arguments?.getString("idEvento") ?: ""
        val ganancia = monto * cuotaSeleccionada!!.valorCuota
        val fechaActual = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val idApuesta = database.child("apuestas").push().key ?: return

        val apuesta = Apuesta(
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

        database.child("apuestas").child(idApuesta).setValue(apuesta).addOnSuccessListener {
            saldoActual -= monto
            database.child("usuarios").child(idUsuario).child("saldo").setValue(saldoActual)
            Toast.makeText(
                requireContext(),
                "Apuesta confirmada!\nMonto: S/. $monto\nGanancia potencial: S/. %.2f".format(ganancia),
                Toast.LENGTH_LONG
            ).show()
            binding.etMonto.text?.clear()
            binding.spinnerCuotas.setSelection(0)
            requireActivity().supportFragmentManager.popBackStack()
        }.addOnFailureListener { error ->
            Toast.makeText(requireContext(), "Error al guardar la apuesta: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
