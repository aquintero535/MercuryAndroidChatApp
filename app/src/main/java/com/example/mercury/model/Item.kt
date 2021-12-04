package com.example.mercury.model

import com.example.mercury.model.Mensaje.Recibir

sealed class Item{
    data class Mensaje(val mensaje: Recibir) : Item()
    data class FechaConversacion(val strFecha : String) : Item()
}