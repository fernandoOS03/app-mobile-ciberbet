package com.cibertec.ciberbet.models

data class UsuarioFirebase(
    val idUsuario: String = "",
    val nombres: String = "",
    val dni: String = "",
    val telefono: String = "",
    val correo: String = "",
    val password: String = "",
    val flgEli: Boolean = false
)
