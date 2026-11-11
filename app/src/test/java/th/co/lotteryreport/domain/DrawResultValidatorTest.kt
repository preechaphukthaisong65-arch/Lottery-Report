package th.co.lotteryreport.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawResultValidatorTest {
    private val drawDate = "2026-11-16"
    private val valid = listOf(
        PrizeValue("FIRST_PRIZE", "123456"),
        PrizeValue("FRONT_THREE", "123"),
        PrizeValue("FRONT_THREE", "456"),
        PrizeValue("LAST_THREE", "789"),
        PrizeValue("LAST_THREE", "012"),
        PrizeValue("LAST_TWO", "34")
    )

    @Test fun acceptsCompleteCoreResultGroups() {
        assertTrue(DrawResultValidator.validate(drawDate, valid))
    }

    @Test fun acceptsOptionalPrizeGroupsWhenComplete() {
        val expanded = valid + (1..5).map { PrizeValue("SECOND_PRIZE", "%06d".format(it)) }
        assertTrue(DrawResultValidator.validate(drawDate, expanded))
    }

    @Test fun rejectsMissingMandatoryGroup() {
        assertFalse(DrawResultValidator.validate(drawDate, valid.filterNot { it.category == "LAST_TWO" }))
    }

    @Test fun rejectsMalformedNumber() {
        assertFalse(DrawResultValidator.validate(drawDate, valid + PrizeValue("LAST_TWO", "A4")))
    }

    @Test fun rejectsWrongGroupCount() {
        assertFalse(DrawResultValidator.validate(drawDate, valid + PrizeValue("FRONT_THREE", "999")))
    }

    @Test fun rejectsDuplicateNumberWithinGroup() {
        assertFalse(DrawResultValidator.validate(drawDate, valid + PrizeValue("LAST_TWO", "34")))
    }

    @Test fun rejectsUnknownCategory() {
        assertFalse(DrawResultValidator.validate(drawDate, valid + PrizeValue("UNKNOWN", "123")))
    }

    @Test fun rejectsInvalidDrawDate() {
        assertFalse(DrawResultValidator.validate("not-a-date", valid))
    }

    @Test fun rejectsEmptyResponse() {
        assertFalse(DrawResultValidator.validate(drawDate, emptyList()))
    }
}
