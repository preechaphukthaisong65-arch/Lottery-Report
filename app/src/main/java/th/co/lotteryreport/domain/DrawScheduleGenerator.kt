package th.co.lotteryreport.domain

import th.co.lotteryreport.data.local.DrawScheduleEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Produces ordinary draw dates as estimates only.
 * It deliberately does not infer official holiday changes or special draws.
 */
object DrawScheduleGenerator {
    private val bangkokZone = ZoneId.of("Asia/Bangkok")

    fun generate(
        today: LocalDate = LocalDate.now(bangkokZone),
        monthsAhead: Long = 12
    ): List<DrawScheduleEntity> {
        require(monthsAhead in 1..24) { "monthsAhead must be between 1 and 24" }

        val lastDate = today.plusMonths(monthsAhead)
        val firstMonth = YearMonth.from(today)
        val lastMonth = YearMonth.from(lastDate)

        return generateSequence(firstMonth) { month ->
            if (month < lastMonth) month.plusMonths(1) else null
        }.flatMap { month ->
            sequenceOf(1, 16).mapNotNull { day ->
                val date = month.atDay(day)
                if (date.isBefore(today) || date.isAfter(lastDate)) null else date
            }
        }.map { date ->
            DrawScheduleEntity(
                drawDate = date.toString(),
                status = "ESTIMATED",
                sourceUrl = null,
                sourcePublishedAtEpochMillis = null,
                checkedAtEpochMillis = System.currentTimeMillis()
            )
        }.toList()
    }
}
