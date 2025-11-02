package com.cibertec.ciberbet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cibertec.ciberbet.models.Usuario

@Dao
interface UsuarioDAO {

    @Insert
    fun addUsuario(usuarioEntity: Usuario)

    @Update
    fun updateUsuario(usuarioEntity: Usuario)

    @Query("UPDATE usuario SET flgEli = 1 WHERE id = :usuarioId")
    fun deleteUsuario(usuarioId: Int)

    @Query("SELECT * FROM usuario WHERE correo = :correoUsu AND password = :passwUs AND flgEli = 0 LIMIT 1")
    fun getUsuario(correoUsu: String, passwUs: String): Usuario?
    @Query("SELECT * FROM usuario WHERE id = :idUsuario LIMIT 1")
    fun getUsuarioPorId(idUsuario:Int):Usuario?

}