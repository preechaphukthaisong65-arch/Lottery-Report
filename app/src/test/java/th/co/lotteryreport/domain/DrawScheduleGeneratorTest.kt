package th.co.lotteryreport.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DrawScheduleGeneratorTest {
    @Test
    fun generatesOnlyFutureOrCurrentOrdinaryDatesWithinHorizon() {
        val today = LocalDate.parse("2026-11-11")
        val dates = DrawScheduleGenerator.generate(today, 12).map { it.drawDate }

        assertEquals("2026-11-16", dates.first())
        assertTrue(dates.contains("2026-12-01"))
        assertTrue(dates.contains("2026-12-16"))
        assertTrue(dates.contains("2027-11-01"))
        assertTrue(dates.none { it == "2026-11-01" })
        assertTrue(dates.all { it.endsWith("-01") || it.endsWith("-16") })
        assertTrue(dates.all { date ->
            val parsed = LocalDate.parse(date)
            !parsed.isBefore(today) && !parsed.isAfter(today.plusMonths(12))
        })
    }

    @Test
    fun allGeneratedDatesAreExplicitlyEstimatedAndHaveNoOfficialSource() {
        val items = DrawScheduleGenerator.generate(LocalDate.parse("2026-11-11"), 2)

        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.status == "ESTIMATED" })
        assertTrue(items.all { it.sourceUrl == null })
        assertTrue(items.all { it.sourcePublishedAtEpochMillis == null })
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnboundedHorizon() {
        DrawScheduleGenerator.generate(LocalDate.parse("2026-11-11"), 100)
    }
}
