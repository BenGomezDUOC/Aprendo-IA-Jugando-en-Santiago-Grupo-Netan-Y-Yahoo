
package cl.triskeledu.aprendogame.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64

import org.json.JSONObject

import java.security.KeyStore

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestiona la sesión local y la sesión autenticada
 * de Spring Boot.
 *
 * Mantiene separados:
 * - El ID del usuario en Room.
 * - El ID del usuario en MySQL.
 * - El token JWT del backend.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    // --------------------------------------------
    // SESIÓN LOCAL DE ROOM
    // --------------------------------------------

    fun guardarUsuarioActivo(id: Int) {
        require(id > 0) {
            "El ID local debe ser positivo"
        }

        val idAnterior = prefs.getInt(KEY_USUARIO_ID, -1)
        val editor = prefs.edit()

        // Al cambiar de jugador, nunca reutilizar
        // el token de otra cuenta.
        if (idAnterior != id) {
            borrarDatosBackend(editor)
        }

        editor.putInt(KEY_USUARIO_ID, id).apply()
    }

    fun obtenerUsuarioActivo(): Int? {
        val id = prefs.getInt(KEY_USUARIO_ID, -1)

        if (id <= 0) return null

        // Si esta cuenta utiliza el backend,
        // necesita un token todavía vigente.
        if (prefs.getBoolean(KEY_BACKEND_ACTIVO, false)) {
            if (obtenerTokenBackend() == null) {
                return null
            }
        }

        return id
    }

    // --------------------------------------------
    // GUARDAR SESIÓN DEL BACKEND
    // --------------------------------------------

    fun guardarSesionBackend(
        usuarioLocalId: Int,
        usuarioBackendId: Long,
        token: String,
        rol: String
    ) {
        require(usuarioLocalId > 0) {
            "El ID de Room no es válido"
        }

        require(usuarioBackendId > 0L) {
            "El ID del backend no es válido"
        }

        require(token.isNotBlank() && !tokenExpirado(token)) {
            "El token JWT no es válido o está vencido"
        }

        require(
            rol == "CLIENTE" ||
                    rol == "ADMINISTRADOR" ||
                    rol == "GENERADOR"
        ) {
            "Rol de usuario no reconocido"
        }

        val tokenCifrado = cifrarToken(token)

        prefs.edit()
            .putInt(KEY_USUARIO_ID, usuarioLocalId)
            .putLong(KEY_BACKEND_ID, usuarioBackendId)
            .putString(KEY_BACKEND_ROL, rol)
            .putString(KEY_BACKEND_TOKEN, tokenCifrado)
            .putBoolean(KEY_BACKEND_ACTIVO, true)
            .apply()
    }

    // --------------------------------------------
    // OBTENER JWT
    // --------------------------------------------

    fun obtenerTokenBackend(): String? {
        val cifrado = prefs.getString(
            KEY_BACKEND_TOKEN,
            null
        ) ?: return null

        return try {
            val token = descifrarToken(cifrado)

            if (tokenExpirado(token)) {
                null
            } else {
                token
            }

        } catch (e: Exception) {
            null
        }
    }

    fun obtenerAuthorizationHeader(): String? {
        val token = obtenerTokenBackend() ?: return null
        return "Bearer $token"
    }

    // --------------------------------------------
    // DATOS DEL USUARIO DEL BACKEND
    // --------------------------------------------

    fun obtenerUsuarioBackendId(): Long? {
        if (obtenerTokenBackend() == null) return null

        val id = prefs.getLong(KEY_BACKEND_ID, -1L)

        return id.takeIf { it > 0L }
    }

    fun obtenerRolBackend(): String? {
        if (obtenerTokenBackend() == null) return null

        return prefs.getString(
            KEY_BACKEND_ROL,
            null
        )
    }

    fun tieneSesionBackendVigente(): Boolean {
        return obtenerUsuarioBackendId() != null
    }

    // --------------------------------------------
    // CERRAR SESIÓN
    // --------------------------------------------

    fun cerrarSesion() {
        prefs.edit()
            .remove(KEY_USUARIO_ID)
            .apply()

        prefs.edit().also { editor ->
            borrarDatosBackend(editor)
            editor.apply()
        }
    }

    private fun borrarDatosBackend(
        editor: SharedPreferences.Editor
    ) {
        editor.remove(KEY_BACKEND_ID)
        editor.remove(KEY_BACKEND_ROL)
        editor.remove(KEY_BACKEND_TOKEN)
        editor.remove(KEY_BACKEND_ACTIVO)
    }

    // --------------------------------------------
    // COMPROBAR VENCIMIENTO DEL JWT
    // --------------------------------------------

    private fun tokenExpirado(token: String): Boolean {
        return try {
            val partes = token.split(".")

            if (partes.size != 3) return true

            val contenido = Base64.decode(
                partes[1],
                Base64.URL_SAFE or
                        Base64.NO_WRAP or
                        Base64.NO_PADDING
            )

            val datos = JSONObject(
                String(contenido, Charsets.UTF_8)
            )

            val expiracion = datos.optLong("exp", 0L)

            if (expiracion <= 0L) return true

            val ahora = System.currentTimeMillis() / 1000L

            // Margen de 30 segundos.
            ahora >= expiracion - 30L

        } catch (e: Exception) {
            true
        }
    }

    // --------------------------------------------
    // CIFRAR TOKEN CON ANDROID KEYSTORE
    // --------------------------------------------

    private fun obtenerClave(): SecretKey {

        val almacen = KeyStore.getInstance(
            "AndroidKeyStore"
        )

        almacen.load(null)

        val existente = almacen.getKey(
            KEYSTORE_ALIAS,
            null
        ) as? SecretKey

        if (existente != null) {
            return existente
        }

        val generador = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val configuracion =
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()

        generador.init(configuracion)

        return generador.generateKey()
    }

    private fun cifrarToken(token: String): String {

        val cipher = Cipher.getInstance(
            "AES/GCM/NoPadding"
        )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            obtenerClave()
        )

        val datosCifrados = cipher.doFinal(
            token.toByteArray(Charsets.UTF_8)
        )

        val resultado = cipher.iv + datosCifrados

        return Base64.encodeToString(
            resultado,
            Base64.NO_WRAP
        )
    }

    private fun descifrarToken(
        valorCifrado: String
    ): String {

        val contenido = Base64.decode(
            valorCifrado,
            Base64.NO_WRAP
        )

        require(contenido.size > 28) {
            "Token cifrado inválido"
        }

        val iv = contenido.copyOfRange(0, 12)

        val datos = contenido.copyOfRange(
            12,
            contenido.size
        )

        val cipher = Cipher.getInstance(
            "AES/GCM/NoPadding"
        )

        cipher.init(
            Cipher.DECRYPT_MODE,
            obtenerClave(),
            GCMParameterSpec(128, iv)
        )

        val resultado = cipher.doFinal(datos)

        return String(resultado, Charsets.UTF_8)
    }

    companion object {

        private const val PREFS_NAME =
            "sesion_usuario_prefs"

        private const val KEY_USUARIO_ID =
            "usuario_activo_id"

        private const val KEY_BACKEND_ID =
            "backend_usuario_id"

        private const val KEY_BACKEND_ROL =
            "backend_usuario_rol"

        private const val KEY_BACKEND_TOKEN =
            "backend_token_cifrado"

        private const val KEY_BACKEND_ACTIVO =
            "backend_sesion_activa"

        private const val KEYSTORE_ALIAS =
            "triskeledu_clave_sesion"
    }
}
