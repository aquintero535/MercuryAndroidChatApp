package com.example.mercury.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.mercury.R
import com.example.mercury.databinding.ActivityIniciarConversacionBinding
import com.example.mercury.model.Usuario
import com.example.mercury.repo.Resource
import com.example.mercury.viewmodel.IniciarConversacionViewModel

class IniciarConversacionActivity : AppCompatActivity(), Observer<Resource<Usuario>> {

    private val viewModel: IniciarConversacionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityIniciarConversacionBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Iniciar conversación"
        viewModel.usuario.observe(this, this)
    }

    private fun iniciarConversacion(usuario: Usuario?) {
        val intent = Intent(this@IniciarConversacionActivity, ConversacionActivity::class.java)
        intent.putExtra("uid", usuario!!.uid)
        intent.putExtra("nombre", usuario.nombre)
        intent.putExtra("imagenPerfil", usuario.imagenPerfil)
        finish()
        startActivity(intent)
    }

    override fun onChanged(usuarioResource: Resource<Usuario>) {
        if (usuarioResource.data != null) {
            iniciarConversacion(usuarioResource.data)
        } else {
            viewModel.setMensajeError(usuarioResource.message ?: "Error")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "IniciarConversacion"
    }
}