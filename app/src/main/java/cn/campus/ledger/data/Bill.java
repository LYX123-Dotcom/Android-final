package cn.campus.ledger.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="bills")
public class Bill {
    @PrimaryKey(autoGenerate=true) public long id;
    public boolean income;
    public long cents;
    @NonNull public String day = "";
    @NonNull public String category = "";
    @NonNull public String note = "";
}
