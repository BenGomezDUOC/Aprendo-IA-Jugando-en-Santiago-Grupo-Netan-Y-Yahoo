
@file:Suppress("LanguageDetectionInspection")

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
import cl.triskeledu.aprendogame.data.local.dao.VideoAdicionalVistoDao
import cl.triskeledu.aprendogame.data.local.dao.VisitaDao
import cl.triskeledu.aprendogame.data.local.dao.NotificacionDao
import cl.triskeledu.aprendogame.data.local.dao.ContenidoEducativoDao
import cl.triskeledu.aprendogame.data.local.dao.VideoRegistradoDao

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.DescubrimientoQr
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VideoVisto
import cl.triskeledu.aprendogame.model.VideoAdicionalVisto
import cl.triskeledu.aprendogame.model.VisitaRealizada
import cl.triskeledu.aprendogame.model.Notificacion
import cl.triskeledu.aprendogame.model.ContenidoEducativo
import cl.triskeledu.aprendogame.model.VideoRegistrado

/**
 * Base de datos Room central de Triskeledu.
 * Conserva los usuarios y su progreso.
 */
@Database(
    entities = [
        PuntoInteres::class,
        Capsula::class,
        Usuario::class,
        VisitaRealizada::class,
        DescubrimientoQr::class,
        VideoVisto::class,
        Notificacion::class,
        VideoAdicionalVisto::class,
        ContenidoEducativo::class,
        VideoRegistrado::class
    ],
    version = 13,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun puntoInteresDao(): PuntoInteresDao

    abstract fun capsulaDao(): CapsulaDao

    abstract fun usuarioDao(): UsuarioDao

    abstract fun visitaDao(): VisitaDao

    abstract fun descubrimientoQrDao(): DescubrimientoQrDao

    abstract fun videoVistoDao(): VideoVistoDao

    abstract fun videoAdicionalVistoDao(): VideoAdicionalVistoDao

    abstract fun notificacionDao(): NotificacionDao

    abstract fun contenidoEducativoDao(): ContenidoEducativoDao

    abstract fun videoRegistradoDao(): VideoRegistradoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // MIGRACIÓN 1 -> 2

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

        // MIGRACIÓN 2 -> 3

        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

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

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_usuarios_correo
                    ON usuarios(correo)
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 3 -> 4

        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(db: SupportSQLiteDatabase) {

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

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_descubrimientos_qr_usuarioId_capsulaId`
                    ON `descubrimientos_qr` (`usuarioId`, `capsulaId`)
                    """.trimIndent()
                )

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

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_videos_vistos_usuarioId_capsulaId`
                    ON `videos_vistos` (`usuarioId`, `capsulaId`)
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 4 -> 5

        private val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE usuarios
                    ADD COLUMN tipoCuentaSistema TEXT DEFAULT NULL
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    UPDATE usuarios
                    SET tipoCuentaSistema = 'ADMIN_PRINCIPAL'
                    WHERE correo = 'admin@triskeledu.cl'
                      AND rol = 'ADMINISTRADOR'
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    UPDATE usuarios
                    SET tipoCuentaSistema = 'GENERADOR_PRINCIPAL'
                    WHERE correo = 'contenido@triskeledu.cl'
                      AND rol = 'GENERADOR'
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_usuarios_tipoCuentaSistema
                    ON usuarios(tipoCuentaSistema)
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 5 -> 6

        private val MIGRATION_5_6 = object : Migration(5, 6) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `notificaciones` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `usuarioId` INTEGER NOT NULL,
                        `titulo` TEXT NOT NULL,
                        `mensaje` TEXT NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `rutaDestino` TEXT NOT NULL,
                        `fechaTimestamp` INTEGER NOT NULL,
                        `leida` INTEGER NOT NULL,
                        FOREIGN KEY(`usuarioId`)
                            REFERENCES `usuarios`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_notificaciones_usuarioId`
                    ON `notificaciones` (`usuarioId`)
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 6 -> 7

        private val MIGRATION_6_7 = object : Migration(6, 7) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `videos_adicionales_vistos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `usuarioId` INTEGER NOT NULL,
                        `videoAdicionalId` INTEGER NOT NULL,
                        `fechaTimestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`usuarioId`)
                            REFERENCES `usuarios`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_videos_adicionales_vistos_usuarioId`
                    ON `videos_adicionales_vistos` (`usuarioId`)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_videos_adicionales_vistos_usuarioId_videoAdicionalId`
                    ON `videos_adicionales_vistos`
                    (`usuarioId`, `videoAdicionalId`)
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 7 -> 8

        private val MIGRATION_7_8 = object : Migration(7, 8) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE capsulas
                    ADD COLUMN categoria TEXT NOT NULL DEFAULT 'General'
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    ALTER TABLE capsulas
                    ADD COLUMN estado TEXT NOT NULL DEFAULT 'PUBLICADA'
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `contenidos_educativos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `titulo` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `categoria` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        `puntoId` INTEGER,
                        FOREIGN KEY(`puntoId`)
                            REFERENCES `puntos_interes`(`id`)
                            ON UPDATE NO ACTION
                            ON DELETE SET NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_contenidos_educativos_puntoId`
                    ON `contenidos_educativos` (`puntoId`)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `videos_registrados` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `titulo` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `urlVideo` TEXT NOT NULL,
                        `contenidoId` INTEGER,
                        FOREIGN KEY(`contenidoId`)
                            REFERENCES `contenidos_educativos`(`id`)
                            ON UPDATE NO ACTION
                            ON DELETE SET NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_videos_registrados_contenidoId`
                    ON `videos_registrados` (`contenidoId`)
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 8 -> 9

        private val MIGRATION_8_9 = object : Migration(8, 9) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE contenidos_educativos
                    ADD COLUMN codigoQr TEXT DEFAULT NULL
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_contenidos_educativos_codigoQr`
                    ON `contenidos_educativos` (`codigoQr`)
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 9 -> 10

        private val MIGRATION_9_10 = object : Migration(9, 10) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE videos_registrados
                    ADD COLUMN autor TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    ALTER TABLE videos_registrados
                    ADD COLUMN duracion INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 10 -> 11

        private val MIGRATION_10_11 = object : Migration(10, 11) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE videos_registrados
                    ADD COLUMN urlMiniatura TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN 11 -> 12

        private val MIGRATION_11_12 = object : Migration(11, 12) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE puntos_interes
                    ADD COLUMN imagenUri TEXT DEFAULT NULL
                    """.trimIndent()
                )
            }
        }

        // =============================================
        // NUEVA MIGRACIÓN 12 -> 13
        // =============================================

        // Agrega la dirección de cada punto turístico.
        // Conserva todos los registros existentes.

        private val MIGRATION_12_13 = object : Migration(12, 13) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE puntos_interes
                    ADD COLUMN direccion TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
            }
        }

        // =============================================
        // INSTANCIA DE ROOM
        // =============================================

        fun getInstance(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(

                    context.applicationContext,

                    AppDatabase::class.java,

                    "aprendo_santiago_database"

                ).addMigrations(

                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13

                ).build().also { instance ->

                    INSTANCE = instance

                    // Carga los registros iniciales faltantes.
                    DatabaseSeeder.seedDatabase(instance)
                }
            }
        }
    }
}
