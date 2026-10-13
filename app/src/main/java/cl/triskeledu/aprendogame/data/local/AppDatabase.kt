package cl.triskeledu.aprendogame.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.triskeledu.aprendogame.data.local.dao.CapsulaDao
import cl.triskeledu.aprendogame.data.local.dao.PuntoInteresDao
import cl.triskeledu.aprendogame.data.local.dao.UsuarioDao
import cl.triskeledu.aprendogame.data.local.dao.VisitaDao
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VisitaRealizada

/**
 * Base de datos Room central de la aplicación.
 * Gestiona el almacenamiento local SQLite 100% offline y autónomo.
 */
@Database(
    entities = [
        PuntoInteres::class,
        Capsula::class,
        Usuario::class,
        VisitaRealizada::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun puntoInteresDao(): PuntoInteresDao
    abstract fun capsulaDao(): CapsulaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun visitaDao(): VisitaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aprendo_santiago_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                DatabaseSeeder.seedDatabase(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
