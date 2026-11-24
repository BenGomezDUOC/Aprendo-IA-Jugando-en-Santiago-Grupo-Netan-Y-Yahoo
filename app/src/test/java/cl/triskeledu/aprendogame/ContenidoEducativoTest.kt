package cl.triskeledu.aprendogame

import cl.triskeledu.aprendogame.model.ContenidoEducativo
import cl.triskeledu.aprendogame.model.VideoRegistrado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Suite 4 (Frontend): Pruebas de contenidos educativos y gestión multimedia para roles de administración.
 * Contiene 5 pruebas unitarias.
 */
class ContenidoEducativoTest {

    @Test
    fun testContenidoEducativo_asociacionCorrectaConPuntoTuristico() {
        val contenido = ContenidoEducativo(
            id = 1,
            titulo = "Historia de la IA en La Moneda",
            descripcion = "Cápsula patrimonial sobre automatización",
            categoria = "Inteligencia Artificial",
            puntoId = 4,
            estado = "PUBLICADA"
        )
        assertEquals("Historia de la IA en La Moneda", contenido.titulo)
        assertEquals(4, contenido.puntoId)
        assertEquals("PUBLICADA", contenido.estado)
    }

    @Test
    fun testContenidoEducativo_soportaContenidoSinPuntoAsociado() {
        val contenidoLibre = ContenidoEducativo(
            id = 2,
            titulo = "Conceptos Básicos de Machine Learning",
            descripcion = "Video introductorio general",
            categoria = "Machine Learning",
            puntoId = null,
            estado = "BORRADOR"
        )
        assertNull(contenidoLibre.puntoId)
        assertEquals("BORRADOR", contenidoLibre.estado)
    }

    @Test
    fun testVideoRegistrado_vinculacionConContenidoEducativo() {
        val video = VideoRegistrado(
            id = 100,
            contenidoId = 1,
            titulo = "Video Oficial La Moneda",
            descripcion = "Explicación institucional",
            urlVideo = "https://triskeledu.cl/videos/moneda.mp4",
            autor = "Admin",
            duracion = 180000L
        )
        assertEquals(1, video.contenidoId)
        assertEquals(180000L, video.duracion)
        assertEquals("https://triskeledu.cl/videos/moneda.mp4", video.urlVideo)
        assertNotNull(video.autor)
    }

    @Test
    fun testContenidoEducativo_actualizacionDeDatosPreservandoId() {
        val original = ContenidoEducativo(
            id = 1,
            titulo = "Título Viejo",
            descripcion = "Desc",
            categoria = "IA",
            estado = "BORRADOR"
        )
        val editado = original.copy(titulo = "Título Nuevo", estado = "PUBLICADA")
        assertEquals(1, editado.id)
        assertEquals("Título Nuevo", editado.titulo)
        assertEquals("PUBLICADA", editado.estado)
    }

    @Test
    fun testVideoRegistrado_duracionPositiva() {
        val video = VideoRegistrado(
            id = 101,
            contenidoId = 2,
            titulo = "Video Corto",
            urlVideo = "https://example.com/corto.mp4",
            duracion = 45000L
        )
        assert(video.duracion > 0L)
    }
}
