package com.example.mercury.model

data class Usuario (
    var email: String = "",
    var uid: String = "",
    var nombre: String = "",
    var imagenPerfil: String = "",
    var mensajePerfil: String = "",
    var numeroTelefono: String = "",
    var online : Boolean = false,
    var ultimaConexion : Long? = null
)