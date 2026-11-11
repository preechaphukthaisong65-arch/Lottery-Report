package th.co.lotteryreport.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DrawResultEntity::class, DrawScheduleEntity::class],
    version = 2,
    exportSchema = true
)
abstract class LotteryDatabase : RoomDatabase() {
    abstract fun drawResultDao(): DrawResultDao
    abstract fun drawScheduleDao(): DrawScheduleDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS draw_schedules (
                        drawDate TEXT NOT NULL PRIMARY KEY,
                        status TEXT NOT NULL,
                        sourceUrl TEXT,
                        sourcePublishedAtEpochMillis INTEGER,
                        checkedAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
