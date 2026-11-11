package th.co.lotteryreport.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object DrawReminderTime {
    private val bangkok = ZoneId.of("Asia/Bangkok")

    fun delayMillis(drawDate: String, now: Instant): Long? {
        val date = runCatching { LocalDate.parse(drawDate) }.getOrNull() ?: return null
        if (!date.isAfter(now.atZone(bangkok).toLocalDate())) return null
        val reminderAt = date.minusDays(1).atTime(LocalTime.of(18, 0)).atZone(bangkok).toInstant()
        return Duration.between(now, reminderAt).toMillis().coerceAtLeast(60_000L)
    }
}
