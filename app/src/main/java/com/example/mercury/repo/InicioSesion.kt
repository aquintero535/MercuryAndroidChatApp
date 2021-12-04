package com.example.mercury.repo

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.mercury.model.Usuario
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object InicioSesion {

    private const val TAG : String = "InicioSesion"
    @JvmField val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
    )

    val iniciarSesionIntent : Intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

    private const val DEFAULT_PROFILE_IMAGE = "https://firebasestorage.googleapis.com/v0/b/" +
            "mercury-6a591.appspot.com/o/imagenes_perfil%2Fdefault-profile-picture.png" +
            "?alt=media&token=e7dfb590-d9b1-4867-a590-16027928d9a0"
    private const val DEFAULT_PROFILE_MESSAGE = "Disponible"

    const val SESION_ABIERTA : Int = 1
    const val SESION_CERRADA : Int = 0
    const val RC_SIGN_IN = 123

    fun comprobarSesion() : Int{
        val usuarioActual = Firebase.auth.currentUser
        if (usuarioActual != null){
            return SESION_ABIERTA
        }
        return SESION_CERRADA
    }

    fun cerrarSesion(){
        Firebase.auth.signOut()
    }

    fun subirNuevoUsuario(){
        val uid = Firebase.auth.currentUser?.uid
        val db = Firebase.database.getReference("/usuarios/$uid")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()){
                    val operacion = mutableMapOf<String, Any?>()
                    val email = Firebase.auth.currentUser?.email
                    var imagenPerfil = Firebase.auth.currentUser?.photoUrl.toString()
                    if (imagenPerfil == "null"){
                        imagenPerfil = DEFAULT_PROFILE_IMAGE
                        actualizarImagenPerfil()
                    }
                    if (email != null) { //Registro por email
                        val usuario = Usuario(
                                email,
                                uid!!,
                                Firebase.auth.currentUser?.displayName?:"",
                                imagenPerfil,
                                DEFAULT_PROFILE_MESSAGE
                        )
                        val formattedEmail = email
                                .replace("@", "_at_")
                                .replace(".", "_dot_")
                        operacion["/usuarios/$uid"] = usuario
                        operacion["/usuarios_correo_uid/$formattedEmail/uid"] = uid
                    } else { //Registro por teléfono
                        val telefono = Firebase.auth.currentUser?.phoneNumber
                        if (telefono != null){
                            val usuario = Usuario(
                                    "",
                            uid!!,
                            telefono,
                            imagenPerfil,
                            DEFAULT_PROFILE_MESSAGE,
                            telefono)
                            operacion["/usuarios/$uid"] = usuario
                            operacion["/usuarios_telefono_uid/$telefono/uid"] = uid
                            actualizarNombrePerfil(telefono)
                        }
                    }
                    db.root.updateChildren(operacion).addOnCompleteListener{
                        if (it.isCanceled){
                            Log.e(TAG, it.exception?.message?:"Error")
                            cerrarSesion()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
                cerrarSesion()
            }
        })
    }

    fun actualizarImagenPerfil(){
        val nuevoUsuario = Firebase.auth.currentUser
        val actualizarPerfil = userProfileChangeRequest {
            this.photoUri = Uri.parse(DEFAULT_PROFILE_IMAGE)
        }
        nuevoUsuario!!.updateProfile(actualizarPerfil).addOnCompleteListener {
            if (it.isCanceled){
                Log.e(TAG, it.exception?.message?:"Error")
            }
        }
    }

    fun actualizarNombrePerfil(nombrePerfil : String){
        val nuevoUsuario = Firebase.auth.currentUser
        val actualizarPerfil = userProfileChangeRequest {
            this.displayName = nombrePerfil
        }
        nuevoUsuario!!.updateProfile(actualizarPerfil).addOnCompleteListener {
            if (it.isCanceled){
                Log.e(TAG, it.exception?.message?:"Error")
            }
        }
    }

    fun cambiarEstadoEnLinea(estado : Boolean){
        val operacion = mutableMapOf<String, Any>()
        operacion["/usuarios/${Firebase.auth.currentUser?.uid}/online"] = estado
        operacion["/usuarios/${Firebase.auth.currentUser?.uid}/ultimaConexion"] = ServerValue.TIMESTAMP
        Firebase.database.reference.updateChildren(operacion)
    }
}