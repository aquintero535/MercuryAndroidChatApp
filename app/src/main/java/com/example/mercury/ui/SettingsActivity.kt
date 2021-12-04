package com.example.mercury.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.mercury.R
import com.example.mercury.databinding.FragmentCambiarContraseniaBinding
import com.example.mercury.databinding.FragmentCambiarCorreoBinding
import com.example.mercury.repo.InicioSesion
import com.example.mercury.viewmodel.SettingsViewModel
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    val viewModel : SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Opciones"
        val frameLayout : FrameLayout = findViewById(R.id.settings)
        viewModel.getLiveDataStatus().observe(this) {
            Snackbar.make(frameLayout, it, Snackbar.LENGTH_LONG).show()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val viewModel : SettingsViewModel by activityViewModels()
        private val cambioNombrePerfil = Preference.OnPreferenceChangeListener {
            _: Preference, any: Any ->
            viewModel.actualizarNombrePerfil(any as String)
            true
        }
        private val cambioMensajePerfil = Preference.OnPreferenceChangeListener {
            _: Preference, any: Any ->
            viewModel.actualizarMensajePerfil(any as String)
            true
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val nombrePerfil : EditTextPreference? = findPreference("display_name")
            val mensajePerfil : EditTextPreference? = findPreference("profile_message")
            val opcionesInicioSesion : Preference? = findPreference("auth_settings")
            nombrePerfil?.onPreferenceChangeListener = cambioNombrePerfil
            mensajePerfil?.onPreferenceChangeListener = cambioMensajePerfil
            opcionesInicioSesion?.setOnPreferenceClickListener {
                startActivityForResult(InicioSesion.iniciarSesionIntent, InicioSesion.RC_SIGN_IN)
                true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == InicioSesion.RC_SIGN_IN && resultCode == Activity.RESULT_OK){
                cambiarFragmento(activity?.supportFragmentManager, AuthSettingsFragment())
            }
        }
    }

    class AuthSettingsFragment : PreferenceFragmentCompat(){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.auth_preferences, rootKey)
            val cambiarCorreo : Preference? = findPreference("change_email")
            val cambiarContrasenia : Preference? = findPreference("change_password")
            cambiarCorreo?.setOnPreferenceClickListener {
                cambiarFragmento(activity?.supportFragmentManager, CambiarCorreoFragment())
                true
            }
            cambiarContrasenia?.setOnPreferenceClickListener {
                cambiarFragmento(activity?.supportFragmentManager, CambiarContraseniaFragment())
                true
            }
        }
    }

    class CambiarCorreoFragment : Fragment(){
        val viewModel : SettingsViewModel by activityViewModels()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val binding = FragmentCambiarCorreoBinding.inflate(layoutInflater)
            binding.lifecycleOwner = this
            binding.viewModel = viewModel
            return binding.root
        }
    }

    class CambiarContraseniaFragment : Fragment(){
        val viewModel : SettingsViewModel by activityViewModels()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val binding = FragmentCambiarContraseniaBinding.inflate(layoutInflater)
            binding.lifecycleOwner = this
            binding.viewModel = viewModel
            return binding.root
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "SettingsActivity"

        private fun cambiarFragmento(fragmentManager: FragmentManager?, fragment: Fragment){
            fragmentManager?.
                    beginTransaction()?.
                    setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)?.
                    replace(R.id.settings, fragment)?.
                    addToBackStack(null)?.
                    commit()
        }
    }
}