package com.example.mercury.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mercury.model.*
import com.example.mercury.repo.RepositorioContactos
import com.example.mercury.repo.RepositorioMensajes
import com.example.mercury.repo.RepositorioUsuario
import com.example.mercury.repo.Resource
import com.example.mercury.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ServerValue
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ConversacionViewModel(usuario2Uid: String, nombreUsuario2: String?,
                            imagenPerfil: String?) : ViewModel() {

    private val repoMensajes: RepositorioMensajes
    private val repoUsuario : RepositorioUsuario
    private val liveDataMensajes: LiveData<Resource<List<Mensaje.Recibir>>>
    val liveDataUsuario : LiveData<Resource<Usuario>>

    val conversacion: Conversacion
    val strCampoMensaje: MutableLiveData<String> = MutableLiveData("")
    val strFechaConversacion : MutableLiveData<String> = MutableLiveData("")
    val uidUsuario: String = Firebase.auth.uid!!

    private var limiteMensajes : Int = 10
    var adapterPosition : Int = 0

    fun enviarMensaje(mensaje: String?) {
        if (mensaje != null) {
            if (mensaje.isNotEmpty()) {
                val nuevoMensaje: Mensaje = Mensaje.Enviar(
                        Firebase.auth.currentUser!!.displayName,
                        Firebase.auth.currentUser!!.uid,
                        mensaje,
                        null,
                        Mensaje.MENSAJE_TEXTO,
                        ServerValue.TIMESTAMP,

                )
                strCampoMensaje.value = ""
                repoMensajes.subirMensaje(nuevoMensaje)
            }
        }
    }

    fun subirImagen(imagenGaleria: Uri, cuerpoMensaje : String = "") {
        val nuevoMensaje = Mensaje.Enviar(
                FirebaseAuth.getInstance().currentUser!!.displayName,
                FirebaseAuth.getInstance().uid,
                cuerpoMensaje,
                null,
                Mensaje.MENSAJE_IMAGEN,
                ServerValue.TIMESTAMP
        )
        repoMensajes.subirMensajeImagen(imagenGaleria, nuevoMensaje)
    }

    fun agregarContacto() {
        val repoContactos = RepositorioContactos()
        repoContactos.subirNuevoContacto(conversacion.uidContacto!!)
    }

    fun obtenerMensaje(): LiveData<Resource<List<Mensaje.Recibir>>> {
        return liveDataMensajes
    }

    fun cargarMasMensajes(){
        limiteMensajes += 10
        repoMensajes.cargarMensajes(limiteMensajes)
    }

    fun obtenerFechaConversacion(position: Int, listaAdaptador: List<Item>){
        if (adapterPosition != position) {
            adapterPosition = position
            if (listaAdaptador[position] is Item.Mensaje) {
                val epochHora = (listaAdaptador[position] as Item.Mensaje).mensaje.hora!!
                val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochHora), ZoneId.systemDefault())
                strFechaConversacion.value =
                        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
            }
        }
    }

    companion object {
        private const val TAG = "ConversacionViewModel"
    }

    init {
        conversacion = Conversacion(Utils.asignarIdConversacion(uidUsuario, usuario2Uid))
        conversacion.uidContacto = usuario2Uid
        conversacion.nombreContacto = nombreUsuario2
        conversacion.imagenPerfilContacto = imagenPerfil
        repoMensajes = RepositorioMensajes(conversacion)
        repoUsuario = RepositorioUsuario()
        liveDataMensajes = repoMensajes.cargarMensajes()
        liveDataUsuario = repoUsuario.obtenerDatosUsuario(conversacion.uidContacto)
    }

} /* Clase para el constructor */

internal class ConversacionViewModelFactory(
        private val usuario2Uid: String,
        private val nombreUsuario2: String,
        private val imagenPerfil: String
        ) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConversacionViewModel(usuario2Uid, nombreUsuario2, imagenPerfil) as T
    }
}