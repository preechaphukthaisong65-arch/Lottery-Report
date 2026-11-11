package th.co.lotteryreport.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draw_schedules")
data class DrawScheduleEntity(
    @PrimaryKey val drawDate: String,
    val status: String,
    val sourceUrl: String?,
    val sourcePublishedAtEpochMillis: Long?,
    val checkedAtEpochMillis: Long
) {
    val isOfficiallyConfirmed: Boolean
        get() = status == "CONFIRMED" && !sourceUrl.isNullOrBlank()
}
