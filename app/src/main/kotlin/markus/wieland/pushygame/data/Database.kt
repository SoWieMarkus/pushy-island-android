package markus.wieland.pushygame.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * A playable level. Campaign levels have a [campaignNumber] and are synced from the app's assets;
 * custom levels are created in the editor or imported from share codes. [json] is level format v2.
 */
@Entity(tableName = "levels", indices = [Index(value = ["campaignNumber"], unique = true)])
data class LevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val json: String,
    val campaignNumber: Int? = null,
    val solved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels WHERE campaignNumber IS NOT NULL ORDER BY campaignNumber")
    fun observeCampaign(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE campaignNumber IS NULL ORDER BY createdAt DESC")
    fun observeCustom(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE campaignNumber IS NOT NULL ORDER BY campaignNumber")
    suspend fun campaign(): List<LevelEntity>

    @Query("SELECT * FROM levels WHERE id = :id")
    suspend fun get(id: Long): LevelEntity?

    @Query("SELECT * FROM levels WHERE campaignNumber = :number")
    suspend fun campaignLevel(number: Int): LevelEntity?

    @Insert
    suspend fun insert(level: LevelEntity): Long

    @Update
    suspend fun update(level: LevelEntity)

    @Query("DELETE FROM levels WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE levels SET solved = 1 WHERE id = :id")
    suspend fun markSolved(id: Long)

    @Query("UPDATE levels SET solved = 1 WHERE campaignNumber IS NOT NULL")
    suspend fun markCampaignSolved()

    @Query("DELETE FROM levels WHERE campaignNumber > :lastNumber")
    suspend fun deleteCampaignAfter(lastNumber: Int)
}

@Database(entities = [LevelEntity::class], version = 1, exportSchema = false)
abstract class PushyDatabase : RoomDatabase() {
    abstract fun levels(): LevelDao

    companion object {
        fun create(context: Context): PushyDatabase =
            Room.databaseBuilder(context, PushyDatabase::class.java, "pushy-island.db").build()
    }
}
