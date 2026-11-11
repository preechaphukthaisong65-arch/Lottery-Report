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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import th.co.lotteryreport.MainActivity
import th.co.lotteryreport.data.local.DatabaseProvider
import th.co.lotteryreport.data.local.VerificationEventEntity
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class VerificationSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = DatabaseProvider.get(applicationContext)
            val modes = listOf("calendar", "results")
            for (mode in modes) {
                val jsonText = downloadReport(mode) ?: return@withContext Result.retry()
                val report = JSONObject(jsonText)
                if (report.optInt("schema_version") != 1) return@withContext Result.retry()
                val checkedAt = report.optString("checked_at", "")
                if (checkedAt.isBlank()) return@withContext Result.retry()

                val failedSources = mutableListOf<String>()
                val checks = report.optJSONArray("checks")
                if (checks != null) {
                    for (index in 0 until checks.length()) {
                        val check = checks.optJSONObject(index) ?: continue
                        val status = check.optString("status")
                        if (status == "SOURCE_UNAVAILABLE" || status == "INCOMPLETE") {
                            failedSources += check.optString("name", "unknown")
                        }
                    }
                }
                val overall = report.optString("overall_status", "INCOMPLETE")
                val isMismatch = overall == "MISMATCH"
                if (!isMismatch && failedSources.isEmpty()) continue

                val status = if (isMismatch) "MISMATCH" else "SOURCE_UNAVAILABLE"
                val sourceText = failedSources.distinct().sorted().joinToString(", ")
                val message = if (isMismatch) {
                    "พบข้อมูลขัดแย้งในรายงาน $mode กรุณาตรวจสอบหลักฐานก่อนยืนยัน"
                } else {
                    "แหล่งข้อมูลบางแห่งตรวจสอบไม่ได้: $sourceText"
                }
                val keyMaterial = "$mode|$status|$sourceText"
                val eventKey = sha256(keyMaterial)
                val inserted = database.verificationEventDao().insertIfNew(
                    VerificationEventEntity(
                        eventKey = eventKey,
                        mode = mode,
                        checkedAt = checkedAt,
                        status = status,
                        message = message,
                        sources = sourceText
                    )
                )
                if (inserted != -1L) showNotification(eventKey, mode, message)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun downloadReport(mode: String): String? {
        val url = URL("$BASE_URL/$mode-latest.json")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Lottery-Report-Android/0.2")
        }
        return try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) return null
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_REPORT_BYTES) throw IllegalStateException("Report too large")
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            bytes.toString(Charsets.UTF_8)
        } finally {
            connection.disconnect()
        }
    }

    private fun showNotification(eventKey: String, mode: String, message: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "แจ้งเตือนการตรวจสอบหวย", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            eventKey.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(if (mode == "results") "ตรวจสอบผลรางวัลพบปัญหา" else "ตรวจสอบปฏิทินพบปัญหา")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(eventKey.hashCode(), notification)
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        private const val BASE_URL =
            "https://raw.githubusercontent.com/preechaphukthaisong65-arch/Lottery-Report/main/data/verification"
        private const val MAX_REPORT_BYTES = 1_000_000
        private const val CHANNEL_ID = "verification_alerts"
    }
}
