package cl.triskeledu.aprendogame

import cl.triskeledu.aprendogame.validation.FormularioValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Suite 1 (Frontend): Pruebas unitarias para validar la lógica desacoplada de formularios (IE 2.2.1).
 * Contiene 5 pruebas enfocadas en validaciones de entrada.
 */
class FormularioValidatorTest {

    @Test
    fun testValidarNombre_rechazaNombresInvalidos_yAceptaValidos() {
        assertNotNull(FormularioValidator.validarNombre(""))
        assertNotNull(FormularioValidator.validarNombre("   "))
        assertNotNull(FormularioValidator.validarNombre("Al"))
        assertNull(FormularioValidator.validarNombre("Fernando Alarcón"))
    }

    @Test
    fun testValidarAlias_validaLongitudMinimaYMaxima() {
        assertNotNull(FormularioValidator.validarAlias(""))
        assertNotNull(FormularioValidator.validarAlias("x"))
        assertNull(FormularioValidator.validarAlias("fer_ia"))
    }

    @Test
    fun testValidarCorreo_verificaEstructuraEstrictaEmail() {
        assertNotNull(FormularioValidator.validarCorreo(""))
        assertNotNull(FormularioValidator.validarCorreo("correoinvalido"))
        assertNotNull(FormularioValidator.validarCorreo("usuario@dominio"))
        assertNull(FormularioValidator.validarCorreo("estudiante@triskeledu.cl"))
    }

    @Test
    fun testValidarTelefono_exigeEntre8y15Digitos() {
        assertNotNull(FormularioValidator.validarTelefono(""))
        assertNotNull(FormularioValidator.validarTelefono("123"))
        assertNull(FormularioValidator.validarTelefono("+56912345678"))
        assertNull(FormularioValidator.validarTelefono("912345678"))
    }

    @Test
    fun testValidarFormularioCompleto_verificaEstadoGlobal() {
        val formValido = FormularioValidator.validarFormularioCompleto(
            nombre = "Fernando Alarcón",
            alias = "FerIA",
            correo = "fernando@triskeledu.cl",
            telefono = "987654321",
            contrasena = "Santiago2026",
            confirmarContrasena = "Santiago2026"
        )
        assertTrue(formValido.esValido)
        assertNull(formValido.errorNombre)

        val formInvalido = FormularioValidator.validarFormularioCompleto(
            nombre = "F",
            alias = "F",
            correo = "correo_malo",
            telefono = "12",
            contrasena = "corta",
            confirmarContrasena = "distinta"
        )
        assertFalse(formInvalido.esValido)
        assertNotNull(formInvalido.errorContrasena)
    }
}
