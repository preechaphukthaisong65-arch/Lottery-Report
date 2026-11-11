package th.co.lotteryreport.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import th.co.lotteryreport.MainActivity
import th.co.lotteryreport.data.local.DatabaseProvider
import java.time.LocalDate
import java.time.ZoneId

class DrawReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val drawDate = inputData.getString(KEY_DRAW_DATE) ?: return Result.failure()
        val drawDay = runCatching { LocalDate.parse(drawDate) }.getOrNull() ?: return Result.failure()
        if (drawDay.isBefore(LocalDate.now(ZoneId.of("Asia/Bangkok")))) return Result.success()

        val schedule = runCatching {
            DatabaseProvider.get(applicationContext).drawScheduleDao().findByDate(drawDate)
        }.getOrNull() ?: return Result.success()

        if (Build.VERSION.SDK_INT >= 33 &&
            applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()

        createNotificationChannel()
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            drawDate.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val status = if (schedule.isOfficiallyConfirmed) "ยืนยันจาก GLO" else "วันประมาณการ"
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("พรุ่งนี้มีวันออกรางวัล")
            .setContentText("งวดวันที่ $drawDate ($status)")
            .setStyle(NotificationCompat.BigTextStyle().bigText("งวดวันที่ $drawDate — $status โปรดตรวจสอบประกาศล่าสุดจาก GLO"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(drawDate.hashCode(), notification)
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "แจ้งเตือนวันออกรางวัล", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    companion object {
        const val KEY_DRAW_DATE = "draw_date"
        private const val CHANNEL_ID = "draw_reminders"
    }
}
