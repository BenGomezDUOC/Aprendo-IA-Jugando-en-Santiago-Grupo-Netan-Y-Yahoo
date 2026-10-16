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
 * Pruebas unitarias de las entidades y modelos de datos de gamificación
 * solicitadas en el documento del caso de Triskeledu SpA.
 */
class GamificacionDataTest {

    @Test
    fun `verificar creacion de punto turistico con radio de tolerancia`() {
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
    fun `verificar asignacion de capsula de IA con codigo QR y puntos`() {
        val capsula = Capsula(
            id = 1,
            puntoId = 1,
            titulo = "IA en la Historia Urbana",
            codigoQr = "QR_PLAZA_ARMAS",
            urlVideo = "https://example.com/video.mp4",
            puntos = 100
        )

        assertEquals("QR_PLAZA_ARMAS", capsula.codigoQr)
        assertEquals(100, capsula.puntos)
        assertNotNull(capsula.urlVideo)
    }

    @Test
    fun `verificar incremento de puntos de gamificacion para usuario cliente`() {
        var usuario = Usuario(
            id = 1,
            nombre = "Estudiante Duoc",
            rol = Rol.CLIENTE,
            puntosAcumulados = 0
        )

        // Simulación de completar 2 cápsulas de IA (100 pts + 150 pts)
        val recompensa1 = 100
        val recompensa2 = 150

        usuario = usuario.copy(puntosAcumulados = usuario.puntosAcumulados + recompensa1)
        assertEquals(100, usuario.puntosAcumulados)

        usuario = usuario.copy(puntosAcumulados = usuario.puntosAcumulados + recompensa2)
        assertEquals(250, usuario.puntosAcumulados)
        assertEquals(Rol.CLIENTE, usuario.rol)
    }
}
