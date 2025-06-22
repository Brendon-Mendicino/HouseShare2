package lol.terabrendon.houseshare2.domain.mapper

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.brendonmendicino.aformvalidator.annotation.ValidationError
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import java.time.LocalDateTime
import javax.inject.Inject

class ShoppingItemFormMapper @Inject constructor() {
    fun map(state: ShoppingItemFormState): Result<ShoppingItemModel, ValidationError> {
        return Ok(
            ShoppingItemModel(
                id = 0,
                name = state.name,
                amount = state.amount ?: return Err(ValidationError.NotNull),
                price = state.price,
                creationTimestamp = LocalDateTime.now(),
                selected = false,
            )
        )
    }
}