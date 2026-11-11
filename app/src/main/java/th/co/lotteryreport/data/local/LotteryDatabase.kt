package th.co.lotteryreport.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DrawResultEntity::class, DrawScheduleEntity::class, VerificationEventEntity::class],
    version = 3,
    exportSchema = true
)
abstract class LotteryDatabase : RoomDatabase() {
    abstract fun drawResultDao(): DrawResultDao
    abstract fun drawScheduleDao(): DrawScheduleDao
    abstract fun verificationEventDao(): VerificationEventDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS verification_events (
                        eventKey TEXT NOT NULL PRIMARY KEY,
                        mode TEXT NOT NULL,
                        checkedAt TEXT NOT NULL,
                        status TEXT NOT NULL,
                        message TEXT NOT NULL,
                        sources TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
