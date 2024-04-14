package lol.terabrendon.houseshare2.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import lol.terabrendon.houseshare2.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.entity.ShoppingItem

@Database(entities = [ShoppingItem::class], version = 1, exportSchema = true)
@TypeConverters(ShoppingItem.Converter::class)
abstract class HouseShareDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao
}