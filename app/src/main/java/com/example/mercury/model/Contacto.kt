package com.example.mercury.model

data class Contacto (
    var email: String = "",
    var imagenPerfil: String = "",
    var nombre: String = "",
    var uid: String = "",
    var mensajePerfil: String = "",
    var numeroTelefono: String = ""
){
    override fun toString(): String {
        return "Nombre: $nombre Uid: $uid"
    }
}