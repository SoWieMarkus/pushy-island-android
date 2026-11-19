package markus.wieland.pushygame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Notice-board dialog in the style of the game: a wooden panel with a pixel title, free
 * [content] and plank buttons. [dismissText] adds a second button that calls [onDismissRequest].
 */
@Composable
fun PixelDialog(
    onDismissRequest: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    dismissText: String? = null,
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Dialog(onDismissRequest, DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier
                .padding(24.dp)
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .pixelPanel(seams = false)
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            if (title != null) {
                Text(title, style = pixelTextStyle(14.sp, PushyColors.Sand, shadow = 4f))
                Spacer(Modifier.height(16.dp))
            }
            content()
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                if (dismissText != null) PixelTextButton(dismissText, onDismissRequest)
                PixelTextButton(confirmText, onConfirm, enabled = confirmEnabled)
            }
        }
    }
}

/** Body text of a [PixelDialog]. */
@Composable
fun PixelDialogText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = pixelTextStyle(10.sp, shadow = 3f))
}

/** Smaller plank with a text label, for dialogs and headers. */
@Composable
fun PixelTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    PixelPlank(onClick, modifier.height(44.dp), enabled = enabled, pixel = 2.dp, nails = false) { colors ->
        Text(text, style = pixelTextStyle(10.sp, colors.text, shadow = 3f), maxLines = 1, softWrap = false)
    }
}

/** Text input carved into the wood like an inventory slot. */
@Composable
fun PixelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val text = pixelTextStyle(11.sp, shadow = 3f)
    Column(modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else maxLines,
            textStyle = text,
            cursorBrush = SolidColor(PushyColors.Sand),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { field ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .pixelSlot()
                        .then(if (isError) Modifier.pixelFrame(PushyColors.Coral) else Modifier)
                        .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(placeholder, style = text.copy(color = Color(0xFFD9B98A)))
                    }
                    field()
                }
            },
        )
        if (supportingText != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                supportingText,
                style = pixelTextStyle(8.sp, if (isError) ErrorText else Color(0xFFF9E7BD), shadow = 2f),
                textAlign = if (isError) TextAlign.Start else TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val ErrorText = Color(0xFFFFB199)
