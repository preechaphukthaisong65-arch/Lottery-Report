package th.co.lotteryreport.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verification_events")
data class VerificationEventEntity(
    @PrimaryKey val eventKey: String,
    val mode: String,
    val checkedAt: String,
    val status: String,
    val message: String,
    val sources: String
)
