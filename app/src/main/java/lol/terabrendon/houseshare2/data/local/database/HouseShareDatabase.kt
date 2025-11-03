package lol.terabrendon.houseshare2.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import lol.terabrendon.houseshare2.data.entity.DateConverter
import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.ExpensePart
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.GroupUserCrossRef
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.local.dao.ExpenseDao
import lol.terabrendon.houseshare2.data.local.dao.GroupDao
import lol.terabrendon.houseshare2.data.local.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.data.local.dao.UserDao

@Database(
    entities = [ShoppingItem::class, User::class, Expense::class, ExpensePart::class, Group::class, GroupUserCrossRef::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, Expense.Converter::class)
abstract class HouseShareDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao

    abstract fun expenseDao(): ExpenseDao

    abstract fun userDao(): UserDao

    abstract fun groupDao(): GroupDao
}