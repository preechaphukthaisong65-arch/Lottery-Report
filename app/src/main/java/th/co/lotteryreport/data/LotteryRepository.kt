package th.co.lotteryreport.data

import androidx.room.withTransaction
import th.co.lotteryreport.data.local.DrawResultEntity
import th.co.lotteryreport.data.local.LotteryDatabase
import th.co.lotteryreport.domain.DrawResultValidator
import th.co.lotteryreport.domain.PrizeValue
import java.time.Instant
import kotlinx.coroutines.flow.Flow

class LotteryRepository(
    private val database: LotteryDatabase
) {
    fun observeAllResults(): Flow<List<DrawResultEntity>> =
        database.drawResultDao().observeAll()

    fun observeResultsFor(drawDate: String): Flow<List<DrawResultEntity>> =
        database.drawResultDao().observeByDrawDate(drawDate)

    /**
     * Stores only structurally complete, already source-verified result groups.
     * Existing records are never replaced by a later response.
     */
    suspend fun saveVerifiedResults(
        drawDate: String,
        values: List<PrizeValue>,
        sourceUrl: String,
        verifiedAt: Instant = Instant.now()
    ): Boolean {
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
