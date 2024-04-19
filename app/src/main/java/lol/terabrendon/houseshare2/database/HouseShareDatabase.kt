package lol.terabrendon.houseshare2.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import lol.terabrendon.houseshare2.dao.ExpenseDao
import lol.terabrendon.houseshare2.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.entity.DateConverter
import lol.terabrendon.houseshare2.entity.Expense
import lol.terabrendon.houseshare2.entity.ExpenseOfUser
import lol.terabrendon.houseshare2.entity.ShoppingItem
import lol.terabrendon.houseshare2.entity.User

@Database(
    entities = [ShoppingItem::class, User::class, Expense::class, ExpenseOfUser::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, Expense.Converter::class)
abstract class HouseShareDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao

    abstract fun expenseDao(): ExpenseDao
}