package com.pmv.agendafirebase.models

import java.io.Serializable

class Contacts(
    var _ID: String? = null,
    var nombre: String? = null,
    var telefono1: String? = null,
    var telefono2: String? = null,
    var direccion: String? = null,
    var notas: String? = null,
    var favorite: Int = 0
) : Serializable
