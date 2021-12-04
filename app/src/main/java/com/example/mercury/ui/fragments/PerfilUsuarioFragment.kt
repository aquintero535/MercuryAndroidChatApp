package com.example.mercury.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.mercury.R
import com.example.mercury.databinding.FragmentPerfilUsuarioBinding
import com.example.mercury.ui.MainActivity
import com.example.mercury.viewmodel.PerfilUsuarioViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class PerfilUsuarioFragment : Fragment() {

    private val viewModel : PerfilUsuarioViewModel by viewModels()
    private lateinit var alert : AlertDialog
    private lateinit var editarMensajePerfil : EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding : FragmentPerfilUsuarioBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_perfil_usuario, container, false)
        binding.viewModel = viewModel
        binding.vista = this
        binding.lifecycleOwner = viewLifecycleOwner
        val vista = binding.root
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.visibility = View.INVISIBLE
        crearAlert()
        return vista
    }

    private fun crearAlert(){
        alert = AlertDialog.Builder(context).create()
        alert.setTitle("Perfil de usuario")
        alert.setMessage("Escribe tu nuevo mensaje de perfil: ")
        editarMensajePerfil = EditText(context)
        alert.setView(editarMensajePerfil)
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "Aceptar") { dialog, which ->
            val mensaje = editarMensajePerfil.text.toString()
            if (mensaje.isNotEmpty()) {
                viewModel.cambiarMensajePerfil(mensaje)
            }
        }

        alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar") { dialog, which -> alert.dismiss() }
    }

    fun botonCambiarImagenPerfil() {
        val galeria = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galeria, SELECCIONAR_IMAGEN)
    }

    fun botonCerrarSesion(){
        viewModel.cerrarSesion()
        requireActivity().finish()
        startActivity(Intent(context, MainActivity::class.java))
    }

    fun botonEditarMensajePerfil(){
        editarMensajePerfil.text = Editable.Factory.getInstance().newEditable(
                viewModel.liveDataUsuario.value?.data?.mensajePerfil?:"")
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == SELECCIONAR_IMAGEN){
            viewModel.subirImagenPerfil(data?.data)
        }
    }

    companion object {

        @JvmStatic
        @BindingAdapter("snackbar")
        fun ejecutarMensaje(view: View, mensaje: String?) {
            if (mensaje != null){
                Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show()
            }
        }

        @JvmStatic
        @BindingAdapter("profileImage")
        fun cargarImagen(view: ImageView, imagenPerfil : String) {
            if (imagenPerfil.isNotEmpty()) {
                Glide.with(view.context)
                        .load(imagenPerfil)
                        .placeholder(ColorDrawable(Color.GRAY))
                        .override(450, 450)
                        .circleCrop()
                        .into(view)
            }
        }

        private const val TAG: String = "PerfilUsuarioFragment"
        private const val SELECCIONAR_IMAGEN: Int = 100

    }

}