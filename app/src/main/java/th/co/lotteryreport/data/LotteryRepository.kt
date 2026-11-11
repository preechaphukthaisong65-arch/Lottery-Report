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

    suspend fun getSchedulesSnapshot(): List<DrawScheduleEntity> =
        database.drawScheduleDao().getAll()

    suspend fun seedEstimatedSchedule(today: LocalDate = LocalDate.now(ZoneId.of("Asia/Bangkok"))): Int {
        val estimates = DrawScheduleGenerator.generate(today, 12)
        return database.drawScheduleDao().insertEstimates(estimates).count { it != -1L }
    }

    suspend fun exportBackupJson(): String = database.withTransaction {
        BackupCodec.encode(
            database.drawScheduleDao().getAll(),
            database.drawResultDao().getAll()
        )
    }

    suspend fun restoreBackupJson(json: String): Boolean {
        val backup = BackupCodec.decode(json) ?: return false
        return runCatching {
            database.withTransaction {
                database.drawResultDao().deleteAll()
                database.drawScheduleDao().deleteAll()
                if (backup.results.isNotEmpty()) {
                    database.drawResultDao().insertAll(backup.results.map { it.copy(id = 0) })
                }
                if (backup.schedules.isNotEmpty()) {
                    database.drawScheduleDao().upsertAll(backup.schedules)
                }
            }
            true
        }.getOrDefault(false)
    }

    suspend fun saveSchedule(
        item: DrawScheduleEntity,
        supersedesEstimatedDate: String? = null
    ): Boolean {
        if (runCatching { LocalDate.parse(item.drawDate) }.isFailure) return false
        if (item.checkedAtEpochMillis <= 0) return false
        if (item.status !in setOf("ESTIMATED", "CONFIRMED")) return false
        if (supersedesEstimatedDate != null) {
            if (supersedesEstimatedDate == item.drawDate ||
                runCatching { LocalDate.parse(supersedesEstimatedDate) }.isFailure
            ) return false
            if (item.status != "CONFIRMED") return false
        }

        if (item.status == "CONFIRMED") {
            if (!isGloHttpsUrl(item.sourceUrl)) return false
            if (item.sourcePublishedAtEpochMillis != null && item.sourcePublishedAtEpochMillis <= 0) return false
        } else if (!item.sourceUrl.isNullOrBlank() || item.sourcePublishedAtEpochMillis != null) {
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
                if (oldPublished != null) {
                    if (newPublished == null || newPublished <= oldPublished) return@withTransaction false
                } else if (item.checkedAtEpochMillis <= existing.checkedAtEpochMillis) {
                    return@withTransaction false
                }
            }
            if (supersedesEstimatedDate != null) {
                val previous = dao.findByDate(supersedesEstimatedDate) ?: return@withTransaction false
                if (previous.status != "ESTIMATED" || previous.isOfficiallyConfirmed) {
                    return@withTransaction false
                }
                dao.deleteEstimatedByDate(supersedesEstimatedDate)
            }
            dao.upsert(item)
            true
        }
    }

    suspend fun saveVerifiedResults(
        drawDate: String,
        values: List<PrizeValue>,
        sourceUrl: String,
        verifiedAt: Instant = Instant.now()
    ): Boolean {
        if (!isGloHttpsUrl(sourceUrl)) return false
        if (!DrawResultValidator.validate(drawDate, values)) return false
        if (verifiedAt.toEpochMilli() <= 0) return false

        val entities = values.map {
            DrawResultEntity(
                drawDate = drawDate,
                category = it.category,
                number = it.number,
                sourceUrl = sourceUrl,
                verifiedAtEpochMillis = verifiedAt.toEpochMilli()
            )
        }
        return database.withTransaction {
            val dao = database.drawResultDao()
            if (dao.getByDrawDate(drawDate).isNotEmpty()) return@withTransaction false
            dao.insertAll(entities).size == entities.size
        }
    }

    private fun isGloHttpsUrl(value: String?): Boolean {
        if (value.isNullOrBlank() || !value.startsWith("https://")) return false
        val host = runCatching { URI(value).host }.getOrNull() ?: return false
        return host == "glo.or.th" || host.endsWith(".glo.or.th")
    }
}
