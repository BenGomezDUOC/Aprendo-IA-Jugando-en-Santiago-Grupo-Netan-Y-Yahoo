package cl.triskeledu.aprendogame.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import cl.triskeledu.aprendogame.data.local.dao.CapsulaDao
import cl.triskeledu.aprendogame.data.local.dao.DescubrimientoQrDao
import cl.triskeledu.aprendogame.data.local.dao.PuntoInteresDao
import cl.triskeledu.aprendogame.data.local.dao.UsuarioDao
import cl.triskeledu.aprendogame.data.local.dao.VideoVistoDao
import cl.triskeledu.aprendogame.data.local.dao.VisitaDao

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.DescubrimientoQr
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VideoVisto
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
        VisitaRealizada::class,
        DescubrimientoQr::class,
        VideoVisto::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun puntoInteresDao(): PuntoInteresDao
    abstract fun capsulaDao(): CapsulaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun visitaDao(): VisitaDao

    // DAO para los QR descubiertos.
    abstract fun descubrimientoQrDao(): DescubrimientoQrDao

    // DAO para los videos vistos.
    abstract fun videoVistoDao(): VideoVistoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migración de la versión 1 a la versión 2.
         * Agrega los campos del registro sin borrar usuarios.
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
         * Migración de la versión 3 a la versión 4.
         *
         * Crea las tablas para registrar los QR descubiertos
         * y los videos vistos por cada jugador.
         *
         * No elimina las tablas existentes.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(db: SupportSQLiteDatabase) {

                // 1. Crear la tabla de QR descubiertos.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `descubrimientos_qr` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `usuarioId` INTEGER NOT NULL,
                        `capsulaId` INTEGER NOT NULL,
                        `fechaTimestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`usuarioId`)
                            REFERENCES `usuarios`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`capsulaId`)
                            REFERENCES `capsulas`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Índices para consultar los descubrimientos.
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_descubrimientos_qr_usuarioId`
                    ON `descubrimientos_qr` (`usuarioId`)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_descubrimientos_qr_capsulaId`
                    ON `descubrimientos_qr` (`capsulaId`)
                    """.trimIndent()
                )

                // Un jugador no puede registrar dos veces la misma cápsula.
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_descubrimientos_qr_usuarioId_capsulaId`
                    ON `descubrimientos_qr` (`usuarioId`, `capsulaId`)
                    """.trimIndent()
                )

                // 2. Crear la tabla de videos vistos.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `videos_vistos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `usuarioId` INTEGER NOT NULL,
                        `capsulaId` INTEGER NOT NULL,
                        `fechaTimestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`usuarioId`)
                            REFERENCES `usuarios`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`capsulaId`)
                            REFERENCES `capsulas`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Índices para consultar los videos vistos.
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_videos_vistos_usuarioId`
                    ON `videos_vistos` (`usuarioId`)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_videos_vistos_capsulaId`
                    ON `videos_vistos` (`capsulaId`)
                    """.trimIndent()
                )

                // Un video visto se registra una vez por jugador.
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_videos_vistos_usuarioId_capsulaId`
                    ON `videos_vistos` (`usuarioId`, `capsulaId`)
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
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()

                INSTANCE = instance
                // Carga los lugares y las cápsulas que falten.
               // También funciona si la base de datos ya existía.
                DatabaseSeeder.seedDatabase(instance)

                instance
            }
        }
    }
}


