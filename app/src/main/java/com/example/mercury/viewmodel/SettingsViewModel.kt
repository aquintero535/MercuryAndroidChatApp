package com.example.mercury.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mercury.repo.RepositorioUsuario

class SettingsViewModel : ViewModel() {

    private val repoUsuario = RepositorioUsuario()
    private val liveDataStatus : MutableLiveData<String> = repoUsuario.liveDataStatus

    fun actualizarNombrePerfil(nuevoNombre : String){
        if (nuevoNombre.isNotBlank()){
            repoUsuario.cambiarNombrePerfil(nuevoNombre)
        }
    }

    fun actualizarMensajePerfil(nuevoMensaje : String){
        if (nuevoMensaje.isNotBlank()){
            repoUsuario.cambiarMensajePerfil(nuevoMensaje)
        }
    }

    fun cambiarContrasenia(nuevaContrasenia : String, nuevaContrasenia2 : String){
        if (nuevaContrasenia.isNotBlank() && nuevaContrasenia2.isNotBlank()) {
            if (nuevaContrasenia == nuevaContrasenia2) {
                repoUsuario.cambiarContrasenia(nuevaContrasenia)
            } else {
                liveDataStatus.value = "Las contraseñas no coinciden."
            }
        }
    }

    fun cambiarCorreo(nuevoCorreo : String){
        if (nuevoCorreo.isNotBlank()){
            repoUsuario.cambiarCorreo(nuevoCorreo)
        }
    }

    fun getLiveDataStatus() : LiveData<String> {
        return liveDataStatus
    }

    companion object{
        private const val TAG = "SettingsViewModel"
    }

}