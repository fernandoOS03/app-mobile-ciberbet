package com.cibertec.ciberbet.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cibertec.ciberbet.MainActivity
import com.cibertec.ciberbet.R
import com.cibertec.ciberbet.databinding.UserInicioBinding
import com.cibertec.ciberbet.fragments.HomeFragment
import com.cibertec.ciberbet.fragments.PerfilFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: UserInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener idUsuario desde intent (viene de Login o Register)
        val idUsuario = intent.getStringExtra("idUsuario") ?: ""

        // Cargar fragmento Home inicial con idUsuario
        if (savedInstanceState == null) {
            val homeFragment = HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("idUsuario", idUsuario)
                }
            }
            cargarFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.btnNavHome
        }

        // Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnNavProfile -> {
                    cargarFragment(PerfilFragment())
                    true
                }
                R.id.btnNavHome -> {
                    val homeFragment = HomeFragment().apply {
                        arguments = Bundle().apply {
                            putString("idUsuario", idUsuario)
                        }
                    }
                    cargarFragment(homeFragment)
                    true
                }
                R.id.btnNavLogout -> {
                    cerrarSesion()
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun cerrarSesion() {
        // Limpiar sesión (opcional, si todavía usas SharedPreferences)
        val prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Volver a Login
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
