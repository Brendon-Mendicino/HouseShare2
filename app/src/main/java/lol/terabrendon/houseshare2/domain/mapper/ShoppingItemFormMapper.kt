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
    fun map(
        state: ShoppingItemFormState,
        ownerId: Long,
        groupId: Long,
    ): Result<ShoppingItemModel, ValidationError> {
        return Ok(
            ShoppingItemModel(
                id = 0,
                ownerId = ownerId,
                groupId = groupId,
                name = state.name,
                amount = state.amount ?: return Err(ValidationError.NotBlank),
                price = state.price,
                creationTimestamp = LocalDateTime.now(),
                selected = false,
            )
        )
    }
}