package th.co.lotteryreport.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawResultDao {
    @Query("SELECT * FROM draw_results ORDER BY drawDate DESC, category ASC, number ASC")
    fun observeAll(): Flow<List<DrawResultEntity>>

    @Query("SELECT * FROM draw_results WHERE drawDate = :drawDate ORDER BY category ASC, number ASC")
    fun observeByDrawDate(drawDate: String): Flow<List<DrawResultEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<DrawResultEntity>): List<Long>
}

@Dao
interface DrawScheduleDao {
    @Query("SELECT * FROM draw_schedules ORDER BY drawDate DESC")
    fun observeAll(): Flow<List<DrawScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DrawScheduleEntity)
}
