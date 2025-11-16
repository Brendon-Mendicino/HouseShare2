package lol.terabrendon.houseshare2.domain.model

import android.annotation.SuppressLint
import io.github.brendonmendicino.aformvalidator.annotation.DependsOn
import io.github.brendonmendicino.aformvalidator.annotation.FormState
import io.github.brendonmendicino.aformvalidator.annotation.Min
import io.github.brendonmendicino.aformvalidator.annotation.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.ToNumber

@SuppressLint("KotlinNullnessAnnotation")
@FormState
data class ShoppingItemFormState(
    @NotBlank
    val name: String = "",
    @NotBlank
    @ToNumber(Int::class)
    val amountStr: String = "1",
    @ToNumber(Double::class)
    val priceStr: String? = null,
    val priority: ShoppingItemPriority = ShoppingItemPriority.Later,
    val ownerId: Long = 0,
    val groupId: Long = 0,
) {
    @Min(0)
    @DependsOn(["amountStr"])
    val amount: Int? = amountStr.toIntOrNull()

    @Min(0)
    @DependsOn(["priceStr"])
    val price: Money? = priceStr?.toMoneyOrNull()
}