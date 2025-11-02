package com.cibertec.ciberbet.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
/*
import com.cibertec.ciberbet.ApuestasActivity*/
import com.cibertec.ciberbet.MainActivity
import com.cibertec.ciberbet.models.Match
import com.cibertec.ciberbet.adapters.MatchAdapter
import com.cibertec.ciberbet.databinding.FragmentHomeBinding
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("SesionUsuario", AppCompatActivity.MODE_PRIVATE)
        val nombreUsuario = prefs.getString("usuario_nombre", "Usuario")

        // --- Mostrar los datos en pantalla ---
        binding.tvUserName.text = nombreUsuario ?: "Usuario"
        binding.tvGreeting.text = obtenerSaludo()

        // 1. Preparamos datos de ejemplo para la lista
        val sampleMatches = listOf(
            Match("Chelsea", "Leicester C", "1 : 2", "49:30", "Premier League"),
            Match("Man. United", "Arsenal", "0 : 0", "25:10", "Premier League"),
            Match("Liverpool", "Man. City", "3 : 1", "78:55", "Premier League"),
            Match("Alianza Lima", "Universitario", "2 : 1", "Finalizado", "Liga 1")
        )
/*
        // 2. Creamos el adaptador
        val adapter = MatchAdapter(sampleMatches) { match ->
            val intent = Intent(requireContext(), ApuestasActivity::class.java)
            startActivity(intent)
        }
*/
        // 3. Configuramos el RecyclerView
        binding.recyclerViewMatches.layoutManager = LinearLayoutManager(requireContext())
 /*       binding.recyclerViewMatches.adapter = adapter*/

        // ⚠️ Ya NO usamos botones de navegación aquí
        // El cambio de fragment se hace desde el BottomNavigationView en HomeActivity
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
