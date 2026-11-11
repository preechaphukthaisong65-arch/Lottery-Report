package th.co.lotteryreport.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawResultValidatorTest {
    private val drawDate = "2026-11-16"
    private val complete = buildList {
        add(PrizeValue("FIRST_PRIZE", "123456"))
        addAll(listOf(PrizeValue("FRONT_THREE", "123"), PrizeValue("FRONT_THREE", "456")))
        addAll(listOf(PrizeValue("LAST_THREE", "789"), PrizeValue("LAST_THREE", "012")))
        add(PrizeValue("LAST_TWO", "34"))
        (1..5).forEach { add(PrizeValue("SECOND_PRIZE", "%06d".format(it))) }
        (1..10).forEach { add(PrizeValue("THIRD_PRIZE", "%06d".format(it + 10))) }
        (1..50).forEach { add(PrizeValue("FOURTH_PRIZE", "%06d".format(it + 20))) }
        (1..100).forEach { add(PrizeValue("FIFTH_PRIZE", "%06d".format(it + 100))) }
        addAll(listOf(PrizeValue("NEARBY_FIRST_PRIZE", "654321"), PrizeValue("NEARBY_FIRST_PRIZE", "654322")))
    }

    @Test fun acceptsCompleteOfficialPrizeSet() {
        assertTrue(DrawResultValidator.validate(drawDate, complete))
    }

    @Test fun rejectsMissingPrizeCategory() {
        assertFalse(DrawResultValidator.validate(drawDate, complete.filterNot { it.category == "LAST_TWO" }))
    }

    @Test fun rejectsIncompletePrizeGroup() {
        assertFalse(DrawResultValidator.validate(drawDate, complete.dropLast(1)))
    }

    @Test fun rejectsMalformedNumber() {
        val invalid = complete.toMutableList().apply { set(0, PrizeValue("FIRST_PRIZE", "ABC456")) }
        assertFalse(DrawResultValidator.validate(drawDate, invalid))
    }

    @Test fun rejectsDuplicateNumberWithinGroup() {
        val invalid = complete.toMutableList().apply {
            set(2, PrizeValue("FRONT_THREE", "123"))
        }
        assertFalse(DrawResultValidator.validate(drawDate, invalid))
    }

    @Test fun rejectsUnknownCategory() {
        assertFalse(DrawResultValidator.validate(drawDate, complete + PrizeValue("UNKNOWN", "123")))
    }

    @Test fun rejectsInvalidDrawDate() {
        assertFalse(DrawResultValidator.validate("not-a-date", complete))
    }

    @Test fun rejectsEmptyResponse() {
        assertFalse(DrawResultValidator.validate(drawDate, emptyList()))
    }
}
