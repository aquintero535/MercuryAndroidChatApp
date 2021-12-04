package com.example.mercury.ui.fragments

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
import com.example.mercury.model.Conversacion
import com.example.mercury.ui.ConversacionActivity
import com.example.mercury.ui.IniciarConversacionActivity
import com.example.mercury.ui.RecyclerViewClickListener
import com.example.mercury.utils.Utils
import com.example.mercury.viewmodel.MainActivityViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PrincipalFragment : Fragment(), RecyclerViewClickListener, Observer<List<Conversacion>> {
    private val viewModel: MainActivityViewModel by activityViewModels()

    private lateinit var listaConversacion: RecyclerView
    private lateinit var listaConversacionAdapter: ListaConversacionAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_principal, container, false)
        val fab : FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_baseline_chat_24)
        fab.setOnClickListener {
            val iniciarConversacion = Intent(activity,
                    IniciarConversacionActivity::class.java)
            startActivity(iniciarConversacion)
        }
        listaConversacion = root.findViewById(R.id.listaChatsRv)
        listaConversacion.layoutManager = LinearLayoutManager(context)
        listaConversacionAdapter = ListaConversacionAdapter(this)
        listaConversacion.adapter = listaConversacionAdapter
        viewModel.cargarDatosPerfil()
        viewModel.listaConversacion.observe(viewLifecycleOwner, this)
        return root
    }

    override fun onClick(view: View?, position: Int) {
        val uidContacto = viewModel.listaConversacion.value!![position].uidContacto
        val nombreContacto = viewModel.listaConversacion.value!![position].nombreContacto
        val imagenPerfilContacto = viewModel.listaConversacion.value!![position].imagenPerfilContacto
        if (uidContacto != null) {
            val abrirConversacion = Intent(context, ConversacionActivity::class.java)
            abrirConversacion.putExtra("uid", uidContacto)
            abrirConversacion.putExtra("nombre", nombreContacto)
            abrirConversacion.putExtra("imagenPerfil", imagenPerfilContacto)
            startActivity(abrirConversacion)
        }
    }

    override fun onChanged(conversacion: List<Conversacion>) {
        listaConversacionAdapter.aniadirConversacion(conversacion)
    }

    inner class ListaConversacionAdapter(private val clickListener: RecyclerViewClickListener)
        : RecyclerView.Adapter<ListaConversacionAdapter.ConversacionViewHolder>() {

        private var listaConversacion: List<Conversacion>? = null

        fun aniadirConversacion(conversacion: List<Conversacion>) {
            if (listaConversacion == null){
                listaConversacion = conversacion
            }
            notifyItemRangeChanged(0, itemCount)
        }

        inner class ConversacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val vistaImagenPerfil: ImageView = itemView.findViewById(R.id.imagenPerfil)
            private val vistaTextoNombreContacto: TextView = itemView.findViewById(R.id.textoNombreContacto)
            private val vistaTextoUltimoMensaje: TextView = itemView.findViewById(R.id.textoUltimoMensaje)
            private val vistaTextoHoraUltimoMensaje: TextView = itemView.findViewById(R.id.conversacionHoraMensaje)

            fun bind(conversacion: Conversacion){
                vistaTextoNombreContacto.text = conversacion.nombreContacto
                var ultimoMensaje = conversacion.ultimoMensaje
                if (ultimoMensaje!!.length > 30) {
                    ultimoMensaje = ultimoMensaje.substring(0, 30) + "..."
                }
                vistaTextoUltimoMensaje.text = ultimoMensaje
                vistaTextoHoraUltimoMensaje.text = Utils.convertirHora(conversacion.horaUltimoMensaje!!, Utils.TIME_PATTERN)
                Glide.with(itemView)
                        .load(conversacion.imagenPerfilContacto)
                        .circleCrop()
                        .override(130, 130)
                        .placeholder(ColorDrawable(Color.GRAY))
                        .into(vistaImagenPerfil)
                itemView.setOnClickListener { clickListener.onClick(it.rootView, adapterPosition) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversacionViewHolder {
            val item = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_conversacion,
                            parent,
                            false)
            return ConversacionViewHolder(item)
        }

        override fun onBindViewHolder(holder: ConversacionViewHolder, position: Int) {
            if (listaConversacion != null){
                holder.bind(listaConversacion!![position])
            }
        }

        override fun getItemCount(): Int {
            return if (listaConversacion != null) listaConversacion!!.size else 0
        }
    }

    companion object {
        private const val TAG = "PrincipalFragment"
    }
}