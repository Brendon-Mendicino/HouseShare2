package lol.terabrendon.houseshare2.domain.form

// TODO: remove this when we will have the function to update every `used` in the validator from the library

/**
 * A field shows its error only once it has been used, which normally happens when the user edits
 * it. On submit every field is marked as used, so that the fields the user never touched are
 * highlighted as well. [io.github.brendonmendicino.aformvalidator.core.ParamState.update] keeps the
 * current value when none is passed.
 *
 * Only the properties of the constructor can be touched, the derived ones inherit `used` from the
 * fields they depend on.
 */
fun GroupMemberFormStateValidator.touchAll() = copy(
    firstName = firstName.update(),
    lastName = lastName.update(),
    picture = picture.update(),
)

fun GroupFormStateValidator.touchAll() = copy(
    name = name.update(),
    description = description.update(),
    users = users.update(),
    imageUrl = imageUrl.update(),
)

fun ShoppingItemFormStateValidator.touchAll() = copy(
    name = name.update(),
    amountStr = amountStr.update(),
    priceStr = priceStr.update(),
)

fun ExpenseFormStateValidator.touchAll() = copy(
    totalAmount = totalAmount.update(),
    description = description.update(),
    title = title.update(),
    category = category.update(),
    payer = payer.update(),
    userParts = userParts.update(),
)

