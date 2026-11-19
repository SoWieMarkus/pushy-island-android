package markus.wieland.pushygame.data

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.level.LevelFormat

class LevelRepository(private val dao: LevelDao, private val assets: AssetManager) {

    val campaign: Flow<List<LevelEntity>> = dao.observeCampaign()
    val customLevels: Flow<List<LevelEntity>> = dao.observeCustom()

    suspend fun get(id: Long): LevelEntity? = dao.get(id)

    /**
     * Makes the database match the campaign files in `assets/levels` (`NNN-name.json`). Level fixes in
     * app updates are picked up; the player's progress is kept.
     */
    suspend fun syncCampaign() = withContext(Dispatchers.IO) {
        val files = assets.list(CAMPAIGN_DIR).orEmpty().filter { it.endsWith(".json") }.sorted()
        for (file in files) {
            val number = file.substringBefore('-').toInt()
            val json = assets.open("$CAMPAIGN_DIR/$file").bufferedReader().use { it.readText() }
            val name = LevelFormat.fromJson(json).name
            val existing = dao.campaignLevel(number)
            when {
                existing == null -> dao.insert(LevelEntity(name = name, json = json, campaignNumber = number))
                existing.json != json || existing.name != name -> dao.update(existing.copy(name = name, json = json))
            }
        }
        dao.deleteCampaignAfter(files.size)
    }

    /** The campaign level after [id], or null if [id] is the last one (or not a campaign level). */
    suspend fun nextCampaignLevel(id: Long): LevelEntity? {
        val number = dao.get(id)?.campaignNumber ?: return null
        return dao.campaignLevel(number + 1)
    }

    suspend fun markSolved(id: Long) = dao.markSolved(id)

    suspend fun markCampaignSolved() = dao.markCampaignSolved()

    /** Saves a custom level; editing a level resets its solved state because it has to be re-verified. */
    suspend fun saveCustom(id: Long?, level: LevelData): Long {
        val json = LevelFormat.toJson(level)
        val existing = id?.let { dao.get(it) }
        return if (existing == null) {
            dao.insert(LevelEntity(name = level.name, json = json))
        } else {
            if (existing.json != json) dao.update(existing.copy(name = level.name, json = json, solved = false))
            existing.id
        }
    }

    suspend fun delete(id: Long) = dao.delete(id)

    companion object {
        const val CAMPAIGN_DIR = "levels"
    }
}

fun LevelEntity.toLevelData(): LevelData = LevelFormat.fromJson(json)
