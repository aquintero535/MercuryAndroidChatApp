package com.example.mercury.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mercury.model.Contacto
import com.example.mercury.model.Conversacion
import com.example.mercury.repo.InicioSesion
import com.example.mercury.repo.RepositorioContactos
import com.example.mercury.repo.RepositorioConversaciones
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivityViewModel : ViewModel() {
    private val repositorioConversaciones: RepositorioConversaciones = RepositorioConversaciones()
    private val repositorioContactos: RepositorioContactos = RepositorioContactos()
    val listaConversacion: LiveData<List<Conversacion>> = repositorioConversaciones.obtenerListaConversaciones()
    val listaContactos: LiveData<List<Contacto>> = repositorioContactos.obtenerContactos()

    val liveDataNombrePerfil : MutableLiveData<String> = MutableLiveData("")
    val liveDataCorreoPerfil : MutableLiveData<String> = MutableLiveData("")
    val liveDataImagenPerfil : MutableLiveData<String> = MutableLiveData("")


    fun cargarDatosPerfil(){
        liveDataNombrePerfil.value = Firebase.auth.currentUser?.displayName
        liveDataCorreoPerfil.value = Firebase.auth.currentUser?.email
        liveDataImagenPerfil.value = Firebase.auth.currentUser?.photoUrl.toString()
    }

    fun comprobarSesion(): Int {
        return InicioSesion.comprobarSesion()
    }

    fun cerrarSesion() {
        InicioSesion.cerrarSesion()
    }

    fun subirNuevoUsuario() {
        InicioSesion.subirNuevoUsuario()
    }

    companion object{
        private const val TAG = "MainActivityViewModel"
    }
}