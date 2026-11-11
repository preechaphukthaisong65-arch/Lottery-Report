package th.co.lotteryreport.data

import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test
import th.co.lotteryreport.data.local.DrawScheduleEntity

class BackupCodecTest {
    @Test fun roundTripsEmptyBackup() {
        assertNotNull(BackupCodec.decode(BackupCodec.encode(emptyList(), emptyList())))
    }

    @Test fun rejectsUnsupportedSchema() {
        val encoded = BackupCodec.encode(emptyList(), emptyList())
            .replace("\"schemaVersion\":1", "\"schemaVersion\":99")
        assertNull(BackupCodec.decode(encoded))
    }

    @Test fun rejectsNonGloScheduleSource() {
        val item = DrawScheduleEntity("2026-11-16", "CONFIRMED", "https://example.com", 1L, 1L)
        assertNull(BackupCodec.decode(BackupCodec.encode(listOf(item), emptyList())))
    }

    @Test fun rejectsEstimatedScheduleWithSourceUrl() {
        val item = DrawScheduleEntity("2026-11-16", "ESTIMATED", "https://www.glo.or.th", null, 1L)
        assertNull(BackupCodec.decode(BackupCodec.encode(listOf(item), emptyList())))
    }

    @Test fun rejectsOversizedInput() {
        assertNull(BackupCodec.decode("x".repeat(5_000_001)))
    }

    @Test fun rejectsMalformedJsonWithoutCrashing() {
        assertNull(BackupCodec.decode("{\"schemaVersion\":1,\"schedules\":null}"))
    }
}
