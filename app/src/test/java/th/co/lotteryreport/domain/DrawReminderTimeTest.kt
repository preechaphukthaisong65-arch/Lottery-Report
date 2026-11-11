package th.co.lotteryreport.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class DrawReminderTimeTest {
    @Test
    fun schedulesReminderAtSixPmBangkokDayBeforeDraw() {
        val now = Instant.parse("2026-11-11T10:00:00Z")
        val expected = 4L * 24 * 60 * 60 * 1000 + 60 * 60 * 1000
        assertEquals(expected, DrawReminderTime.delayMillis("2026-11-16", now))
    }

    @Test
    fun schedulesImmediatelyWhenReminderTimeHasPassedButDrawIsTomorrow() {
        val now = Instant.parse("2026-11-14T12:00:00Z")
        assertEquals(60_000L, DrawReminderTime.delayMillis("2026-11-15", now))
    }

    @Test
    fun skipsPastAndCurrentDrawDates() {
        val now = Instant.parse("2026-11-15T12:00:00Z")
        assertNull(DrawReminderTime.delayMillis("2026-11-15", now))
        assertNull(DrawReminderTime.delayMillis("2026-11-01", now))
    }

    @Test
    fun rejectsInvalidDate() {
        assertNull(DrawReminderTime.delayMillis("invalid", Instant.now()))
    }
}
