package lol.terabrendon.houseshare2.presentation.billing

import android.annotation.SuppressLint
import io.github.brendonmendicino.aformvalidator.annotation.FormState
import io.github.brendonmendicino.aformvalidator.annotation.Min
import io.github.brendonmendicino.aformvalidator.annotation.MinDouble
import io.github.brendonmendicino.aformvalidator.annotation.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.NotNull
import io.github.brendonmendicino.aformvalidator.annotation.ToNumber
import lol.terabrendon.houseshare2.domain.model.ExpenseCategory
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.util.IsTrue
import lol.terabrendon.houseshare2.util.currencyFormat

@SuppressLint("KotlinNullnessAnnotation")
@FormState
data class ExpenseFormState(
    @NotBlank
    @ToNumber(Double::class)
    val totalAmount: String = "",
    val description: String? = null,
    @NotBlank
    val title: String = "",
    val category: ExpenseCategory? = null,
    @NotNull
    val payer: UserModel? = null,
    val userParts: List<UserPart> = emptyList(),
    val simpleDivisionEnabled: Boolean = true,
) {
    @MinDouble(0.0)
    val totalAmountNum: Double = totalAmount.toDoubleOrNull() ?: 0.0

    val convertedValues: List<Double>
        get() {
            val totalMoney = totalAmount.toDoubleOrNull() ?: 0.0

            val nonQuotaMoney = userParts
                .sumOf { part ->
                    when (part.paymentUnit) {
                        PaymentUnit.Additive -> part.amountNum
                        PaymentUnit.Percentage -> totalMoney * (part.amountNum / 100.0)
                        PaymentUnit.Quota -> 0.0
                    }
                }

            val totalQuotes = userParts
                .sumOf { part -> if (part.paymentUnit == PaymentUnit.Quota) part.amountNum else 0.0 }

            val converted = userParts
                .map { part ->
                    val moneyPerQuota =
                        if (totalQuotes != 0.0) (totalMoney - nonQuotaMoney) * part.amountNum / totalQuotes else 0.0

                    val paymentAmount = when (part.paymentUnit) {
                        PaymentUnit.Additive -> part.amountNum
                        PaymentUnit.Percentage -> (part.amountNum / 100.0) * totalMoney
                        PaymentUnit.Quota -> moneyPerQuota
                    }

                    paymentAmount
                }

            return converted
        }

    @IsTrue
    val partsEqualTotal: Boolean =
        simpleDivisionEnabled || convertedValues.sum()
            .currencyFormat() == totalAmountNum.currencyFormat()
}

@FormState
data class UserPart(
    val paymentUnit: PaymentUnit = PaymentUnit.Additive,
    @ToNumber(Double::class)
    val amount: String = "",
) {
    @Min(0)
    val amountNum: Double = amount.toDoubleOrNull() ?: 0.0
}
