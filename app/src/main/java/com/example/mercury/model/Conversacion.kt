package com.example.mercury.model

class Conversacion(val idConversacion: String) {
    var ultimoMensaje: String? = ""
    var horaUltimoMensaje: Long? = 0
    var nombreContacto: String? = ""
    var uidContacto: String? = ""
    var imagenPerfilContacto: String? = ""

    override fun toString(): String {
        return " |Nombre: $nombreContacto Mensaje: $ultimoMensaje"
    }
}