package markus.wieland.pushygame.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import markus.wieland.pushygame.R

/** Pixel font from the original game, used for headings and labels. */
val PixelFont = FontFamily(Font(R.font.press_start_2p))

object PushyColors {
    val Ocean = Color(0xFF7DAED6)
    val DeepOcean = Color(0xFF2F5F8A)
    val Sand = Color(0xFFDDD0A0)
    val Palm = Color(0xFF5E9A45)
    val Coral = Color(0xFFE8734A)
    val Night = Color(0xFF14243A)
    val Panel = Color(0xCC14243A)
}

private val colors = darkColorScheme(
    primary = PushyColors.Sand,
    onPrimary = PushyColors.Night,
    secondary = PushyColors.Palm,
    onSecondary = Color.White,
    tertiary = PushyColors.Coral,
    background = PushyColors.DeepOcean,
    onBackground = Color.White,
    surface = PushyColors.Night,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF223A57),
    onSurfaceVariant = Color(0xFFD5E2F0),
    surfaceContainer = Color(0xFF1B2F48),
    surfaceContainerHigh = Color(0xFF223A57),
    error = Color(0xFFFF8A80),
)

private val typography = Typography().run {
    copy(
        displaySmall = TextStyle(fontFamily = PixelFont, fontSize = 32.sp, lineHeight = 40.sp),
        headlineSmall = TextStyle(fontFamily = PixelFont, fontSize = 18.sp, lineHeight = 26.sp),
        titleMedium = TextStyle(fontFamily = PixelFont, fontSize = 12.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = PixelFont, fontSize = 11.sp, lineHeight = 16.sp),
    )
}

@Composable
fun PushyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
