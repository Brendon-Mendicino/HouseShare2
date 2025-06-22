package lol.terabrendon.houseshare2.domain.model

import android.annotation.SuppressLint
import io.github.brendonmendicino.aformvalidator.annotation.FormState
import io.github.brendonmendicino.aformvalidator.annotation.Min
import io.github.brendonmendicino.aformvalidator.annotation.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.NotNull
import io.github.brendonmendicino.aformvalidator.annotation.ToNumber

@SuppressLint("KotlinNullnessAnnotation")
@FormState
data class ShoppingItemFormState(
    @NotBlank
    val name: String = "",
    @ToNumber(Int::class)
    @NotNull
    val amountStr: String? = null,
    @Min(0)
    val amount: Int? = null,
    @ToNumber(Double::class)
    val priceStr: String? = null,
    @Min(0)
    val price: Double? = 0.0,
)