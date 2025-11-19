package lol.terabrendon.houseshare2.domain.form

import android.annotation.SuppressLint
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Min
import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.annotations.ToNumber
import io.github.brendonmendicino.aformvalidator.core.DependsOn
import io.github.brendonmendicino.aformvalidator.core.FormState
import lol.terabrendon.houseshare2.domain.model.Money
import lol.terabrendon.houseshare2.domain.model.ShoppingItemPriority
import lol.terabrendon.houseshare2.domain.model.toMoneyOrNull

@SuppressLint("KotlinNullnessAnnotation")
@FormState
data class ShoppingItemFormState(
    @NotBlank
    val name: String = "",
    @NotBlank
    @ToNumber(numberClass = Int::class)
    val amountStr: String = "1",
    @ToNumber(numberClass = Double::class)
    val priceStr: String? = null,
    val priority: ShoppingItemPriority = ShoppingItemPriority.Later,
    val ownerId: Long = 0,
    val groupId: Long = 0,
) {
    @Min(min = 0)
    @DependsOn(["amountStr"])
    val amount: Int? = amountStr.toIntOrNull()

    @Min(min = 0)
    @DependsOn(["priceStr"])
    val price: Money? = priceStr?.toMoneyOrNull()
}