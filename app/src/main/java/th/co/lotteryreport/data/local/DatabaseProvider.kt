package th.co.lotteryreport.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: LotteryDatabase? = null

    fun get(context: Context): LotteryDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                LotteryDatabase::class.java,
                "lottery-report.db"
            )
                .addMigrations(LotteryDatabase.MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
}
