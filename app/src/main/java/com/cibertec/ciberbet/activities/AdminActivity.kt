package com.cibertec.ciberbet.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.cibertec.ciberbet.R
import com.cibertec.ciberbet.databinding.AdminActivityAdminBinding
import com.cibertec.ciberbet.fragments.CuotasContainerFragment
import com.cibertec.ciberbet.fragments.CuotasFragment
import com.cibertec.ciberbet.fragments.DeportesFragment
import com.cibertec.ciberbet.fragments.EquiposFragment
import com.cibertec.ciberbet.fragments.EventosFragment

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Panel de Administración"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Cargar fragment inicial (Deportes)
        if (savedInstanceState == null) {
            cargarFragment(DeportesFragment())
            binding.bottomNavigationAdmin.selectedItemId = R.id.nav_admin_deportes
        }

        // Configurar el Bottom Navigation para Admin
        binding.bottomNavigationAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_deportes -> {
                    cargarFragment(DeportesFragment())
                    true
                }
                R.id.nav_admin_equipos -> {
                    cargarFragment(EquiposFragment())
                    true
                }
                R.id.nav_admin_eventos -> {
                    cargarFragment(EventosFragment())
                    true
                }
                R.id.nav_admin_cuotas -> {
                    cargarFragment(CuotasContainerFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}