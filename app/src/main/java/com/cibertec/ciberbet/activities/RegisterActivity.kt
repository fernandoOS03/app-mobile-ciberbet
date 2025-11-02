package com.cibertec.ciberbet.activities

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatActivity

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import com.cibertec.ciberbet.MainActivity
import com.cibertec.ciberbet.models.Usuario
import com.cibertec.ciberbet.data.database.AppApplication
import com.cibertec.ciberbet.databinding.AppActivityRegisterBinding
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: AppActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Este código se ejecuta cuando el usuario hace clic en el botón de login
        binding.textViewLogin.setOnClickListener {

            handleInicioSesion() // Llama a la función que definimos abajo
        }
        binding.buttonRegister.setOnClickListener {
            handleRegister() // Llama a la función que definimos abajo
        }
    }

    private fun validateFields(): Boolean {
        // Obtenemos los valores sin trim() para la validación del formato
        val nombres = binding.editTextFullName.text.toString().trim()
        val dni = binding.editTextDni.text.toString().trim()
        val telefono = binding.editTextPhone.text.toString().trim()
        val correo = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        var isValid = true
        val soloLetras = Regex("\\d")
        val dniPattern = Regex("^\\d{8}$")

        // Se recomienda hacer una función para limpiar todos los errores
        binding.textInputLayoutFullName.error = null
        binding.textInputLayoutDni.error = null
        binding.textInputLayoutPhone.error = null
        binding.textInputLayoutEmail.error = null
        binding.textInputLayoutPassword.error = null

        if (nombres.isEmpty()) {
            binding.textInputLayoutFullName.error = "El nombre completo es obligatorio."
            isValid = false
        } else if(nombres.matches(soloLetras)) {
            binding.textInputLayoutFullName.error = "El nombre no debe contener números."

        }

        if (!dni.matches(dniPattern)) {
            binding.textInputLayoutDni.error = "El DNI debe tener  8 dígitos."
            isValid = false
        }

        if (telefono.length != 9) {
            binding.textInputLayoutPhone.error = "El teléfono debe tener 9 dígitos."
            isValid = false
        } else if (!telefono.startsWith("9")) {
            binding.textInputLayoutPhone.error = "El teléfono debe comenzar con 9."
            isValid = false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.textInputLayoutEmail.error = "Ingrese un formato de correo válido."
            isValid = false
        }

        if (password.length < 8) {
            binding.textInputLayoutPassword.error = "La contraseña debe tener al menos 8 caracteres."
            isValid = false
        }
        return isValid
    }

    private fun handleInicioSesion() {
        val register = Intent(this, MainActivity::class.java)
        startActivity(register)
        finish()

    }

    private fun handleRegister() {
        // Validar campos
        if(!validateFields()){
            Toast.makeText(this, "Por favor corrige los errores del formulario", Toast.LENGTH_LONG).show()
            return
        }

        // Obtener valores de los campos
        val nombres = binding.editTextFullName.text.toString().trim()
        val dni = binding.editTextDni.text.toString().trim()
        val telefono = binding.editTextPhone.text.toString().trim()
        val correo = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (nombres.isEmpty() || dni.isEmpty() || telefono.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val saldoInicial = 500.0

        // Preparar objeto para Room
        val usuarioRoom = Usuario(
            nombres = nombres,
            dni = dni,
            telefono = telefono,
            correo = correo,
            password = password,
            flgEli = false
        )

        // Preparar Firebase
        val database = FirebaseDatabase.getInstance().reference.child("usuarios")
        val idUsuarioFirebase = database.push().key ?: java.util.UUID.randomUUID().toString()

        val usuarioFirebase = mapOf(
            "idUsuario" to idUsuarioFirebase,
            "nombres" to nombres,
            "dni" to dni,
            "telefono" to telefono,
            "correo" to correo,
            "password" to password,
            "flgEli" to false,
            "saldo" to saldoInicial
        )

        // Guardar en Room en hilo secundario
        lifecycleScope.launch(Dispatchers.IO) {
            AppApplication.database.usuarioDao().addUsuario(usuarioRoom)

            // Guardar en Firebase en hilo principal
            launch(Dispatchers.Main) {
                database.child(idUsuarioFirebase).setValue(usuarioFirebase)
                    .addOnSuccessListener {
                        Toast.makeText(this@RegisterActivity, "Registro exitoso. Saldo inicial: S/. $saldoInicial", Toast.LENGTH_SHORT).show()
                        handleInicioSesion()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@RegisterActivity, "Error al registrar en Firebase: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        val prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE)
        prefs.edit()
            .putString("idUsuario", idUsuarioFirebase)
            .putString("nombreUsuario", nombres)
            .apply()

    }

}