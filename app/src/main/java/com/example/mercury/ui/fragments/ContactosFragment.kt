package com.example.mercury.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mercury.R
import com.example.mercury.model.Contacto
import com.example.mercury.ui.AgregarContactoActivity
import com.example.mercury.ui.ConversacionActivity
import com.example.mercury.ui.RecyclerViewClickListener
import com.example.mercury.viewmodel.MainActivityViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.stfalcon.imageviewer.StfalconImageViewer

class ContactosFragment : Fragment(), RecyclerViewClickListener, Observer<List<Contacto>> {

    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var rvListaContactos: RecyclerView
    private lateinit var listaContactosAdapter: ListaContactosAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_contactos, container, false)
        val fab : FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_baseline_person_add_24)
        fab.setOnClickListener {
            val agregarContacto = Intent(context, AgregarContactoActivity::class.java)
            startActivity(agregarContacto)
        }
        rvListaContactos = root.findViewById(R.id.rvListaContactos)
        rvListaContactos.layoutManager = LinearLayoutManager(context)
        listaContactosAdapter = ListaContactosAdapter(this, requireContext())
        rvListaContactos.adapter = listaContactosAdapter
        viewModel.cargarDatosPerfil()
        viewModel.listaContactos.observe(viewLifecycleOwner, this)
        return root
    }

    override fun onClick(view: View?, position: Int) {
        val nuevaConversacion = Intent(activity, ConversacionActivity::class.java)
        nuevaConversacion.putExtra("uid", viewModel.listaContactos.value!![position].uid)
        nuevaConversacion.putExtra("nombre", viewModel.listaContactos.value!![position].nombre)
        nuevaConversacion.putExtra(
                "imagenPerfil", viewModel.listaContactos.value!![position].imagenPerfil
        )
        startActivity(nuevaConversacion)
    }

    override fun onChanged(contactos: List<Contacto>) {
        listaContactosAdapter.actualizarLista(contactos)
    }

    inner class ListaContactosAdapter(private val clickListener: RecyclerViewClickListener,
                                val context : Context)
        : RecyclerView.Adapter<ListaContactosAdapter.ContactosViewHolder>() {

        private var listaContactos : List<Contacto>? = null

        fun actualizarLista(contactos: List<Contacto>){
            if (this.listaContactos == null){
                listaContactos = contactos
            }
            notifyItemRangeChanged(0, itemCount)
        }

        inner class ContactosViewHolder(itemView: View)
            : RecyclerView.ViewHolder(itemView) {

            private val vistaImagenPerfil: ImageView = itemView.findViewById(R.id.contactosImagenPerfil)
            private val vistaNombreContacto : TextView = itemView.findViewById(R.id.contactosTextoNombre)
            private val vistaCorreoContacto : TextView = itemView.findViewById(R.id.contactosTextoCorreo)
            private val vistaMensajeContacto : TextView = itemView.findViewById(R.id.contactosTextoMensaje)

            fun bind(contacto : Contacto){
                Glide.with(itemView)
                        .load(contacto.imagenPerfil)
                        .override(130, 130)
                        .placeholder(ColorDrawable(Color.GRAY))
                        .circleCrop()
                        .into(vistaImagenPerfil)
                vistaNombreContacto.text = contacto.nombre
                vistaMensajeContacto.text = contacto.mensajePerfil
                if (!contacto.email.isNullOrEmpty()) {
                    vistaCorreoContacto.text = contacto.email
                } else if (!contacto.numeroTelefono.isNullOrBlank()) {
                    vistaCorreoContacto.text = contacto.numeroTelefono
                }
                itemView.setOnClickListener { clickListener.onClick(it.rootView, adapterPosition) }
                vistaImagenPerfil.setOnClickListener {
                    abrirVisorImagenPerfil(contacto.imagenPerfil)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactosViewHolder {
            val item = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_contacto, parent, false)
            return ContactosViewHolder(item)
        }

        override fun onBindViewHolder(holder: ContactosViewHolder, position: Int) {
            if (listaContactos != null) {
                holder.bind(listaContactos!![position])
            }
        }

        private fun abrirVisorImagenPerfil(uriImagen : String) {
            StfalconImageViewer.Builder(context, arrayListOf(uriImagen))
            { imageView: ImageView, s: String ->
                Glide.with(imageView)
                        .load(s)
                        .placeholder(R.color.colorGray)
                        .into(imageView)
            }.build().show(true)
        }

        override fun getItemCount(): Int {
            return if (listaContactos != null) listaContactos!!.size else 0
        }
    }

    companion object {
        private const val TAG = "ContactosFragment"
    }
}