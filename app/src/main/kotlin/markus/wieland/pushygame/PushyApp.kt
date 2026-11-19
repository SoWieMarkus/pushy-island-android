package markus.wieland.pushygame

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import markus.wieland.pushygame.data.LevelRepository
import markus.wieland.pushygame.data.PushyDatabase
import markus.wieland.pushygame.data.Settings
import markus.wieland.pushygame.render.SpriteSheet

/** Tiny manual dependency container – the app is small enough not to need a DI framework. */
class AppContainer(context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob())
    private val database = PushyDatabase.create(context)
    val levels = LevelRepository(database.levels(), context.assets)
    val settings = Settings(context)
    val sprites by lazy { SpriteSheet.load(context.assets) }
}

class PushyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.applicationScope.launch { container.levels.syncCampaign() }
    }
}

val Context.appContainer: AppContainer get() = (applicationContext as PushyApp).container
