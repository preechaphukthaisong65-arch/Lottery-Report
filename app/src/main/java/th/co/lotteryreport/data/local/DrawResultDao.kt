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

    @Query("SELECT * FROM draw_results WHERE drawDate = :drawDate ORDER BY category ASC, number ASC")
    suspend fun getByDrawDate(drawDate: String): List<DrawResultEntity>

    @Query("SELECT * FROM draw_results ORDER BY drawDate DESC, category ASC, number ASC")
    suspend fun getAll(): List<DrawResultEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<DrawResultEntity>): List<Long>

    @Query("DELETE FROM draw_results")
    suspend fun deleteAll()
}

@Dao
interface DrawScheduleDao {
    @Query("SELECT * FROM draw_schedules ORDER BY drawDate DESC")
    fun observeAll(): Flow<List<DrawScheduleEntity>>

    @Query("SELECT * FROM draw_schedules ORDER BY drawDate DESC")
    suspend fun getAll(): List<DrawScheduleEntity>

    @Query("SELECT * FROM draw_schedules WHERE drawDate = :drawDate LIMIT 1")
    suspend fun findByDate(drawDate: String): DrawScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DrawScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DrawScheduleEntity>)

    @Query("DELETE FROM draw_schedules")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEstimates(items: List<DrawScheduleEntity>): List<Long>
}
