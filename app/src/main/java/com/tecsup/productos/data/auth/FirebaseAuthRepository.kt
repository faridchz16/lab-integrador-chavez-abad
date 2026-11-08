package com.tecsup.productos.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.productos.domain.model.ErrorAutenticacion
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> = try {
        val usuario = auth.signInWithEmailAndPassword(correo, clave).await().user
            ?: throw ErrorAutenticacion.CredencialesInvalidas()
        Result.success(leerSesion(usuario.uid, usuario.email ?: correo))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(traducir(e))
    }

    override suspend fun sesionActual(): Sesion? {
        val usuario = auth.currentUser ?: return null
        return leerSesion(usuario.uid, usuario.email.orEmpty())
    }

    override fun cerrarSesion() = auth.signOut()

    private suspend fun leerSesion(uid: String, correo: String): Sesion {
        val rol = try {
            db.collection(COLECCION_USUARIOS).document(uid).get().await().getString(CAMPO_ROL)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        return Sesion(
            uid = uid,
            correo = correo,
            rol = if (rol == ROL_ADMIN) Rol.ADMIN else Rol.USUARIO
        )
    }

    private fun traducir(error: Exception): ErrorAutenticacion = when (error) {
        is ErrorAutenticacion -> error
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> ErrorAutenticacion.CredencialesInvalidas()
        is FirebaseTooManyRequestsException -> ErrorAutenticacion.DemasiadosIntentos()
        is FirebaseNetworkException -> ErrorAutenticacion.SinConexion(error)
        else -> ErrorAutenticacion.Desconocido(error)
    }

    private companion object {
        const val COLECCION_USUARIOS = "usuarios"
        const val CAMPO_ROL = "rol"
        const val ROL_ADMIN = "admin"
    }
}
