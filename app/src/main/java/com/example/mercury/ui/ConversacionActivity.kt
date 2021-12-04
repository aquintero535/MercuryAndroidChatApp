package com.example.mercury.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Fade
import android.transition.TransitionManager
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mercury.R
import com.example.mercury.databinding.ActivityConversacionBinding
import com.example.mercury.databinding.VistaContactoActionbarBinding
import com.example.mercury.model.Item
import com.example.mercury.model.Mensaje
import com.example.mercury.model.Usuario
import com.example.mercury.repo.InicioSesion
import com.example.mercury.repo.Resource
import com.example.mercury.utils.Utils
import com.example.mercury.viewmodel.ConversacionViewModel
import com.example.mercury.viewmodel.ConversacionViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.synthetic.main.enviar_imagen.*
import kotlinx.android.synthetic.main.enviar_imagen.view.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.*

class ConversacionActivity : AppCompatActivity(), Observer<Resource<List<Mensaje.Recibir>>> {

    private lateinit var viewModel: ConversacionViewModel
    private lateinit var listaMensajesAdapter: ListaMensajesAdapter
    private lateinit var binding: ActivityConversacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
                this,
                ConversacionViewModelFactory(
                        intent.getStringExtra("uid")!!,
                        intent.getStringExtra("nombre")!!,
                        intent.getStringExtra("imagenPerfil")!!)
        ).get(ConversacionViewModel::class.java)
        binding = ActivityConversacionBinding.inflate(layoutInflater)
        binding.apply {
            vista = this@ConversacionActivity
            lifecycleOwner = this@ConversacionActivity
            viewModel = this@ConversacionActivity.viewModel
        }
        setContentView(binding.root)
        configurarActionBar()
        configurarLista()
        viewModel.obtenerMensaje().observe(this, this)

    }

    private fun configurarActionBar(){
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        val actionBarBinding = VistaContactoActionbarBinding.inflate(layoutInflater)
        actionBarBinding.apply {
            vista = this@ConversacionActivity
            lifecycleOwner = this@ConversacionActivity
            viewModel = this@ConversacionActivity.viewModel
        }
        supportActionBar!!.customView = actionBarBinding.root
    }

    private fun configurarLista(){
        listaMensajesAdapter = ListaMensajesAdapter(this)
        binding.listaMensajes.layoutManager = LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            this.stackFromEnd = true
        }

        binding.listaMensajes.adapter = listaMensajesAdapter
        val transition = Fade()
        transition.addTarget(binding.frameLayoutItemFecha)
        binding.listaMensajes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val position = (recyclerView.layoutManager as LinearLayoutManager)
                        .findFirstCompletelyVisibleItemPosition()
                if (position != RecyclerView.NO_POSITION) {
                    viewModel.obtenerFechaConversacion(position, listaMensajesAdapter.getListaItems())
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    if (binding.frameLayoutItemFecha.visibility != View.VISIBLE) {
                        transition.startDelay = -1
                        TransitionManager.beginDelayedTransition(binding.frameLayoutItemFecha, transition)
                        binding.frameLayoutItemFecha.visibility = View.VISIBLE
                    }
                    if (!recyclerView.canScrollVertically(-1)){
                        viewModel.cargarMasMensajes()
                    }
                    if (recyclerView.canScrollVertically(-1)){
                        binding.botonDesplazar.visibility = View.VISIBLE
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    if (!recyclerView.canScrollVertically(1)){
                        binding.botonDesplazar.visibility = View.GONE
                    }
                    transition.startDelay = 3000
                    TransitionManager.beginDelayedTransition(binding.frameLayoutItemFecha, transition)
                    binding.frameLayoutItemFecha.visibility = View.GONE
                }
            }
        })
    }

    fun abrirGaleria() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(gallery, SELECCIONAR_IMAGEN)
    }

    fun desplazarLista(){
        binding.listaMensajes.scrollToPosition(listaMensajesAdapter.itemCount -1)
        binding.botonDesplazar.visibility = View.GONE
    }

    fun verImagenPerfil(){
        StfalconImageViewer.Builder(
                this, arrayListOf(viewModel.conversacion.imagenPerfilContacto))
        {
            imageView: ImageView, s: String? ->
            Glide.with(this)
                    .load(s)
                    .placeholder(R.color.colorGray)
                    .into(imageView)
        }.build().show(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == SELECCIONAR_IMAGEN) {
            if (data?.data != null) {
                prepararEnviarImagen(data.data!!)
            }
        }
    }

    private fun prepararEnviarImagen(uriImagen : Uri) {
        val vistaEnviarImagen = View.inflate(this, R.layout.enviar_imagen, enviarImagenConstraintLayout)
        val visor = StfalconImageViewer.Builder(this, arrayListOf(uriImagen)) { imageView: ImageView, uri: Uri ->
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.color.colorGray)
                    .into(imageView)
        }.withOverlayView(vistaEnviarImagen)
                .withDismissListener {
                    (vistaEnviarImagen.parent as ViewGroup).removeView(vistaEnviarImagen)
                }.allowSwipeToDismiss(false)
                .build()
        visor.show(true)
        vistaEnviarImagen.enviarImagenBotonEnviarMensaje.setOnClickListener {
            val cuerpoMensaje = vistaEnviarImagen.enviarImagenEditTextMensaje.editText?.text.toString()
            viewModel.subirImagen(uriImagen, cuerpoMensaje)
            visor.close()
        }
        vistaEnviarImagen.enviarImagenBotonRegresar.setOnClickListener {
            visor.close()
        }
    }

    /* Método que se ejecuta al cargarse los mensajes */
    override fun onChanged(mensajeResource: Resource<List<Mensaje.Recibir>>) {
        if (mensajeResource is Resource.Success) {
            listaMensajesAdapter.aniadirMensaje(mensajeResource.data!!)
            if (!binding.listaMensajes.canScrollVertically(1)){
                binding.listaMensajes.smoothScrollToPosition(listaMensajesAdapter.itemCount -1)
            } else{
                binding.listaMensajes.scrollToPosition(viewModel.adapterPosition)
            }
        } else if (mensajeResource is Resource.Error) {
            Snackbar.make(binding.root, mensajeResource.message!!, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        InicioSesion.cambiarEstadoEnLinea(true)
    }

    override fun onPause() {
        super.onPause()
        InicioSesion.cambiarEstadoEnLinea(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.action_agregar_contacto) {
            viewModel.agregarContacto()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.conversacion_menu, menu)
        return true
    }

    inner class ListaMensajesAdapter(private val context: Context)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val verImagen = VistaVerImagen(context)
        private val listaItems : MutableList<Item> = mutableListOf()
        private val listaImagenes : MutableList<Mensaje.Recibir> = mutableListOf()

        inner class MensajesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val vistaMensajeImagen: ImageView = itemView.findViewById(R.id.mensajeImagen)
            private val vistaCuerpoMensaje: TextView = itemView.findViewById(R.id.textoMensaje)
            private val vistaHoraMensaje: TextView = itemView.findViewById(R.id.textoHoraMensaje)

            fun bind(mensaje : Mensaje.Recibir) {
                Glide.with(context).clear(vistaMensajeImagen)
                vistaMensajeImagen.setImageDrawable(null)
                vistaHoraMensaje.text = Utils.convertirHora(mensaje.hora!!, Utils.TIME_PATTERN)
                vistaCuerpoMensaje.text = mensaje.cuerpoMensaje
                if (mensaje.tipoMensaje == Mensaje.MENSAJE_IMAGEN) {
                    vistaCuerpoMensaje.visibility = View.VISIBLE
                    vistaMensajeImagen.visibility = View.VISIBLE
                    Glide.with(context)
                            .asDrawable()
                            .load(Uri.parse(mensaje.uriImagen))
                            .centerCrop()
                            .placeholder(R.color.colorGray)
                            .into(vistaMensajeImagen)
                    vistaMensajeImagen.setOnClickListener {
                        abrirVisorImagenes(listaImagenes.indexOf(mensaje))
                    }
                } else {
                    vistaCuerpoMensaje.visibility = View.VISIBLE
                    vistaMensajeImagen.visibility = View.GONE
                }
            }
        }

        inner class FechaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val vistaTextoFechaConversacion : TextView = itemView.findViewById(R.id.textoItemFechaConversacion)

            fun bind(strFecha : String){
                vistaTextoFechaConversacion.text = strFecha
            }
        }

        fun aniadirMensaje(listaMensajes: List<Mensaje.Recibir>) {
            this.listaItems.clear()
            this.listaImagenes.clear()
            for (mensaje in listaMensajes){
                if (mensaje.tipoMensaje == Mensaje.MENSAJE_IMAGEN){
                    listaImagenes.add(mensaje)
                }
                listaItems.add(Item.Mensaje(mensaje))
            }
            Utils.colocarFechaConversacion(listaItems)
            notifyDataSetChanged()
        }

        private fun abrirVisorImagenes(posicionInicial : Int){
            var mensaje = listaImagenes[posicionInicial]
            verImagen.actualizar(
                    mensaje.uriImagen!!,
                    Utils.convertirFecha(mensaje.hora!!, FormatStyle.LONG),
                    mensaje.cuerpoMensaje)
            StfalconImageViewer.Builder(context, listaImagenes){ imageView: ImageView, m: Mensaje.Recibir ->
                Glide.with(context)
                        .load(m.uriImagen)
                        .placeholder(R.color.colorGray)
                        .into(imageView)
            }.withOverlayView(verImagen).withDismissListener {
                (verImagen.parent as ViewGroup).removeView(verImagen)
            }.withImageChangeListener {
                mensaje = listaImagenes[it]
                verImagen.actualizar(
                        mensaje.uriImagen!!,
                        Utils.convertirFecha(mensaje.hora!!, FormatStyle.LONG),
                        mensaje.cuerpoMensaje)
            }.withStartPosition(posicionInicial)
                    .build()
                    .show()
        }

        fun getListaItems() : List<Item> {
            return listaItems
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == ITEM_FECHA) {
                val item = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_fecha, parent, false)
                return FechaViewHolder(item)
            } else if (viewType == ITEM_MENSAJE_EMISOR){
                val item = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_mensaje_emisor, parent, false)
                return MensajesViewHolder(item)
            } else {
                val item = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_mensaje_receptor, parent, false)
                return MensajesViewHolder(item)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MensajesViewHolder) {
                holder.bind((listaItems[position] as Item.Mensaje).mensaje)
            } else if (holder is FechaViewHolder){
                holder.bind((listaItems[position] as Item.FechaConversacion).strFecha)
            }
        }

        override fun getItemCount(): Int {
            return listaItems.size
        }

        override fun getItemViewType(position: Int): Int {
            return when (listaItems[position]) {
                is Item.Mensaje -> if ((listaItems[position] as Item.Mensaje).mensaje.uidEmisor == viewModel.uidUsuario)
                    ITEM_MENSAJE_EMISOR else ITEM_MENSAJE_RECEPTOR
                is Item.FechaConversacion -> ITEM_FECHA
            }
        }
    }

    companion object {
        private const val TAG = "ConversacionActivity"
        private const val SELECCIONAR_IMAGEN = 100
        private const val ITEM_MENSAJE_EMISOR = 1
        private const val ITEM_MENSAJE_RECEPTOR = 2
        private const val ITEM_FECHA = 3

        @JvmStatic
        @BindingAdapter("cargarImagenPerfilActionBar")
        fun cargarImagenPerfilActionBar(view : ImageView, imagenPerfil : String){
            Glide.with(view.context)
                    .load(imagenPerfil)
                    .placeholder(R.color.colorGray)
                    .circleCrop()
                    .into(view)
        }

        @JvmStatic
        @BindingAdapter("cargarEstadoUsuario")
        fun cargarEstadoUsuario(view : TextView, usuario : Usuario?){
            if (usuario != null) {
                if (usuario.online) {
                    view.text = view.context.resources.getText(R.string.user_online)
                } else {
                    if (usuario.ultimaConexion != null) {
                        view.text = "${view.context.resources.getString(R.string.user_offline)} ${Utils.crearCadenaEstadoUsuario(usuario.ultimaConexion!!)}"
                    }
                }
            }
        }
    }
}