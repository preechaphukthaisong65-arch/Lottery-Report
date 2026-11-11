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

    @Query("DELETE FROM draw_results WHERE drawDate = :drawDate")
    suspend fun deleteByDrawDate(drawDate: String)
}
