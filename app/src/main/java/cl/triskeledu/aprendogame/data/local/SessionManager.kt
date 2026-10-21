package cl.triskeledu.aprendogame.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de persistencia de sesión de usuario en SharedPreferences.
 * Permite que la app recuerde al usuario autenticado entre reinicios.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun guardarUsuarioActivo(id: Int) {
        prefs.edit().putInt(KEY_USUARIO_ID, id).apply()
    }

    fun obtenerUsuarioActivo(): Int? {
        val id = prefs.getInt(KEY_USUARIO_ID, -1)
        return if (id != -1) id else null
    }

    fun cerrarSesion() {
        prefs.edit().remove(KEY_USUARIO_ID).apply()
    }

    companion object {
        private const val PREFS_NAME = "sesion_usuario_prefs"
        private const val KEY_USUARIO_ID = "usuario_activo_id"
    }
}
