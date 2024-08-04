package com.pmv.agendafirebase

import android.app.Activity
import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*
import com.pmv.agendafirebase.models.Contacts
import com.pmv.agendafirebase.models.FirebaseReferences

class ListActivity : ListActivity() {

    private lateinit var basedatabase: FirebaseDatabase
    private lateinit var referencia: DatabaseReference
    private lateinit var btnNuevo: Button
    private val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        basedatabase = FirebaseDatabase.getInstance()
        referencia = basedatabase.getReferenceFromUrl(
            "${FirebaseReferences.URL_DATABASE}${FirebaseReferences.DATABASE_NAME}/${FirebaseReferences.TABLE_NAME}"
        )
        btnNuevo = findViewById(R.id.btnNuevo)
        obtenerContactos()
        btnNuevo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun obtenerContactos() {
        val contactos = ArrayList<Contacts>()

        val listener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val contacto = dataSnapshot.getValue(Contacts::class.java)
                if (contacto != null) {
                    contactos.add(contacto)
                }
                val adapter = MyArrayAdapter(context, R.layout.layout_contacto, contactos)
                listAdapter = adapter
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }

        referencia.addChildEventListener(listener)
    }

    inner class MyArrayAdapter(
        context: Context,
        private val textViewResourceId: Int,
        private val objects: ArrayList<Contacts>
    ) : ArrayAdapter<Contacts>(context, textViewResourceId, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(textViewResourceId, null)
            val lblNombre = view.findViewById<TextView>(R.id.lblNombreContacto)
            val lblTelefono = view.findViewById<TextView>(R.id.lblTelefonoContacto)
            val btnModificar = view.findViewById<Button>(R.id.btnModificar)
            val btnBorrar = view.findViewById<Button>(R.id.btnBorrar)

            val contacto = objects[position]

            if (contacto.favorite > 0) {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            } else {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            }

            lblNombre.text = contacto.nombre
            lblTelefono.text = contacto.telefono1

            btnBorrar.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Confirmación")
                    .setMessage("¿Está seguro de que desea borrar a ${contacto.nombre}?")
                    .setPositiveButton("Sí") { _, _ ->
                        borrarContacto(contacto._ID!!)
                        objects.removeAt(position)
                        notifyDataSetChanged()
                        Toast.makeText(context, "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            btnModificar.setOnClickListener {
                val oBundle = Bundle()
                oBundle.putSerializable("contacto", contacto)
                val i = Intent()
                i.putExtras(oBundle)
                setResult(Activity.RESULT_OK, i)
                finish()
            }

            return view
        }
    }

    private fun borrarContacto(childIndex: String) {
        referencia.child(childIndex).removeValue()
    }
}
