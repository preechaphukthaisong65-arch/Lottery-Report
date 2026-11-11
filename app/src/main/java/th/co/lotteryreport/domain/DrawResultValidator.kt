package th.co.lotteryreport.domain

import java.time.LocalDate

data class PrizeValue(
    val category: String,
    val number: String
)

object DrawResultValidator {
    private val expected = mapOf(
        "FIRST_PRIZE" to (6 to 1),
        "FRONT_THREE" to (3 to 2),
        "LAST_THREE" to (3 to 2),
        "LAST_TWO" to (2 to 1),
        "SECOND_PRIZE" to (6 to 5),
        "THIRD_PRIZE" to (6 to 10),
        "FOURTH_PRIZE" to (6 to 50),
        "FIFTH_PRIZE" to (6 to 100),
        "NEARBY_FIRST_PRIZE" to (6 to 2)
    )

    /**
     * Requires every L6 prize category and its exact official number count.
     * This checks structure only; source authenticity must be verified separately.
     */
    fun validate(drawDate: String, values: List<PrizeValue>): Boolean {
        if (runCatching { LocalDate.parse(drawDate) }.isFailure) return false
        if (values.isEmpty() || values.any { it.category !in expected }) return false
        if (values.any { it.number.any { char -> char !in '0'..'9' } }) return false

        for ((category, rule) in expected) {
            val group = values.filter { it.category == category }
            if (group.size != rule.second) return false
            if (group.any { it.number.length != rule.first }) return false
            if (group.map { it.number }.distinct().size != group.size) return false
        }
        return values.size == expected.values.sumOf { it.second }
    }
}
