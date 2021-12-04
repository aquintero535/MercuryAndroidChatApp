package com.example.mercury.repo

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.mercury.model.Conversacion
import com.example.mercury.model.Mensaje
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class RepositorioMensajes (private val conversacion: Conversacion) {

    private val liveDataMensajes = MutableLiveData<Resource<List<Mensaje.Recibir>>>()
    private val uidUsuario : String = Firebase.auth.uid ?: ""
    private val dbMensajes : DatabaseReference =
            Firebase.database.getReference("/mensajes/${conversacion.idConversacion}")

    fun subirMensaje(mensaje : Mensaje){
        val db = Firebase.database.reference
        val operacion = mutableMapOf<String, Any?>()
        operacion["/usuarios_chats/${uidUsuario}/${conversacion.idConversacion}"] = true
        operacion["/usuarios_chats/${conversacion.uidContacto}/${conversacion.idConversacion}"] = true
        operacion["/chats_miembros/${conversacion.idConversacion}/${uidUsuario}"] = true
        operacion["/chats_miembros/${conversacion.idConversacion}/${conversacion.uidContacto}"] = true
        operacion["/mensajes/${conversacion.idConversacion}/${dbMensajes.push().key}"] = mensaje
        db.updateChildren(operacion) { error, ref ->
            if (error == null) {
                operacion.clear()
                if (mensaje.tipoMensaje == Mensaje.MENSAJE_TEXTO) {
                    operacion["/metainfo_chats/${conversacion.idConversacion}/ultimo_mensaje/"] =
                            mensaje.cuerpoMensaje
                } else if (mensaje.tipoMensaje == Mensaje.MENSAJE_IMAGEN) {
                    operacion["/metainfo_chats/${conversacion.idConversacion}/ultimo_mensaje/"] =
                            mensajeImagenStr
                }
                operacion["/metainfo_chats/${conversacion.idConversacion}/timestamp/"] =
                        ServerValue.TIMESTAMP
                db.updateChildren(operacion)
            } else {
                Log.e(TAG, error.message)
                liveDataMensajes.value = Resource.Error(ERROR_SUBIR_MENSAJE)
            }
        }
    }

    fun subirMensajeImagen(imagenGaleria : Uri, mensaje : Mensaje.Enviar){
        val dbImg = Firebase.storage.getReference("chat_imagenes/${uidUsuario}"
                .plus(UUID.randomUUID().toString()))
        dbImg.putFile(imagenGaleria)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        dbImg.downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful) {
                                mensaje.uriImagen = it.result.toString()
                                subirMensaje(mensaje)
                            } else {
                                Log.e(TAG, it.exception?.message ?: "Error")
                            }
                        }
                    } else {
                        Log.e(TAG, it.exception?.message ?: "Error")
                    }
                }
    }

    fun cargarMensajes(limiteMensajes : Int = 10) : MutableLiveData<Resource<List<Mensaje.Recibir>>>{
        val listaMensajes = mutableListOf<Mensaje.Recibir>()
        this.dbMensajes.limitToLast(limiteMensajes)
                .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    listaMensajes.clear()
                    for (nodo in snapshot.children) {
                        val mensaje: Mensaje.Recibir? = nodo.getValue<Mensaje.Recibir>()
                        if (mensaje != null) {
                            listaMensajes.add(mensaje)
                        }
                    }
                    liveDataMensajes.value = Resource.Success(listaMensajes, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                liveDataMensajes.value = Resource.Error(error.message)
                Log.e(TAG, error.message + " Code: ${error.code}")
            }

        })
        return liveDataMensajes
    }

    companion object{
        private const val TAG : String = "RepositorioMensajes"
        private const val ERROR_SUBIR_MENSAJE = "Ha ocurrido un error al enviar tu mensaje"
        private val mensajeImagenStr : String = "${String(Character.toChars(0x1F5BC))} Imagen"
    }
}