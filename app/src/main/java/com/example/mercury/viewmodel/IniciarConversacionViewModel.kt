package com.example.mercury.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.mercury.repo.RepositorioUsuario
import com.example.mercury.model.Usuario
import com.example.mercury.repo.Resource
import java.lang.Boolean

class IniciarConversacionViewModel : ViewModel() {

    private val repo = RepositorioUsuario()
    val usuario: LiveData<Resource<Usuario>> = repo.buscarUsuarioPorCorreo(null)
    val mensajeError = ObservableField<String>()
    val mensajeErrorVisible = ObservableField(Boolean.FALSE)

    fun buscarUsuario(cuenta: String) {
        if (cuenta.isNotEmpty()) {
            when {
                cuenta.contains("@") -> {
                    repo.buscarUsuarioPorCorreo(cuenta)
                }
                cuenta.contains(""".\d""".toRegex()) -> {
                    repo.buscarUsuarioPorTelefono(cuenta)
                }
                else -> {
                    setMensajeError(CORREO_INVALIDO)
                }
            }
        }
    }

    fun setMensajeError(mensajeError: String) {
        this.mensajeError.set(mensajeError)
        mensajeErrorVisible.set(Boolean.TRUE)
        this.mensajeError.notifyChange()
        mensajeErrorVisible.notifyChange()
    }

    companion object {
        private const val TAG = "IniciarConversacionVM"
        private const val CORREO_INVALIDO = "El correo introducido no es válido."
    }
}