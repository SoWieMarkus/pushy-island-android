package markus.wieland.pushygame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import markus.wieland.pushygame.render.LocalSprites
import markus.wieland.pushygame.ui.PushyNavigation
import markus.wieland.pushygame.ui.PushyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Full-screen game: hide the system bars, show them temporarily on swipe.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        val sprites = appContainer.sprites
        setContent {
            PushyTheme {
                CompositionLocalProvider(LocalSprites provides sprites) {
                    // Surface provides the background colour and light text for all screens.
                    Surface(color = MaterialTheme.colorScheme.background) {
                        PushyNavigation()
                    }
                }
            }
        }
    }
}
