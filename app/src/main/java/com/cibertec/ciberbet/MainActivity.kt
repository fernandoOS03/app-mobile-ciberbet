package com.cibertec.ciberbet

// --- IMPORTACIONES NECESARIAS ---
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.ciberbet.activities.AdminActivity
import com.cibertec.ciberbet.activities.HomeActivity
import com.cibertec.ciberbet.activities.RegisterActivity
import com.cibertec.ciberbet.data.database.AppApplication
import com.cibertec.ciberbet.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ---------------------------------

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Este código se ejecuta cuando el usuario hace clic en el botón de login
        binding.buttonLogin.setOnClickListener {
            handleLogin() // Llama a la función que definimos abajo
        }
        binding.textViewRegister.setOnClickListener {
            handleRegister()
        }
    }

    /**
     * Esta función se encarga de verificar el email y la contraseña
     * y navegar a la siguiente pantalla si son correctos.
     */

    private fun handleRegister() {
        val register = Intent(this, RegisterActivity::class.java)
        startActivity(register)
        finish()
    }

    private fun handleLogin() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            if(email == "admin@ciberbet.com" && password == "admin321"){
                Toast.makeText(this, "¡Bienvenido administrador Ciberbet!", Toast.LENGTH_SHORT).show()

                //  pantalla para el administrador
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
                finish() // Cierra la pantalla de login para que no se pueda volver atrás
            }
            else{
                // Ejecutamos en un hilo de corrutina
                lifecycleScope.launch {
                    val usuario = withContext(Dispatchers.IO) {
                        AppApplication.database.usuarioDao().getUsuario(email, password)
                    }

                    if (usuario != null) {
                        Toast.makeText(this@MainActivity, "Bienvenido ${usuario.nombres}", Toast.LENGTH_SHORT).show()

                        val prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE)
                        prefs.edit()
                            .putString("idUsuario", usuario.id.toString())       // antes era usuario_id
                            .putString("nombreUsuario", usuario.nombres)        // antes era usuario_nombre
                            .putString("emailUsuario", usuario.correo)
                            .apply()

                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    /*private fun handleLogin() {
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()

        // cuenta para iniciar secion
        if (email == "admin@ciberbet.com" && password == "admin321") {
            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

            //  pantalla para el administrador
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
            finish() // Cierra la pantalla de login para que no se pueda volver atrás
            }
        else if (email == "" && password ==""){
            Toast.makeText(this, "¡Bienvenido Usuario!", Toast.LENGTH_SHORT).show()

            //  pantalla para el usuario
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Cierra la pantalla de login para que no se pueda volver atrás
        }
        else {
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
        }
    }*/
}