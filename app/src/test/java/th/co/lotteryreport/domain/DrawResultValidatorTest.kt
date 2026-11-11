package th.co.lotteryreport.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawResultValidatorTest {
    private val valid = listOf(
        PrizeValue("FIRST_PRIZE", "123456"),
        PrizeValue("FRONT_THREE", "123"),
        PrizeValue("FRONT_THREE", "456"),
        PrizeValue("LAST_THREE", "789"),
        PrizeValue("LAST_THREE", "012"),
        PrizeValue("LAST_TWO", "34")
    )

    @Test fun acceptsCompleteCoreResultSet() {
        assertTrue(DrawResultValidator.validate(valid))
    }

    @Test fun rejectsMissingCategory() {
        assertFalse(DrawResultValidator.validate(valid.filterNot { it.category == "LAST_TWO" }))
    }

    @Test fun rejectsMalformedNumber() {
        assertFalse(DrawResultValidator.validate(valid + PrizeValue("LAST_TWO", "A4")))
    }

    @Test fun rejectsUnknownCategory() {
        assertFalse(DrawResultValidator.validate(valid + PrizeValue("UNKNOWN", "123")))
    }

    @Test fun rejectsEmptyResponse() {
        assertFalse(DrawResultValidator.validate(emptyList()))
    }
}
