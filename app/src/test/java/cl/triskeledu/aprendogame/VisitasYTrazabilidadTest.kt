package cl.triskeledu.aprendogame

import cl.triskeledu.aprendogame.model.DescubrimientoQr
import cl.triskeledu.aprendogame.model.VideoVisto
import cl.triskeledu.aprendogame.model.VisitaRealizada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Suite 3 (Frontend): Pruebas de trazabilidad de visitas, descubrimientos QR y selfies turísticas.
 * Contiene 5 pruebas unitarias.
 */
class VisitasYTrazabilidadTest {

    @Test
    fun testVisitaRealizada_creacionExitosaConEvidenciaSelfie() {
        val timestamp = System.currentTimeMillis()
        val visita = VisitaRealizada(
            id = 1,
            puntoId = 3,
            usuarioId = 10,
            capsulaId = 2,
            rutaFotoSelfie = "/data/user/0/app/selfie_moneda.jpg",
            fechaTimestamp = timestamp,
            completada = true
        )
        assertEquals(3, visita.puntoId)
        assertEquals(10, visita.usuarioId)
        assertEquals("/data/user/0/app/selfie_moneda.jpg", visita.rutaFotoSelfie)
        assertTrue(visita.completada)
    }

    @Test
    fun testDescubrimientoQr_registroYTimestamp() {
        val now = System.currentTimeMillis()
        val descubrimiento = DescubrimientoQr(
            id = 1,
            usuarioId = 5,
            capsulaId = 8,
            fechaTimestamp = now
        )
        assertEquals(5, descubrimiento.usuarioId)
        assertEquals(8, descubrimiento.capsulaId)
        assertTrue(descubrimiento.fechaTimestamp > 0)
    }

    @Test
    fun testVideoVisto_registroVisualizacionCapsula() {
        val video = VideoVisto(
            id = 1,
            usuarioId = 5,
            capsulaId = 8,
            fechaTimestamp = System.currentTimeMillis()
        )
        assertEquals(5, video.usuarioId)
        assertEquals(8, video.capsulaId)
    }

    @Test
    fun testVisitaRealizada_verificacionIdPuntoValido() {
        val visita = VisitaRealizada(puntoId = 4, usuarioId = 1, completada = true)
        assertTrue(visita.puntoId > 0)
        assertTrue(visita.usuarioId > 0)
    }

    @Test
    fun testDescubrimientoQr_inmutabilidadDeDatosGenerados() {
        val descOriginal = DescubrimientoQr(usuarioId = 1, capsulaId = 2)
        val copia = descOriginal.copy(usuarioId = 2)
        assertEquals(1, descOriginal.usuarioId)
        assertEquals(2, copia.usuarioId)
        assertEquals(2, descOriginal.capsulaId)
    }
}
