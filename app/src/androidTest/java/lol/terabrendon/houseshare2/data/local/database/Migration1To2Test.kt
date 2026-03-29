package lol.terabrendon.houseshare2.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {
    companion object {
        private const val TEST_DB = "group-migration-test"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HouseShareDatabase::class.java
    )

    @Test
    fun migrate1To2_addsNullableImageUrlColumn() {
        // Create DB at version 1
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO `Group` (id, name, description)
                VALUES (1, 'Group A', 'desc')
            """.trimIndent()
            )
            close()
        }

        // Run migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Verify data
        val cursor = db.query("SELECT id, name, description, imageUrl FROM `Group`")

        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getLong(0))
        assertEquals("Group A", cursor.getString(1))
        assertEquals("desc", cursor.getString(2))
        assertNull(cursor.getString(3)) // new column is NULL

        cursor.close()
    }


    @Test
    fun migrate1To2_imageUrlColumnIsNullable() {
        helper.createDatabase(TEST_DB, 1).apply { close() }
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        val cursor = db.query("PRAGMA table_info(`Group`)")
        var found = false

        val nameIndex = cursor.getColumnIndex("name")
        val notNullIndex = cursor.getColumnIndex("notnull")

        while (cursor.moveToNext()) {
            val columnName = cursor.getString(nameIndex)
            if (columnName == "imageUrl") {
                found = true
                val notNull = cursor.getInt(notNullIndex)
                assertEquals(0, notNull) // nullable
            }
        }

        assertTrue(found)
        cursor.close()
    }
}