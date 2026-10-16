
package cl.triskeledu.aprendogame.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
 * Gestiona el almacenamiento local SQLite.
 */
@Database(
    entities = [
        PuntoInteres::class,
        Capsula::class,
        Usuario::class,
        VisitaRealizada::class
    ],
    version = 3,
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

        /**
         * Migración de la versión 1 a la versión 2.
         * Agrega los campos del registro sin borrar usuarios existentes.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN alias TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN correo TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN telefono TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN contrasena TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN direccion TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE usuarios ADD COLUMN avatar INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        /**
         * Migración de la versión 2 a la versión 3.
         * Impide correos duplicados sin borrar los jugadores.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

                // Comprueba si existen correos repetidos.
                db.query(
                    """
                    SELECT correo, COUNT(*) AS cantidad
                    FROM usuarios
                    GROUP BY correo
                    HAVING COUNT(*) > 1
                    """.trimIndent()
                ).use { cursor ->

                    if (cursor.moveToFirst()) {
                        throw IllegalStateException(
                            "Existen correos duplicados. " +
                                    "Debemos revisarlos antes de crear el índice único."
                        )
                    }
                }

                // Crea el índice único que espera Room.
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_usuarios_correo
                    ON usuarios(correo)
                    """.trimIndent()
                )
            }
        }

        /**
         * Obtiene una única instancia de la base de datos.
         */
        fun getInstance(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aprendo_santiago_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3
                    )
                    .addCallback(object : Callback() {

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            // Inicializar datos cuando se crea la base.
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


