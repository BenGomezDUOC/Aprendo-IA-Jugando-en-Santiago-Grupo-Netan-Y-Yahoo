package cl.triskeledu.aprendogame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AzulTriskeledu,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = CelesteAcento,
    onSecondary = AzulOscuro,
    background = FondoApp,
    onBackground = TextoPrincipal,
    surface = FondoTarjeta,
    onSurface = TextoPrincipal,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextoSecundario,
    error = RojoError,
    onError = Color.White
)

@Composable
fun AprendoIAJugandoEnSantiagoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
