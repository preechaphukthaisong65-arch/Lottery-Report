package th.co.lotteryreport.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "draw_results",
    indices = [Index(value = ["drawDate", "category", "number"], unique = true)]
)
data class DrawResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drawDate: String,
    val category: String,
    val number: String,
    val sourceUrl: String,
    val verifiedAtEpochMillis: Long
)
