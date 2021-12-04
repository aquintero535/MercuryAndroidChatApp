package com.example.mercury.model

open class Mensaje(
        var nombreEmisor: String? = null,
        var uidEmisor: String? = null,
        var cuerpoMensaje: String? = null,
        var uriImagen: String? = null,
        var tipoMensaje : Int? = null
) {
    class Enviar(nombreEmisor: String? = null,
                 uidEmisor: String? = null,
                 cuerpoMensaje: String? = null,
                 uriImagen: String? = null,
                 tipoMensaje: Int? = null,
                 var hora: Map<*,*>? = null
    ) : Mensaje(nombreEmisor, uidEmisor, cuerpoMensaje, uriImagen, tipoMensaje)

    class Recibir(nombreEmisor: String? = null,
                 uidEmisor: String? = null,
                 cuerpoMensaje: String? = null,
                 uriImagen: String? = null,
                 tipoMensaje: Int? = null,
                 var hora: Long? = null
    ) : Mensaje(nombreEmisor, uidEmisor, cuerpoMensaje, uriImagen, tipoMensaje) {
        override fun toString(): String {
            return "$nombreEmisor: $cuerpoMensaje [${hora.toString()}]"
        }
    }

    companion object {
        const val MENSAJE_TEXTO = 1
        const val MENSAJE_IMAGEN = 2
    }
}