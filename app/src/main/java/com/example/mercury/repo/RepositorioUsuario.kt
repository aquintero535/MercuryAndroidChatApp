package com.example.mercury.repo

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.mercury.model.Usuario
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.concurrent.TimeUnit

class RepositorioUsuario {

    private val uidUsuario : String = Firebase.auth.currentUser?.uid!!
    private val liveDataUsuario = MutableLiveData<Resource<Usuario>>()
    val liveDataStatus = MutableLiveData<String>()

    fun buscarUsuarioPorCorreo(correo: String?) : MutableLiveData<Resource<Usuario>>{
        val correoUsuario = Firebase.auth.currentUser?.email
        if (correo != null && correo != correoUsuario) {
            val nodoUsuario = correo
                    .replace("@", "_at_")
                    .replace(".", "_dot_")
            val db = Firebase.database.getReference("/usuarios_correo_uid/$nodoUsuario/uid")
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val uidUsuario: String? = snapshot.getValue<String>()
                        obtenerDatosUsuario(uidUsuario)
                    } else {
                        liveDataUsuario.value = Resource.Error(USUARIO_NO_ENCONTRADO)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, error.message)
                    liveDataUsuario.value = Resource.Error(error.message)
                }
            })
        }
        return liveDataUsuario;
    }

    fun buscarUsuarioPorTelefono(telefono: String?) : MutableLiveData<Resource<Usuario>>{
        val telefonoUsuario = Firebase.auth.currentUser?.phoneNumber
        if (telefono != null && telefono != telefonoUsuario) {
            val db = Firebase.database.getReference("/usuarios_telefono_uid/$telefono/uid")
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val uidUsuario: String? = snapshot.getValue<String>()
                        obtenerDatosUsuario(uidUsuario)
                    } else {
                        liveDataUsuario.value = Resource.Error(USUARIO_NO_ENCONTRADO)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, error.message)
                    liveDataUsuario.value = Resource.Error(error.message)
                }
            })
        }
        return liveDataUsuario;
    }

    fun obtenerDatosUsuario(uidUsuario: String?) : MutableLiveData<Resource<Usuario>>{
        val dbUsuario = Firebase.database.getReference("/usuarios/$uidUsuario")
        dbUsuario.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val usuario : Usuario? = snapshot.getValue<Usuario>()
                    if (usuario != null){
                        liveDataUsuario.value = Resource.Success(usuario, null)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                liveDataUsuario.value = Resource.Error(error.message)
            }
        })
        return liveDataUsuario
    }


    fun subirImagenPerfil(uriImagen : Uri?) {
        val storageReference = FirebaseStorage.getInstance()
                .getReference("imagenes_perfil/$uidUsuario")
        storageReference.putFile(uriImagen!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageReference.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val nuevaImagenPerfil = task.result!!
                        actualizarImagenPerfil(nuevaImagenPerfil)
                    }
                }
            } else {
                liveDataStatus.value = IMAGEN_ERROR
            }
        }
    }

    private fun actualizarImagenPerfil(nuevaImagenPerfil : Uri){
        val subirImagen = UserProfileChangeRequest.Builder()
                .setPhotoUri(nuevaImagenPerfil)
                .build()
        val operacion = FirebaseAuth.getInstance().currentUser!!
                .updateProfile(subirImagen)
        operacion.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val db = FirebaseDatabase
                        .getInstance()
                        .getReference("/usuarios/$uidUsuario/imagenPerfil")
                db.setValue(nuevaImagenPerfil.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                liveDataStatus.value = IMAGEN_ACTUALIZADA
                            }
                        }
            } else {
                liveDataStatus.value = task.exception?.message
            }
        }
    }

    fun cambiarNombrePerfil(nuevoNombre : String){
        Firebase.database.getReference("/usuarios/$uidUsuario/nombre")
                .setValue(nuevoNombre)
                .addOnCompleteListener { it ->
                    if (it.isSuccessful){
                        val actualizarNombre = UserProfileChangeRequest.Builder().apply {
                            displayName = nuevoNombre
                        }.build()
                        Firebase.auth.currentUser?.updateProfile(actualizarNombre)?.addOnCompleteListener {
                            if (it.isSuccessful){
                                liveDataStatus.value = NOMBRE_PERFIL_ACTUALIZADO
                            } else {
                                liveDataStatus.value = it.exception?.message
                            }
                        }
                    } else {
                        liveDataStatus.value = it.exception?.message
                    }
                }
    }

    fun cambiarMensajePerfil(nuevoMensaje : String?){
        val dbMensaje = FirebaseDatabase
                .getInstance()
                .getReference("/usuarios/$uidUsuario/mensajePerfil")
        dbMensaje.setValue(nuevoMensaje).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                liveDataStatus.value = MENSAJE_PERFIL_ACTUALIZADO
            } else {
                liveDataStatus.value = task.exception?.message
            }
        }
    }

    fun cambiarContrasenia(nuevaContrasenia : String){
        Firebase.auth.currentUser?.updatePassword(nuevaContrasenia)?.addOnCompleteListener {
            if (it.isSuccessful){
                liveDataStatus.value = CONTRASENIA_CAMBIADA
            } else {
                liveDataStatus.value = it.exception?.message
            }
        }
    }

    fun cambiarCorreo(nuevoCorreo : String){
        Firebase.auth.currentUser?.updateEmail(nuevoCorreo)?.addOnCompleteListener {
            if (it.isSuccessful){
                liveDataStatus.value = CORREO_CAMBIADO
            } else {
                liveDataStatus.value = it.exception?.message
            }
        }
    }

    companion object {
        private const val TAG = "RepositorioUsuario"
        private const val USUARIO_NO_ENCONTRADO : String = "Usuario no encontrado."
        private const val IMAGEN_ACTUALIZADA = "Se ha actualizado tu imagen de perfil."
        private const val NOMBRE_PERFIL_ACTUALIZADO = "Nombre de perfil cambiado"
        private const val NOMBRE_PERFIL_ERROR = "Ha ocurrido un erorr al cambiar tu nombre de perfil"
        private const val MENSAJE_PERFIL_ACTUALIZADO = "Mensaje de perfil cambiado."
        private const val IMAGEN_ERROR = "Ha ocurrido un error al actualizar tu imagen de perfil"
        private const val CONTRASENIA_CAMBIADA = "Contraseña cambiada con éxito."
        private const val CORREO_CAMBIADO = "Correo cambiado con éxito."
    }
}