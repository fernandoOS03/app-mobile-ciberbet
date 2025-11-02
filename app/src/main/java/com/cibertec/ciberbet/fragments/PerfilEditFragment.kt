package com.cibertec.ciberbet.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cibertec.ciberbet.MainActivity
import com.cibertec.ciberbet.data.database.AppApplication
import com.cibertec.ciberbet.databinding.UserFragmentPerfilEditBinding
import com.cibertec.ciberbet.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilEditFragment : Fragment() {

    private var _binding: UserFragmentPerfilEditBinding? = null
    private val binding get() = _binding!!

    private var usuarioActual: Usuario? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserFragmentPerfilEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("SesionUsuario", AppCompatActivity.MODE_PRIVATE)
        val usuarioId = prefs.getInt("usuario_id", -1)

        if (usuarioId == -1) {
            Toast.makeText(requireContext(), "Error al obtener el usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 Cargar datos reales desde Room
        lifecycleScope.launch {
            usuarioActual = withContext(Dispatchers.IO) {
                AppApplication.database.usuarioDao().getUsuarioPorId(usuarioId)
            }

            usuarioActual?.let {
                binding.editTextFullName.setText(it.nombres)
                binding.editTextDni.setText(it.dni)
                binding.editTextPhone.setText(it.telefono)
                binding.editTextEmail.setText(it.correo)
            }
        }

        // 🔹 Guardar cambios
        binding.buttonSave.setOnClickListener {
            val nombre = binding.editTextFullName.text.toString().trim()
            val dni = binding.editTextDni.text.toString().trim()
            val telefono = binding.editTextPhone.text.toString().trim()
            val correo = binding.editTextEmail.text.toString().trim()

            if (nombre.isEmpty() || dni.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            usuarioActual?.let { usuario ->
                usuario.nombres = nombre
                usuario.dni = dni
                usuario.telefono = telefono
                usuario.correo = correo

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        AppApplication.database.usuarioDao().updateUsuario(usuario)
                    }

                    // 🔹 Limpiar sesión actual
                    prefs.edit().clear().apply()

                    Toast.makeText(requireContext(), "Datos actualizados. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()

                    // 🔹 Redirigir al login
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }

        // 🔹 Cancelar cambios
        binding.buttonCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 🔹 Cambiar foto (opcional)
        binding.textChangePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidad de cambiar foto próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
