package z.music.db

import androidx.room.*
import z.music.model.Track

@Database(entities = [Track::class], version = 2)
abstract class Db : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    @Dao
    interface TrackDao {

        @Insert
        fun insert(track: Track)

        @Query("SELECT * FROM Track")
        fun all(): List<Track>

        @Delete
        fun delete(track: Track)

        @Query("SELECT EXISTS(SELECT 1 FROM Track WHERE id=:i)")
        fun isAdded(i: Int): Boolean

    }
}