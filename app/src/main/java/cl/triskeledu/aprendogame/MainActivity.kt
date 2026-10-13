package cl.triskeledu.aprendogame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.triskeledu.aprendogame.navigation.AppNavigation
import cl.triskeledu.aprendogame.ui.theme.AprendoIAJugandoEnSantiagoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { //
            AprendoIAJugandoEnSantiagoTheme {
                AppNavigation()
            }
        }
    }
}