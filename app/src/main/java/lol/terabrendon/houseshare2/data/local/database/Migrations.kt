package lol.terabrendon.houseshare2.data.local.database

import androidx.room.migration.Migration

@Deprecated("This migration is not needed anymore")
val MIGRATION_1_2 = Migration(1, 2) { db ->
    db.execSQL(
        """
        ALTER TABLE `Group`
        ADD COLUMN `imageUrl` TEXT
        """.trimIndent()
    )
}
