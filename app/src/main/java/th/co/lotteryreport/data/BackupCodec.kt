package th.co.lotteryreport.data

import com.google.gson.Gson
import th.co.lotteryreport.data.local.DrawResultEntity
import th.co.lotteryreport.data.local.DrawScheduleEntity
import th.co.lotteryreport.domain.DrawResultValidator
import th.co.lotteryreport.domain.PrizeValue
import java.net.URI
import java.time.LocalDate

data class LotteryBackup(
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val schedules: List<DrawScheduleEntity>,
    val results: List<DrawResultEntity>
)

object BackupCodec {
    private const val CURRENT_SCHEMA = 1
    private const val MAX_SCHEDULES = 2_000
    private const val MAX_RESULTS = 20_000
    private val gson = Gson()

    fun encode(schedules: List<DrawScheduleEntity>, results: List<DrawResultEntity>): String =
        gson.toJson(LotteryBackup(CURRENT_SCHEMA, System.currentTimeMillis(), schedules, results))

    fun decode(json: String): LotteryBackup? {
        if (json.isBlank() || json.length > 5_000_000) return null
        return try {
            val backup = gson.fromJson(json, LotteryBackup::class.java) ?: return null
            if (backup.schemaVersion != CURRENT_SCHEMA || backup.exportedAtEpochMillis <= 0) return null
            if (backup.schedules.size > MAX_SCHEDULES || backup.results.size > MAX_RESULTS) return null
            if (backup.schedules.map { it.drawDate }.distinct().size != backup.schedules.size) return null
            if (backup.results.map { "${it.drawDate}|${it.category}|${it.number}" }.distinct().size != backup.results.size) return null

            val schedulesValid = backup.schedules.all { item ->
                runCatching { LocalDate.parse(item.drawDate) }.isSuccess &&
                    item.checkedAtEpochMillis > 0 &&
                    when (item.status) {
                        "ESTIMATED" -> item.sourceUrl.isNullOrBlank() && item.sourcePublishedAtEpochMillis == null
                        "CONFIRMED" -> isGloHttpsUrl(item.sourceUrl) &&
                            (item.sourcePublishedAtEpochMillis == null || item.sourcePublishedAtEpochMillis > 0)
                        else -> false
                    }
            }
            if (!schedulesValid) return null

            val resultsValid = backup.results.all { item ->
                runCatching { LocalDate.parse(item.drawDate) }.isSuccess &&
                    item.verifiedAtEpochMillis > 0 &&
                    isGloHttpsUrl(item.sourceUrl)
            }
            if (!resultsValid) return null
            val completeGroups = backup.results.groupBy { it.drawDate }.values.all { rows ->
                DrawResultValidator.validate(rows.first().drawDate, rows.map { PrizeValue(it.category, it.number) })
            }
            if (!completeGroups) return null
            backup
        } catch (_: Exception) {
            null
        }
    }

    private fun isGloHttpsUrl(value: String?): Boolean {
        if (value.isNullOrBlank() || !value.startsWith("https://")) return false
        val host = runCatching { URI(value).host }.getOrNull() ?: return false
        return host == "glo.or.th" || host.endsWith(".glo.or.th")
    }
}
