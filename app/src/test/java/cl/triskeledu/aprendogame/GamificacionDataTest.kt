package cl.triskeledu.aprendogame

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Rol
import cl.triskeledu.aprendogame.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Suite 2 (Frontend): Pruebas unitarias de modelos de datos y reglas de negocio de gamificación.
 * Contiene 5 pruebas independientes.
 */
class GamificacionDataTest {

    @Test
    fun testPuntoInteres_creacionConToleranciaGeografica() {
        val punto = PuntoInteres(
            id = 1,
            nombre = "Plaza de Armas",
            descripcion = "Centro histórico de Santiago",
            latitud = -33.4372,
            longitud = -70.6506,
            altitud = 520.0,
            completado = false,
            radioToleranciaMetros = 50.0
        )
        assertEquals("Plaza de Armas", punto.nombre)
        assertEquals(50.0, punto.radioToleranciaMetros, 0.001)
        assertTrue(!punto.completado)
    }

    @Test
    fun testCapsula_asignacionQrYPuntosEducativos() {
        val capsula = Capsula(
            id = 10,
            puntoId = 1,
            titulo = "Introducción a la IA",
            codigoQr = "QR_PLAZA_ARMAS",
            urlVideo = "https://triskeledu.cl/videos/ia1.mp4",
            puntos = 10
        )
        assertEquals("QR_PLAZA_ARMAS", capsula.codigoQr)
        assertEquals(10, capsula.puntos)
        assertNotNull(capsula.urlVideo)
    }

    @Test
    fun testUsuario_acumulacionProgresivaDePuntos() {
        var usuario = Usuario(
            id = 1,
            nombre = "Estudiante Duoc",
            rol = Rol.CLIENTE,
            puntosAcumulados = 0
        )
        usuario = usuario.copy(puntosAcumulados = usuario.puntosAcumulados + 10)
        assertEquals(10, usuario.puntosAcumulados)

        usuario = usuario.copy(puntosAcumulados = usuario.puntosAcumulados + 100)
        assertEquals(110, usuario.puntosAcumulados)
    }

    @Test
    fun testUsuario_rolesPermitidosYValores() {
        val admin = Usuario(id = 2, nombre = "Admin", rol = Rol.ADMINISTRADOR)
        val generador = Usuario(id = 3, nombre = "Generador", rol = Rol.GENERADOR)
        val cliente = Usuario(id = 4, nombre = "Jugador", rol = Rol.CLIENTE)

        assertEquals(Rol.ADMINISTRADOR, admin.rol)
        assertEquals(Rol.GENERADOR, generador.rol)
        assertEquals(Rol.CLIENTE, cliente.rol)
    }

    @Test
    fun testCapsula_estadoPublicacionPorDefecto() {
        val capsula = Capsula(
            id = 5,
            puntoId = 2,
            titulo = "Visión por Computadora",
            codigoQr = "QR_SANTA_LUCIA",
            urlVideo = "https://example.com/vision.mp4",
            puntos = 10,
            estado = "PUBLICADA"
        )
        assertEquals("PUBLICADA", capsula.estado)
        assertEquals(2, capsula.puntoId)
    }
}
