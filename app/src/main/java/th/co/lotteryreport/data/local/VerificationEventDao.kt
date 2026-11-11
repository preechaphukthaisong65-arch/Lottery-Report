package th.co.lotteryreport.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationEventDao {
    @Query("SELECT * FROM verification_events ORDER BY checkedAt DESC LIMIT 50")
    fun observeRecent(): Flow<List<VerificationEventEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNew(event: VerificationEventEntity): Long
}
