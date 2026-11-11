package th.co.lotteryreport.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DrawResultEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LotteryDatabase : RoomDatabase() {
    abstract fun drawResultDao(): DrawResultDao
}
