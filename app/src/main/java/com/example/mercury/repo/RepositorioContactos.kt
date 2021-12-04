package com.example.mercury.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.mercury.model.Contacto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class RepositorioContactos {

    private val uid : String? = FirebaseAuth.getInstance().currentUser?.uid
    private val listaContactos : MutableList<Contacto> = mutableListOf()
    private val liveDataContactos : MutableLiveData<List<Contacto>> = MutableLiveData()

    fun obtenerContactos() : MutableLiveData<List<Contacto>> {
        val dbContactos = Firebase.database.getReference("/usuarios_contactos/$uid")
        dbContactos.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (nodo in snapshot.children){
                        val contacto = Contacto()
                        contacto.uid = nodo.key!!
                        obtenerDatosContacto(contacto)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
            }

        })
        return liveDataContactos
    }

    fun obtenerDatosContacto(contacto: Contacto){
        val dbUsuario = Firebase.database.getReference("/usuarios/${contacto.uid}")
        dbUsuario.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    contacto.nombre = snapshot.child("nombre").getValue<String>()!!
                    contacto.email = if (snapshot.child("email").exists())
                        snapshot.child("email").getValue<String>()!! else ""
                    contacto.numeroTelefono = if (snapshot.child("numeroTelefono").exists())
                        snapshot.child("numeroTelefono").getValue<String>()!! else ""
                    contacto.mensajePerfil = snapshot.child("mensajePerfil").getValue<String>()!!
                    contacto.imagenPerfil = snapshot.child("imagenPerfil").getValue<String>()!!
                    enviarDatos(contacto)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
            }
        })
    }

    fun enviarDatos(contacto: Contacto){
        if (!(listaContactos.contains(contacto))){
            listaContactos.add(contacto)
        }
        listaContactos.sortBy { it.nombre }
        liveDataContactos.value = listaContactos
    }

    fun subirNuevoContacto(uidContacto : String){
        val dbContactos = Firebase.database.getReference("/usuarios_contactos/$uid")
        dbContactos.child(uidContacto).setValue(true).addOnSuccessListener {
            Log.i(TAG, "Usuario $uidContacto subido.")
        }
    }

    companion object {
        private const val TAG : String = "RepositorioContactos"
    }
}