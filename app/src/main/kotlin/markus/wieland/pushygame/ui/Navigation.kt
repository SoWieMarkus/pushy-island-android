package markus.wieland.pushygame.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import markus.wieland.pushygame.ui.campaign.CampaignScreen
import markus.wieland.pushygame.ui.custom.CustomLevelsScreen
import markus.wieland.pushygame.ui.editor.EditorScreen
import markus.wieland.pushygame.ui.game.GameScreen
import markus.wieland.pushygame.ui.menu.MenuScreen

@Serializable data object MenuRoute : NavKey
@Serializable data object CampaignRoute : NavKey
@Serializable data object CustomLevelsRoute : NavKey

/** [isTest] = playing a custom level: no "next level" afterwards. */
@Serializable data class GameRoute(val levelId: Long, val isTest: Boolean = false) : NavKey

/** [levelId] = 0 creates a new level. */
@Serializable data class EditorRoute(val levelId: Long = 0) : NavKey

@Composable
fun PushyNavigation() {
    val backStack = rememberNavBackStack(MenuRoute)
    val back: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    NavDisplay(
        backStack = backStack,
        onBack = back,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        popTransitionSpec = { fadeIn() togetherWith fadeOut() },
        predictivePopTransitionSpec = { fadeIn() togetherWith fadeOut() },
        entryProvider = entryProvider {
            entry<MenuRoute> {
                MenuScreen(
                    onCampaign = { backStack.add(CampaignRoute) },
                    onCustomLevels = { backStack.add(CustomLevelsRoute) },
                )
            }
            entry<CampaignRoute> {
                CampaignScreen(onBack = back, onPlay = { backStack.add(GameRoute(it)) })
            }
            entry<CustomLevelsRoute> {
                CustomLevelsScreen(
                    onBack = back,
                    onPlay = { backStack.add(GameRoute(it, isTest = true)) },
                    onEdit = { backStack.add(EditorRoute(it)) },
                )
            }
            entry<GameRoute> { route ->
                GameScreen(
                    levelId = route.levelId,
                    isTest = route.isTest,
                    onBack = back,
                    onNextLevel = { next -> backStack[backStack.lastIndex] = GameRoute(next) },
                )
            }
            entry<EditorRoute> { route ->
                EditorScreen(levelId = route.levelId, onDone = back)
            }
        },
    )
}
