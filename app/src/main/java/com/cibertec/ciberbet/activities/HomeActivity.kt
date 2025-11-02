package com.cibertec.ciberbet.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity

import com.cibertec.ciberbet.R
import com.cibertec.ciberbet.fragments.HomeFragment
import com.cibertec.ciberbet.fragments.PerfilFragment
import com.cibertec.ciberbet.databinding.UserInicioBinding
import com.cibertec.ciberbet.fragments.MisApuestasFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: UserInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar fragment inicial (Home)
        if (savedInstanceState == null) {
            cargarFragment(HomeFragment())
        }

        // Configurar el Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    cargarFragment(HomeFragment())
                    true
                }
                R.id.nav_apuestas -> {
                    cargarFragment(MisApuestasFragment())
                    true
                }
                R.id.nav_perfil -> {
                    cargarFragment(PerfilFragment())
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
}
