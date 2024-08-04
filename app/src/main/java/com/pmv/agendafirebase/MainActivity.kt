package com.pmv.agendafirebase

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pmv.agendafirebase.models.FirebaseReferences
import com.pmv.agendafirebase.models.Contacts

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnGuardar: Button
    private lateinit var btnListar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var txtNombre: TextView
    private lateinit var txtDireccion: TextView
    private lateinit var txtTelefono1: TextView
    private lateinit var txtTelefono2: TextView
    private lateinit var txtNotas: TextView
    private lateinit var cbkFavorite: CheckBox
    private lateinit var basedatabase: FirebaseDatabase
    private lateinit var referencia: DatabaseReference
    private var savedContacto: Contacts? = null
    private var id: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
        setEvents()
    }

    private fun initComponents() {
        // Se obtiene una instancia de la base de datos y se obtiene la referencia que apunta a la tabla contactos
        basedatabase = FirebaseDatabase.getInstance()
        referencia = basedatabase.getReferenceFromUrl(
            FirebaseReferences.URL_DATABASE + FirebaseReferences.DATABASE_NAME + "/" + FirebaseReferences.TABLE_NAME
        )
        txtNombre = findViewById(R.id.txtNombre)
        txtTelefono1 = findViewById(R.id.txtTelefono1)
        txtTelefono2 = findViewById(R.id.txtTelefono2)
        txtDireccion = findViewById(R.id.txtDireccion)
        txtNotas = findViewById(R.id.txtNotas)
        cbkFavorite = findViewById(R.id.cbxFavorito)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnListar = findViewById(R.id.btnListar)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        savedContacto = null
    }

    private fun setEvents() {
        btnGuardar.setOnClickListener(this)
        btnListar.setOnClickListener(this)
        btnLimpiar.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (isNetworkAvailable()) {
            when (view.id) {
                R.id.btnGuardar -> {
                    var completo = true
                    if (txtNombre.text.toString().isEmpty()) {
                        txtNombre.error = "Introduce el Nombre"
                        completo = false
                    }
                    if (txtTelefono1.text.toString().isEmpty()) {
                        txtTelefono1.error = "Introduce el Telefono Principal"
                        completo = false
                    }
                    if (txtDireccion.text.toString().isEmpty()) {
                        txtDireccion.error = "Introduce la Direccion"
                        completo = false
                    }
                    if (completo) {
                        val nContacto = Contacts().apply {
                            nombre = txtNombre.text.toString()
                            telefono1 = txtTelefono1.text.toString()
                            telefono2 = txtTelefono2.text.toString()
                            direccion = txtDireccion.text.toString()
                            notas = txtNotas.text.toString()
                            favorite = if (cbkFavorite.isChecked) 1 else 0
                        }

                        if (savedContacto == null) {
                            agregarContacto(nContacto)
                            Toast.makeText(
                                applicationContext,
                                "Contacto guardado con exito",
                                Toast.LENGTH_SHORT
                            ).show()
                            limpiar()
                        } else {
                            actualizarContacto(id, nContacto)
                            Toast.makeText(
                                applicationContext,
                                "Contacto actualizado con exito",
                                Toast.LENGTH_SHORT
                            ).show()
                            limpiar()
                        }
                    }
                }

                R.id.btnLimpiar -> limpiar()

                R.id.btnListar -> {
                    val intent = Intent(this, ListActivity::class.java)
                    limpiar()
                    startActivityForResult(intent, 0)
                }
            }
        } else {
            Toast.makeText(
                applicationContext,
                "Se necesita tener conexion a internet",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun agregarContacto(c: Contacts) {
        val newContactoReference = referencia.push()
        // Obtener el id del registro y setearlo
        val id = newContactoReference.key
        c._ID = id
        newContactoReference.setValue(c)
    }

    private fun actualizarContacto(id: String, p: Contacts) {
        // Actualizar un objeto al nodo referencia
        p._ID = id
        referencia.child(id).setValue(p)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    private fun limpiar() {
        savedContacto = null
        txtNombre.text = ""
        txtTelefono1.text = ""
        txtTelefono2.text = ""
        txtNotas.text = ""
        txtDireccion.text = ""
        cbkFavorite.isChecked = false
        id = ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent != null) {
            val oBundle = intent.extras
            if (resultCode == RESULT_OK) {
                val contacto = oBundle?.getSerializable("contacto") as Contacts
                savedContacto = contacto
                id = contacto._ID ?: ""

                txtNombre.text = contacto.nombre
                txtTelefono1.text = contacto.telefono1
                txtTelefono2.text = contacto.telefono2
                txtDireccion.text = contacto.direccion
                txtNotas.text = contacto.notas
                cbkFavorite.isChecked = contacto.favorite > 0
            } else {
                limpiar()
            }
        }
    }
}
