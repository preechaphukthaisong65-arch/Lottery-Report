package th.co.lotteryreport.domain

data class PrizeValue(
    val category: String,
    val number: String
)

object DrawResultValidator {
    private val requiredCategories = setOf(
        "FIRST_PRIZE",
        "FRONT_THREE",
        "LAST_THREE",
        "LAST_TWO"
    )

    fun validate(values: List<PrizeValue>): Boolean {
        if (values.isEmpty()) return false
        if (values.any { it.category.isBlank() || it.number.isBlank() }) return false
        if (values.any { value ->
                when (value.category) {
                    "FIRST_PRIZE" -> value.number.length != 6 || !value.number.all(Char::isDigit)
                    "FRONT_THREE", "LAST_THREE" ->
                        value.number.length != 3 || !value.number.all(Char::isDigit)
                    "LAST_TWO" -> value.number.length != 2 || !value.number.all(Char::isDigit)
                    else -> true
                }
            }) return false

        return requiredCategories.all { category -> values.any { it.category == category } }
    }
}
