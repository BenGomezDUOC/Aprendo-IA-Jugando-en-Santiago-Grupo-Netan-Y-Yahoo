package cl.triskeledu.aprendogame.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtils {

    private const val ITERACIONES = 210_000
    private const val LONGITUD_CLAVE = 256
    private const val ALGORITMO = "PBKDF2WithHmacSHA256"

    // Protege la contraseña antes de guardarla en Room.
    fun crearHash(contrasena: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val hash = calcularHash(contrasena, salt, ITERACIONES)

        val saltTexto = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashTexto = Base64.encodeToString(hash, Base64.NO_WRAP)

        return "$ITERACIONES:$saltTexto:$hashTexto"
    }

    // Comprueba una contraseña al iniciar sesión.
    fun verificarContrasena(
        contrasena: String,
        hashGuardado: String
    ): Boolean {
        return try {
            val partes = hashGuardado.split(":")
            if (partes.size != 3) return false

            val iteraciones = partes[0].toInt()
            if (iteraciones != ITERACIONES) return false

            val salt = Base64.decode(partes[1], Base64.NO_WRAP)
            val hashOriginal = Base64.decode(partes[2], Base64.NO_WRAP)
            val hashCalculado = calcularHash(
                contrasena,
                salt,
                iteraciones
            )

            java.security.MessageDigest.isEqual(
                hashOriginal,
                hashCalculado
            )
        } catch (e: Exception) {
            false
        }
    }

    private fun calcularHash(
        contrasena: String,
        salt: ByteArray,
        iteraciones: Int
    ): ByteArray {
        val especificacion = PBEKeySpec(
            contrasena.toCharArray(),
            salt,
            iteraciones,
            LONGITUD_CLAVE
        )

        return try {
            SecretKeyFactory
                .getInstance(ALGORITMO)
                .generateSecret(especificacion)
                .encoded
        } finally {
            especificacion.clearPassword()
        }
    }
}
