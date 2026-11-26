package cn.campus.ledger.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LedgerDao {

    @Query("SELECT * FROM bills ORDER BY day DESC, id DESC")
    fun all(): List<Bill>

    @Query("SELECT * FROM bills WHERE day >= :start AND day < :end ORDER BY day DESC, id DESC")
    fun month(start: String, end: String): List<Bill>

    @Query("SELECT * FROM bills WHERE id = :id")
    fun bill(id: Long): Bill?

    @Insert
    fun add(bill: Bill): Long

    @Update
    fun update(bill: Bill): Int

    @Delete
    fun remove(bill: Bill): Int
}
