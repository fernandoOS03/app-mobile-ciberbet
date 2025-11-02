package com.cibertec.ciberbet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val listaEventos = mutableListOf<Evento>()
    private lateinit var adapter: MatchAdapter

    private val equiposMap = mutableMapOf<String, Equipo>()
    private val deportesMap = mutableMapOf<String, Deporte>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserFragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("SesionUsuario", AppCompatActivity.MODE_PRIVATE)
        val nombreUsuario = prefs.getString("usuario_nombre", "Usuario")

        binding.tvUserName.text = nombreUsuario ?: "Usuario"
        binding.tvGreeting.text = obtenerSaludo()

        // Firebase
        val db = FirebaseDatabase.getInstance()
        eventosRef = db.getReference("eventos_deportivos")
        equiposRef = db.getReference("equipos")
        deportesRef = db.getReference("deportes")

        // Configurar RecyclerView
        adapter = MatchAdapter(listaEventos, equiposMap, deportesMap)
        binding.recyclerViewMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMatches.adapter = adapter

        cargarDatos()
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
                for (data in snapshot.children) {
                    val evento = data.getValue(Evento::class.java)
                    evento?.let { listaEventos.add(it) }
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
