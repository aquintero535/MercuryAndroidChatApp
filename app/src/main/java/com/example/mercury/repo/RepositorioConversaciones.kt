package com.example.mercury.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.mercury.model.Conversacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class RepositorioConversaciones {

    private val uid : String? = FirebaseAuth.getInstance().uid
    private val listaConversaciones : MutableList<Conversacion> = ArrayList()
    private val liveDataConversacion : MutableLiveData<List<Conversacion>> = MutableLiveData()

    fun obtenerListaConversaciones() : MutableLiveData<List<Conversacion>> {
        val dbUsuarioChats = Firebase.database.getReference("/usuarios_chats/$uid")
        dbUsuarioChats.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    listaConversaciones.clear()
                    for (nodo in snapshot.children) {
                        if (nodo.key != null) {
                            val conversacion = Conversacion(nodo.key!!)
                            buscarUltimoMensaje(conversacion)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
            }
        })
        return liveDataConversacion
    }

    fun buscarUltimoMensaje(conversacion: Conversacion){
        val dbMetaInfo = Firebase.database.getReference("/metainfo_chats/${conversacion.idConversacion}")
        dbMetaInfo.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    conversacion.ultimoMensaje = snapshot.child("ultimo_mensaje").getValue<String>()
                    conversacion.horaUltimoMensaje = snapshot.child("timestamp").getValue<Long>()
                    buscarMiembrosChats(conversacion)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
            }
        })
    }

    fun buscarMiembrosChats(conversacion: Conversacion){
        val dbMiembros = Firebase.database.getReference("/chats_miembros/${conversacion.idConversacion}")
        dbMiembros.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (nodo in snapshot.children){
                        var uidContacto = nodo.key
                        if (!(uidContacto.equals(uid))){ //TODO: Incompatible en grupos.
                            conversacion.uidContacto = uidContacto
                            buscarContacto(conversacion)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
            }
        })
    }

    fun buscarContacto(conversacion: Conversacion){
        val dbContacto = Firebase.database.getReference("/usuarios/${conversacion.uidContacto}")
        dbContacto.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    conversacion.nombreContacto = snapshot.child("nombre").getValue<String>()
                    conversacion.imagenPerfilContacto = snapshot.child("imagenPerfil").getValue<String>()
                    enviarDatos(conversacion)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
            }
        })
    }

    fun enviarDatos(conversacion: Conversacion){
        if (!listaConversaciones.contains(conversacion)){
            listaConversaciones.add(conversacion)
        }
        listaConversaciones.sortByDescending { it.horaUltimoMensaje }
        liveDataConversacion.value = listaConversaciones
    }

    companion object {
        private const val TAG : String = "RepositorioConversa"
    }

}