package com.example.mercury.utils

import com.example.mercury.model.Item
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

object Utils {

    private const val TAG = "Utils"
    const val TIME_PATTERN = "hh:mm a"

    fun convertirHora(hora : Long, pattern : String) : String {
        val date = Date(hora)
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    fun convertirFecha(tiempo : Long, estilo : FormatStyle) : String {
        val fecha = LocalDateTime.ofInstant(Instant.ofEpochMilli(tiempo), ZoneId.systemDefault())
        val zone = ZonedDateTime.of(fecha, ZoneId.systemDefault());
        return DateTimeFormatter.ofLocalizedDateTime(estilo).format(zone)
    }

    fun colocarFechaConversacion(listaItems : MutableList<Item>){
        val fechaHoy = LocalDateTime.now(ZoneId.systemDefault())
        //Se utiliza para conocer la posición del primer mensaje de una fecha (día) específico. Si
        //el siguiente mensaje pertenece al mismo día, el set no lo guarda y la posición tampoco.
        val fechas = mutableSetOf<LocalDate>()

        //Mapa que guarda tanto la posición del mensaje y la cadena con la fecha de la conversación.
        //La posición se guarda para añadir el item de fecha en esas posiciones.
        val indices = mutableMapOf<Int, String>()

        //Ciclo que recorre la lista de mensajes, obtiene sus fechas, y guarda la posición en las que
        //se colocarán los items con la fecha de la conversación.
        for (i in listaItems.indices){
            if (listaItems[i] is Item.Mensaje){
                val mensaje = (listaItems[i] as Item.Mensaje).mensaje
                val fecha = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(mensaje.hora!!), ZoneId.systemDefault()).toLocalDate()
                if (fechas.add(fecha)){
                    if (fechaHoy.dayOfYear == fecha.dayOfYear && fechaHoy.year == fecha.year) {
                        indices[i] = "Hoy"
                    } else if (fechaHoy.dayOfYear-1 == fecha.dayOfYear && fechaHoy.year == fecha.year){
                        indices[i] = "Ayer"
                    } else {
                        val strFecha = fecha.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                        indices[i] = strFecha
                    }
                }
            }
        }

        //Ciclo que coloca los items de la fecha de conversación. El contador 'j' sirve para conocer
        //cuantas posiciones ha incrementado la lista, después de ser añadido un item.
        var j = 0
        for (key in indices.keys){
            listaItems.add(key+j, Item.FechaConversacion(indices[key]!!))
            j += 1
        }
    }

    fun crearCadenaEstadoUsuario(ultimaConexion : Long) : String {
        val fechaConexion = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ultimaConexion), ZoneId.systemDefault())
        val fechaHoy = LocalDateTime.now(ZoneId.systemDefault())
        return when (fechaConexion.dayOfYear) {
            fechaHoy.dayOfYear -> {
                "hoy ${fechaConexion.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}"
            }
            fechaHoy.dayOfYear-1 -> {
                "ayer ${fechaConexion.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}"
            }
            else -> {
                fechaConexion.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            }
        }
    }

    fun asignarIdConversacion(usuarioUid : String, usuario2Uid: String): String {
        val comparar = usuarioUid.compareTo(usuario2Uid)
        return if (comparar < 0) {
            usuarioUid + "_" + usuario2Uid
        } else {
            usuario2Uid + "_" + usuarioUid
        }
    }
}