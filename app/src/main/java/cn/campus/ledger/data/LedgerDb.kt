package cn.campus.ledger.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [Bill::class], version = 1, exportSchema = false)
abstract class LedgerDb : RoomDatabase() {

    abstract fun dao(): LedgerDao

    companion object {

        private const val NAME = "campus-ledger.db"

        private val worker: ExecutorService = Executors.newSingleThreadExecutor()
        private val main = Handler(Looper.getMainLooper())

        @Volatile
        private var instance: LedgerDb? = null

        fun get(context: Context): LedgerDao {
            instance?.let { return it.dao() }
            synchronized(this) {
                instance?.let { return it.dao() }
                val created = Room.databaseBuilder(
                    context.applicationContext,
                    LedgerDb::class.java,
                    NAME
                ).build()
                instance = created
                return created.dao()
            }
        }

        fun <T> run(job: () -> T, onSuccess: (T) -> Unit, onFailure: () -> Unit) {
            worker.execute {
                try {
                    val result = job()
                    main.post { onSuccess(result) }
                } catch (e: Exception) {
                    Log.e("CampusLedger", "Database operation failed", e)
                    main.post { onFailure() }
                }
            }
        }
    }
}
