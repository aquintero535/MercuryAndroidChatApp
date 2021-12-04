package com.example.mercury.ui

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.FrameLayout
import com.example.mercury.R
import kotlinx.android.synthetic.main.vista_ver_imagen.view.*

class VistaVerImagen (context: Context,
                      attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.vista_ver_imagen, this)
    }

    fun actualizar(uriImagen : String, hora : String, cuerpoMensaje : String?){
        imagenHora.text = hora
        imagenDescripcion.text = cuerpoMensaje
        botonCompartir.setOnClickListener {
            context.startActivity(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, uriImagen)
                type = "text/plain"
            })
        }
    }

}