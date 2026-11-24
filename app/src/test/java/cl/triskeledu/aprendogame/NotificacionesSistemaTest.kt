package cl.triskeledu.aprendogame

import cl.triskeledu.aprendogame.model.Notificacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Suite 5 (Frontend): Pruebas de notificaciones del sistema y experiencia del usuario.
 * Contiene 5 pruebas unitarias.
 */
class NotificacionesSistemaTest {

    @Test
    fun testNotificacion_creacionConEstadoNoLeido() {
        val timestamp = System.currentTimeMillis()
        val notif = Notificacion(
            id = 1,
            usuarioId = 10,
            titulo = "¡Nuevo punto desbloqueado!",
            mensaje = "Has completado la cápsula de Plaza de Armas.",
            tipo = "PUNTO_DESBLOQUEADO",
            rutaDestino = "mapa",
            fechaTimestamp = timestamp,
            leida = false
        )
        assertEquals("¡Nuevo punto desbloqueado!", notif.titulo)
        assertFalse(notif.leida)
        assertEquals("PUNTO_DESBLOQUEADO", notif.tipo)
    }

    @Test
    fun testNotificacion_marcarComoLeida() {
        val notif = Notificacion(
            id = 2,
            usuarioId = 10,
            titulo = "Premio disponible",
            mensaje = "Tienes puntos suficientes para canjear un sticker.",
            tipo = "PREMIO",
            rutaDestino = "coleccion",
            leida = false
        )
        val notifLeida = notif.copy(leida = true)
        assertTrue(notifLeida.leida)
        assertEquals(notif.id, notifLeida.id)
    }

    @Test
    fun testNotificacion_destinoRutaConfigurado() {
        val notifConRuta = Notificacion(
            id = 3,
            usuarioId = 10,
            titulo = "Revisa tu mapa",
            mensaje = "Nuevos hitos agregados",
            tipo = "AVISO",
            rutaDestino = "mapa"
        )
        assertEquals("mapa", notifConRuta.rutaDestino)

        val notifGeneral = Notificacion(
            id = 4,
            usuarioId = 10,
            titulo = "Aviso general",
            mensaje = "Mantenimiento del servidor",
            tipo = "SISTEMA",
            rutaDestino = ""
        )
        assertEquals("", notifGeneral.rutaDestino)
    }

    @Test
    fun testNotificacion_asociacionAUsuarioEspecifico() {
        val notifUser1 = Notificacion(usuarioId = 1, titulo = "Bienvenida", mensaje = "Hola 1", tipo = "INFO", rutaDestino = "")
        val notifUser2 = Notificacion(usuarioId = 2, titulo = "Bienvenida", mensaje = "Hola 2", tipo = "INFO", rutaDestino = "")
        assertEquals(1, notifUser1.usuarioId)
        assertEquals(2, notifUser2.usuarioId)
    }

    @Test
    fun testNotificacion_validarMensajeNoVacio() {
        val notif = Notificacion(
            usuarioId = 1,
            titulo = "Alerta",
            mensaje = "Contenido de notificación importante",
            tipo = "ALERTA",
            rutaDestino = ""
        )
        assertNotNull(notif.mensaje)
        assertTrue(notif.mensaje.isNotBlank())
    }
}
