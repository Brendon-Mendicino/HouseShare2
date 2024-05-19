package lol.terabrendon.houseshare2.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.entity.ExpenseWithUsers
import lol.terabrendon.houseshare2.entity.PaymentWithUser
import java.time.LocalDateTime

data class ExpenseModel(
    val id: Long,
    val amount: Double,
    val expenseOwner: UserModel,
    val category: ExpenseCategory,
    val title: String,
    val description: String?,
    val creationTimestamp: LocalDateTime,
    val userExpenses: List<UserExpenseModel>,
) {
    companion object {
        @JvmStatic
        fun from(expense: ExpenseWithUsers): ExpenseModel = ExpenseModel(
            id = expense.expense.id,
            amount = expense.expense.amount,
            expenseOwner = UserModel.from(expense.owner),
            category = expense.expense.category,
            title = expense.expense.title,
            description = expense.expense.description,
            creationTimestamp = expense.expense.creationTimestamp,
            userExpenses = expense.expensesWithUser.map { UserExpenseModel.from(it) }
        )

        @JvmStatic
        fun default(): ExpenseModel = ExpenseModel(
            id = 0,
            amount = 0.0,
            expenseOwner = UserModel.default(),
            category = ExpenseCategory.Car,
            title = "Title",
            description = "Description",
            creationTimestamp = LocalDateTime.now(),
            userExpenses = listOf(),
        )
    }
}

data class UserExpenseModel(
    val user: UserModel,
    val amount: Double,
) {
    companion object {
        @JvmStatic
        fun from(payment: PaymentWithUser): UserExpenseModel = UserExpenseModel(
            user = UserModel.from(payment.user),
            amount = payment.payment.amount,
        )

        @JvmStatic
        fun default(): UserExpenseModel = UserExpenseModel(user = UserModel.default(), amount = 0.0)

    }
}

enum class ExpenseCategory {
    Car,
    Education,
    Training,
    ElectricBill,
    GasBill,
    Bill,
    Home,
    Restaurant,
    Games,
    FreeTime,
    Shopping,
    Travel,
    Rent,
    Meals,
    Others;

    @StringRes
    fun toStringRes(): Int = when (this) {
        Car -> R.string.car
        Education -> R.string.education
        Training -> R.string.training
        ElectricBill -> R.string.electric_bill
        GasBill -> R.string.gas_bill
        Bill -> R.string.bill
        Home -> R.string.home
        Restaurant -> R.string.restaurant
        Games -> R.string.games
        FreeTime -> R.string.free_time
        Shopping -> R.string.shopping
        Travel -> R.string.travel
        Rent -> R.string.rent
        Meals -> R.string.meals
        Others -> R.string.others
    }

    fun toImageVector(): ImageVector = when (this) {
        Car -> Icons.Filled.DirectionsCar
        Education -> Icons.Filled.Book
        Training -> Icons.Filled.FitnessCenter
        ElectricBill -> Icons.Filled.ElectricBolt
        GasBill -> Icons.Filled.GasMeter
        Bill -> Icons.AutoMirrored.Filled.ReceiptLong
        Home -> Icons.Filled.Home
        Restaurant -> Icons.Filled.Restaurant
        Games -> Icons.Filled.SportsEsports
        FreeTime -> Icons.Filled.Cloud
        Shopping -> Icons.Filled.ShoppingBag
        Travel -> Icons.Filled.Flight
        Rent -> Icons.Filled.Paid
        Meals -> Icons.Filled.ShoppingBasket
        Others -> Icons.Filled.MoreHoriz
    }
}