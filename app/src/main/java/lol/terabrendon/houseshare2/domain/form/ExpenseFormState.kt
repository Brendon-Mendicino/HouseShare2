package lol.terabrendon.houseshare2.domain.form

import android.annotation.SuppressLint
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Min
import io.github.brendonmendicino.aformvalidator.annotation.annotations.MinDouble
import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotNull
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Pattern
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Size
import io.github.brendonmendicino.aformvalidator.annotation.annotations.ToNumber
import io.github.brendonmendicino.aformvalidator.core.DependsOn
import io.github.brendonmendicino.aformvalidator.core.FormState
import lol.terabrendon.houseshare2.domain.model.ExpenseCategory
import lol.terabrendon.houseshare2.domain.model.Money
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.domain.model.sum
import lol.terabrendon.houseshare2.domain.model.toMoney
import lol.terabrendon.houseshare2.domain.model.toMoneyOrNull
import lol.terabrendon.houseshare2.presentation.billing.PaymentUnit
import lol.terabrendon.houseshare2.util.IsTrue
import java.math.BigDecimal

@SuppressLint("KotlinNullnessAnnotation")
@FormState
data class ExpenseFormState(
    @NotBlank
    @ToNumber(numberClass = Double::class)
    @Pattern(regex = """^\s*\d+(\.(\d{1,2})?)?\s*$""")
    val totalAmount: String = "",
    @NotBlank
    @Size(max = 250)
    val description: String? = null,
    @NotBlank
    @Size(max = 250)
    val title: String = "",
    @NotNull
    val category: ExpenseCategory? = null,
    @NotNull
    val payer: UserModel? = null,
    val userParts: List<UserPart> = emptyList(),
    val simpleDivisionEnabled: Boolean = true,
) {
    @MinDouble(min = 0.01)
    @DependsOn(["totalAmount"])
    val totalAmountMoney: Money = totalAmount.toMoneyOrNull() ?: Money.ZERO

    val convertedValues: List<Money>
        get() {
            val totalMoney = totalAmountMoney

            val nonQuotaMoney = userParts
                .map { part ->
                    when (part.paymentUnit) {
                        PaymentUnit.Additive -> part.amountMoney
                        PaymentUnit.Percentage -> (totalMoney * part.amountDouble) / 100
                        PaymentUnit.Quota -> 0.toMoney()
                    }
                }
                .sum()

            val totalQuotes = userParts
                .sumOf { part -> if (part.paymentUnit == PaymentUnit.Quota) part.amountDouble else 0.0 }

            val converted = userParts
                .map { part ->
                    val moneyPerQuota =
                        if (totalQuotes > 0.0) (totalMoney - nonQuotaMoney) * (part.amountDouble / totalQuotes)
                        else 0.toMoney()

                    when (part.paymentUnit) {
                        PaymentUnit.Additive -> part.amountMoney
                        PaymentUnit.Percentage -> (totalMoney * part.amountDouble) / 100
                        PaymentUnit.Quota -> moneyPerQuota
                    }
                }
                .toMutableList()

            // Assign left overs for quotas or percentages
            val totalPerc = userParts.sumOf { it.amountBig }.toInt()
            if (totalQuotes != 0.0 || totalPerc == 100) {
                var leftovers = totalMoney - converted.sum()

                val indexes = generateSequence { 0..<converted.size }.flatMap { it }.iterator()

                while (leftovers > 0) {
                    val i = indexes.next()
                    val unit = userParts[i].paymentUnit
                    if (unit != PaymentUnit.Quota && unit != PaymentUnit.Percentage) {
                        continue
                    }

                    converted[i] += Money.ATOM
                    leftovers -= Money.ATOM
                }

                check(converted.sum() == totalMoney) { "Converted money must be equal to the total the leftover assignment!" }
            }

            return converted.toList()
        }

    @IsTrue
    val partsEqualTotal: Boolean =
        simpleDivisionEnabled || convertedValues.sum() == totalAmountMoney
}

@FormState
data class UserPart(
    val paymentUnit: PaymentUnit = PaymentUnit.Additive,
    @ToNumber(numberClass = Double::class)
    val amount: String = "",
) {
    @Min(min = 0)
    @DependsOn(["amount"])
    val amountDouble: Double = amount.toDoubleOrNull() ?: 0.0

    val amountBig: BigDecimal = amount.toBigDecimalOrNull() ?: 0.toBigDecimal()

    val amountMoney: Money = amount.toMoneyOrNull() ?: Money.ZERO
}
