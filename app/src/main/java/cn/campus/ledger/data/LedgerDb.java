package cn.campus.ledger.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Database(entities = {Bill.class}, version = 1, exportSchema = false)
public abstract class LedgerDb extends RoomDatabase {
    public abstract LedgerDao dao();
    private static LedgerDb instance;
    private static final ExecutorService worker = Executors.newSingleThreadExecutor();
    private static final Handler main = new Handler(Looper.getMainLooper());

    public static synchronized LedgerDao get(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    LedgerDb.class, "campus-ledger.db").build();
        }
        return instance.dao();
    }

    public static <T> void run(Callable<T> job, Consumer<T> onSuccess, Runnable onFailure) {
        worker.execute(() -> {
            try {
                T result = job.call();
                main.post(() -> onSuccess.accept(result));
            } catch (Exception e) {
                Log.e("CampusLedger", "Database operation failed", e);
                main.post(onFailure);
            }
        });
    }
}
