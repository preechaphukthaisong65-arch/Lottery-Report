package th.co.lotteryreport.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import th.co.lotteryreport.data.local.DrawScheduleEntity
import th.co.lotteryreport.domain.DrawReminderTime
import java.time.Instant
import java.util.concurrent.TimeUnit

object DrawReminderScheduler {
    private const val TAG = "lottery_draw_reminder"
    private val lock = Any()

    fun schedule(context: Context, schedules: List<DrawScheduleEntity>, now: Instant = Instant.now()) {
        synchronized(lock) {
            val workManager = WorkManager.getInstance(context.applicationContext)
            workManager.cancelAllWorkByTag(TAG).result.get()
            schedules.forEach { item ->
                val delay = DrawReminderTime.delayMillis(item.drawDate, now) ?: return@forEach
                val request = OneTimeWorkRequestBuilder<DrawReminderWorker>()
                    .setInputData(Data.Builder().putString(DrawReminderWorker.KEY_DRAW_DATE, item.drawDate).build())
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .addTag(TAG)
                    .build()
                workManager.enqueueUniqueWork(
                    "draw-reminder-${item.drawDate}",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            }
        }
    }
}
