package lol.terabrendon.houseshare2.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE `Group`
            ADD COLUMN `imageUrl` TEXT
            """.trimIndent()
        )
    }
}
