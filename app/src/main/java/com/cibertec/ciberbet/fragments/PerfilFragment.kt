package com.cibertec.ciberbet.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cibertec.ciberbet.MainActivity
/*
import com.cibertec.ciberbet.HistorialApuestasActivity*/
import com.cibertec.ciberbet.data.database.AppApplication
import com.cibertec.ciberbet.databinding.UserFragmentPerfilBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilFragment : Fragment() {

    private var _binding: UserFragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserFragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar datos guardados del usuario
        val prefs = requireContext().getSharedPreferences("SesionUsuario", AppCompatActivity.MODE_PRIVATE)
        val nombreUsuario = prefs.getString("usuario_nombre", "Usuario")
        val correoUsuario = prefs.getString("usuario_email", "example@email.com")

        binding.tvProfileName.text = nombreUsuario
        binding.tvProfileEmail.text = correoUsuario

        // Botón eliminar cuenta con confirmación
        binding.btnEliminarCuenta.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        // Botón configuración
        binding.btnConfiguracion.setOnClickListener {
            Toast.makeText(requireContext(), "Función para actualizar perfil en desarrollo", Toast.LENGTH_SHORT).show()
        }
/*
        // Botón mostrar apuestas
        binding.btnMostrarApuestas.setOnClickListener {
            val intent = Intent(requireContext(), HistorialApuestasActivity::class.java)
            startActivity(intent)
        }*/

        // Botón configuración
        binding.btnConfiguracion.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.cibertec.ciberbet.R.id.fragment_container, PerfilEditFragment())
                .addToBackStack(null)
                .commit()
         }

    }

    private fun eliminarCuenta() {
        val prefs = requireContext().getSharedPreferences("SesionUsuario", AppCompatActivity.MODE_PRIVATE)
        val idUsuario = prefs.getInt("usuario_id", -1)

        if (idUsuario != -1) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    AppApplication.database.usuarioDao().deleteUsuario(idUsuario)
                }

                prefs.edit().clear().apply()
                Toast.makeText(requireContext(), "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()

                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        } else {
            Toast.makeText(requireContext(), "Error al obtener el ID del usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCuenta()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
