package th.co.lotteryreport.data

import androidx.room.withTransaction
import th.co.lotteryreport.data.local.DrawResultEntity
import th.co.lotteryreport.data.local.DrawScheduleEntity
import th.co.lotteryreport.data.local.LotteryDatabase
import th.co.lotteryreport.domain.DrawResultValidator
import th.co.lotteryreport.domain.DrawScheduleGenerator
import th.co.lotteryreport.domain.PrizeValue
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

class LotteryRepository(
    private val database: LotteryDatabase
) {
    fun observeAllResults(): Flow<List<DrawResultEntity>> =
        database.drawResultDao().observeAll()

    fun observeResultsFor(drawDate: String): Flow<List<DrawResultEntity>> =
        database.drawResultDao().observeByDrawDate(drawDate)

    fun observeSchedules(): Flow<List<DrawScheduleEntity>> =
        database.drawScheduleDao().observeAll()

    /**
     * Seeds ordinary 1st/16th dates as estimates for the next year.
     * INSERT IGNORE guarantees this bootstrap cannot overwrite official dates.
     */
    suspend fun seedEstimatedSchedule(today: LocalDate = LocalDate.now(ZoneId.of("Asia/Bangkok"))): Int {
        val estimates = DrawScheduleGenerator.generate(today, 12)
        return database.drawScheduleDao().insertEstimates(estimates).count { it != -1L }
    }

    suspend fun saveSchedule(item: DrawScheduleEntity): Boolean {
        if (runCatching { LocalDate.parse(item.drawDate) }.isFailure) return false
        if (item.status !in setOf("ESTIMATED", "CONFIRMED")) return false

        if (item.status == "CONFIRMED") {
            if (!item.sourceUrl.orEmpty().startsWith("https://")) return false
            val host = runCatching { URI(item.sourceUrl ?: return false).host }.getOrNull()
            if (host != "glo.or.th" && host?.endsWith(".glo.or.th") != true) return false
        } else if (!item.sourceUrl.isNullOrBlank()) {
            return false
        }

        return database.withTransaction {
            val dao = database.drawScheduleDao()
            val existing = dao.findByDate(item.drawDate)
            if (existing?.isOfficiallyConfirmed == true && item.status == "ESTIMATED") {
                return@withTransaction false
            }
            if (existing?.isOfficiallyConfirmed == true && item.status == "CONFIRMED") {
                val oldPublished = existing.sourcePublishedAtEpochMillis
                val newPublished = item.sourcePublishedAtEpochMillis
                if (newPublished == null || (oldPublished != null && newPublished <= oldPublished)) {
                    return@withTransaction false
                }
            }
            dao.upsert(item)
            true
        }
    }

    /**
     * Stores only structurally complete, already source-verified result groups.
     * Existing result rows are never replaced by a later response.
     */
    suspend fun saveVerifiedResults(
        drawDate: String,
        values: List<PrizeValue>,
        sourceUrl: String,
        verifiedAt: Instant = Instant.now()
    ): Boolean {
        val host = runCatching { URI(sourceUrl).host }.getOrNull()
        if (host != "glo.or.th" && host?.endsWith(".glo.or.th") != true) return false
        if (!sourceUrl.startsWith("https://")) return false
        if (!DrawResultValidator.validate(drawDate, values)) return false

        val entities = values.map {
            DrawResultEntity(
                drawDate = drawDate,
                category = it.category,
                number = it.number,
                sourceUrl = sourceUrl,
                verifiedAtEpochMillis = verifiedAt.toEpochMilli()
            )
        }
        database.withTransaction {
            database.drawResultDao().insertAll(entities)
        }
        return true
    }
}
