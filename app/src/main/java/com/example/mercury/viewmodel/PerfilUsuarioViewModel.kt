package com.example.mercury.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.mercury.model.Usuario
import com.example.mercury.repo.InicioSesion
import com.example.mercury.repo.Resource
import com.example.mercury.repo.RepositorioUsuario
import com.google.firebase.auth.FirebaseAuth

class PerfilUsuarioViewModel : ViewModel() {

    private val repositorioUsuario = RepositorioUsuario()
    private val uidUsuario: String = FirebaseAuth.getInstance().uid!!
    val liveDataUsuario : LiveData<Resource<Usuario>> = repositorioUsuario.obtenerDatosUsuario(uidUsuario)
    val liveDataStatus = repositorioUsuario.liveDataStatus

    fun subirImagenPerfil(uriImagen: Uri?) {
        repositorioUsuario.subirImagenPerfil(uriImagen)
    }

    fun cambiarMensajePerfil(nuevoMensaje: String?) {
        repositorioUsuario.cambiarMensajePerfil(nuevoMensaje)
    }

    fun cerrarSesion() {
        InicioSesion.cerrarSesion()
    }

    companion object {
        private const val TAG = "PerfilUsuarioViewModel"
    }

}