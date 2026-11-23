package cn.campus.ledger.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface LedgerDao {
    @Query("SELECT * FROM bills ORDER BY day DESC, id DESC")
    List<Bill> all();

    // Half-open date range, e.g. 2026-11-01 to 2026-12-01.
    @Query("SELECT * FROM bills WHERE day >= :start AND day < :end ORDER BY day DESC, id DESC")
    List<Bill> month(String start, String end);

    @Query("SELECT * FROM bills WHERE id = :id")
    Bill bill(long id);

    @Insert long add(Bill bill);
    @Update int update(Bill bill);
    @Delete int remove(Bill bill);
}
