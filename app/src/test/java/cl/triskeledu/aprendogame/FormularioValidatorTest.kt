package cl.triskeledu.aprendogame

import cl.triskeledu.aprendogame.validation.FormularioValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validar la lógica desacoplada de formularios (IE 2.2.1).
 * Garantiza que las reglas de negocio no dependan de componentes gráficos de Android.
 */
class FormularioValidatorTest {

    @Test
    fun `validarNombre rechaza nombres en blanco o menores a 3 caracteres`() {
        assertNotNull(FormularioValidator.validarNombre(""))
        assertNotNull(FormularioValidator.validarNombre("   "))
        assertNotNull(FormularioValidator.validarNombre("Al"))
        assertNull(FormularioValidator.validarNombre("Benjamín Gómez"))
    }

    @Test
    fun `validarAlias exige al menos 3 caracteres`() {
        assertNotNull(FormularioValidator.validarAlias("x"))
        assertNull(FormularioValidator.validarAlias("benja_dev"))
    }

    @Test
    fun `validarCorreo verifica formato estandar de email`() {
        assertNotNull(FormularioValidator.validarCorreo("correoinvalido"))
        assertNotNull(FormularioValidator.validarCorreo("sin_arroba.cl"))
        assertNull(FormularioValidator.validarCorreo("estudiante@duocuc.cl"))
    }

    @Test
    fun `validarTelefono exige entre 8 y 15 digitos numericos`() {
        assertNotNull(FormularioValidator.validarTelefono("123"))
        assertNull(FormularioValidator.validarTelefono("+56912345678"))
        assertNull(FormularioValidator.validarTelefono("912345678"))
    }

    @Test
    fun `validarContrasena exige longitud minima de 8 caracteres`() {
        assertNotNull(FormularioValidator.validarContrasena("1234567"))
        assertNull(FormularioValidator.validarContrasena("passwordSegura123"))
    }

    @Test
    fun `validarConfirmacion comprueba coincidencia exacta de claves`() {
        assertNotNull(FormularioValidator.validarConfirmacion("clave1234", "claveDiferente"))
        assertNull(FormularioValidator.validarConfirmacion("clave1234", "clave1234"))
    }

    @Test
    fun `validarFormularioCompleto aprueba cuando todos los campos son correctos`() {
        val resultado = FormularioValidator.validarFormularioCompleto(
            nombre = "Benjamín Gómez",
            alias = "Benja",
            correo = "benjamin@duocuc.cl",
            telefono = "987654321",
            contrasena = "secreta123",
            confirmarContrasena = "secreta123"
        )
        assertTrue(resultado.esValido)
        assertNull(resultado.errorNombre)
        assertNull(resultado.errorCorreo)
    }

    @Test
    fun `validarFormularioCompleto falla si hay un campo invalido`() {
        val resultado = FormularioValidator.validarFormularioCompleto(
            nombre = "B",
            alias = "Benja",
            correo = "correo_malo",
            telefono = "123",
            contrasena = "corta",
            confirmarContrasena = "diferente"
        )
        assertFalse(resultado.esValido)
        assertNotNull(resultado.errorNombre)
        assertNotNull(resultado.errorCorreo)
        assertNotNull(resultado.errorTelefono)
        assertNotNull(resultado.errorContrasena)
        assertNotNull(resultado.errorConfirmacion)
    }
}
