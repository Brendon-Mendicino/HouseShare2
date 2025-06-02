package lol.terabrendon.houseshare2.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import lol.terabrendon.houseshare2.data.dao.ExpenseDao
import lol.terabrendon.houseshare2.data.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.data.dao.UserDao
import lol.terabrendon.houseshare2.data.entity.DateConverter
import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.Payment
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.User

@Database(
    entities = [ShoppingItem::class, User::class, Expense::class, Payment::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, Expense.Converter::class)
abstract class HouseShareDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao

    abstract fun expenseDao(): ExpenseDao

    abstract fun userDao(): UserDao
}