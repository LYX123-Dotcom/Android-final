package cn.campus.ledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var income: Boolean = false,
    var cents: Long = 0L,
    var day: String = "",
    var category: String = "",
    var note: String = ""
)
