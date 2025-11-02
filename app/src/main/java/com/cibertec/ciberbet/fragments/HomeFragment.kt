package com.cibertec.ciberbet.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.ciberbet.adapters.MatchAdapter
import com.cibertec.ciberbet.databinding.UserFragmentHomeBinding
import com.cibertec.ciberbet.models.Deporte
import com.cibertec.ciberbet.models.Equipo
import com.cibertec.ciberbet.models.Evento
import com.google.firebase.database.*
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: UserFragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventosRef: DatabaseReference
    private lateinit var equiposRef: DatabaseReference
    private lateinit var deportesRef: DatabaseReference
    private lateinit var usuarioRef: DatabaseReference

    private val listaEventos = mutableListOf<Evento>()
    private val listaFiltrada = mutableListOf<Evento>()
    private lateinit var adapter: MatchAdapter

    private val equiposMap = mutableMapOf<String, Equipo>()
    private val deportesMap = mutableMapOf<String, Deporte>()

    private var idUsuario: String = ""
    private var saldoActual: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserFragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("SesionUsuario", AppCompatActivity.MODE_PRIVATE)
        idUsuario = prefs.getString("idUsuario", "") ?: ""
        val nombreUsuario = prefs.getString("nombreUsuario", "Usuario") ?: "Usuario"
        binding.tvUserName.text = nombreUsuario
        binding.tvGreeting.text = obtenerSaludo()

        inicializarSaldo() // <-- inicializamos saldo en tiempo real

        // Firebase
        val db = FirebaseDatabase.getInstance()
        eventosRef = db.getReference("eventos_deportivos")
        equiposRef = db.getReference("equipos")
        deportesRef = db.getReference("deportes")

        // RecyclerView
        adapter = MatchAdapter(listaFiltrada, equiposMap, deportesMap) { evento ->
            val bundle = Bundle().apply {
                putString("idEvento", evento.idEvento)
                putString("idUsuario", idUsuario)
                putString("equipoLocal", equiposMap[evento.equipoLocal]?.nombre ?: "Equipo Local")
                putString("equipoVisitante", equiposMap[evento.equipoVisitante]?.nombre ?: "Equipo Visitante")
                putString("fecha_hora", evento.fecha_hora)
                putString("ubicacion", evento.ubicacion)
                putString("estadoEvento", evento.estadoEvento)
                putInt("resultadoLocal", evento.resultadoLocal)
                putInt("resultadoVisitante", evento.resultadoVisitante)
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.cibertec.ciberbet.R.id.fragment_container, ApuestasFragment().apply {
                    arguments = bundle
                })
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerViewMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMatches.adapter = adapter

        binding.btnFutbol.setOnClickListener {
            actualizarBotones(it)
            filtrarPorDeporte("all")
        }

        binding.btnVoley.setOnClickListener {
            actualizarBotones(it)
            filtrarPorDeporte("Voley")
        }

        binding.btnBasket.setOnClickListener {
            actualizarBotones(it)
            filtrarPorDeporte("Futbol")
        }

        cargarDatos()
    }

    private fun inicializarSaldo() {
        val db = FirebaseDatabase.getInstance()
        usuarioRef = db.getReference("usuarios").child(idUsuario)

        usuarioRef.child("saldo").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                saldoActual = snapshot.getValue(Double::class.java) ?: 0.0
                binding.tvBalanceAmount.text = "S/. %.2f".format(saldoActual)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error
            }
        })

        // Para mostrar saldo después de ingresar un monto
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarSaldoPreview(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun actualizarSaldoPreview(montoStr: String) {
        val monto = montoStr.toDoubleOrNull() ?: 0.0
        val saldoDespues = saldoActual - monto
        binding.tvBalanceAmount.setTextColor(if (saldoDespues < 0) Color.RED else Color.BLACK)
        binding.tvBalanceAmount.text = "S/. %.2f".format(saldoDespues)
    }

    fun confirmarApuesta(monto: Double) {
        if (saldoActual >= monto) {
            val nuevoSaldo = saldoActual - monto
            usuarioRef.child("saldo").setValue(nuevoSaldo)
                .addOnSuccessListener {
                    saldoActual = nuevoSaldo
                    binding.tvBalanceAmount.text = "S/. %.2f".format(saldoActual)
                }
                .addOnFailureListener {
                    // Error al actualizar saldo
                }
        }
    }

    private fun cargarDatos() {
        deportesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                deportesMap.clear()
                for (data in snapshot.children) {
                    val dep = data.getValue(Deporte::class.java)
                    dep?.let { deportesMap[it.idDeporte] = it }
                }
                cargarEquipos()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarEquipos() {
        equiposRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                equiposMap.clear()
                for (data in snapshot.children) {
                    val eq = data.getValue(Equipo::class.java)
                    eq?.let { equiposMap[it.idEquipo] = it }
                }
                cargarEventos()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarEventos() {
        eventosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaEventos.clear()
                listaFiltrada.clear()
                for (data in snapshot.children) {
                    val evento = data.getValue(Evento::class.java)
                    evento?.let { listaEventos.add(it) }
                }
                listaFiltrada.addAll(listaEventos)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun obtenerSaludo(): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hora) {
            in 0..11 -> "Buenos días,"
            in 12..17 -> "Buenas tardes,"
            else -> "Buenas noches,"
        }
    }

    private fun filtrarPorDeporte(nombreDeporte: String) {
        val deporteSeleccionado = deportesMap.values.find {
            it.nombre.equals(nombreDeporte, ignoreCase = true)
        }

        listaFiltrada.clear()
        if (deporteSeleccionado != null) {
            val idDep = deporteSeleccionado.idDeporte
            val eventosFiltrados = listaEventos.filter { it.idDeporte == idDep }
            listaFiltrada.addAll(eventosFiltrados)
        } else {
            listaFiltrada.addAll(listaEventos)
        }
        adapter.notifyDataSetChanged()
    }

    private fun actualizarBotones(activo: View) {
        val botones = listOf(binding.btnFutbol, binding.btnVoley, binding.btnBasket)
        for (btn in botones) {
            btn.setBackgroundColor(Color.parseColor("#EEEEEE"))
            btn.setTextColor(Color.parseColor("#757575"))
        }
        (activo as Button).setBackgroundColor(Color.parseColor("#212121"))
        activo.setTextColor(Color.WHITE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
