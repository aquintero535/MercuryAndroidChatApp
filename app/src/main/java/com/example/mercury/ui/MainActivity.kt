package com.example.mercury.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.mercury.R
import com.example.mercury.databinding.ActivityMainBinding
import com.example.mercury.databinding.NavHeaderMainBinding
import com.example.mercury.repo.InicioSesion
import com.example.mercury.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val navBinding = DataBindingUtil.inflate<NavHeaderMainBinding>(
                layoutInflater, R.layout.nav_header_main, binding.navView, false)
        binding.navView.addHeaderView(navBinding.root)
        navBinding.viewModel = viewModel
        navBinding.lifecycleOwner = this
        setContentView(binding.root)
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = binding.drawerLayout

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
                R.id.nav_conversaciones, R.id.nav_contactos, R.id.nav_perfil)
                .setDrawerLayout(drawer)
                .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration)
        NavigationUI.setupWithNavController(binding.navView, navController)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings){
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp())
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.comprobarSesion() == InicioSesion.SESION_CERRADA) {
            startActivityForResult(InicioSesion.iniciarSesionIntent, InicioSesion.RC_SIGN_IN)
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.comprobarSesion() == InicioSesion.SESION_ABIERTA) {
            InicioSesion.cambiarEstadoEnLinea(true)
        }
    }

    override fun onPause() {
        super.onPause()
        InicioSesion.cambiarEstadoEnLinea(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == InicioSesion.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                viewModel.subirNuevoUsuario()
                finish()
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Error al iniciar sesión.", Toast.LENGTH_SHORT).show()
                onStart()
            }
        }
    }

    companion object{
        private const val TAG = "MainActivity"

        @JvmStatic
        @BindingAdapter("cargarImagenPerfilNavMenu")
        fun cargarImagenPerfil(view : ImageView, uriImagen : String){
            Glide.with(view.context).load(uriImagen)
                    .override(130, 130).circleCrop().into(view)
        }
    }
}