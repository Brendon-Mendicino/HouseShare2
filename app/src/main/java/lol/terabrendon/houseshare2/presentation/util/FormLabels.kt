package lol.terabrendon.houseshare2.presentation.util

import io.github.brendonmendicino.aformvalidator.annotation.error.ValidationError
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.form.ExpenseFormStateValidator
import lol.terabrendon.houseshare2.domain.form.GroupFormStateValidator
import lol.terabrendon.houseshare2.domain.form.GroupMemberFormStateValidator
import lol.terabrendon.houseshare2.domain.form.LoginFormStateValidator
import lol.terabrendon.houseshare2.domain.form.ShoppingItemFormStateValidator
import lol.terabrendon.houseshare2.domain.form.UserPartValidator

/**
 * The generated validators key their [errors] by the Kotlin property name, which is not something
 * a user should ever read. Every form declares here the label of each of its validated fields, by
 * property reference, so that renaming a field breaks the build instead of silently leaking the
 * property name into a snackbar.
 *
 * Fields without any validation annotation never show up in `errors`, so they need no label.
 */
fun GroupMemberFormStateValidator.labels(): Map<String, Int> = mapOf(
    GroupMemberFormStateValidator::firstName.name to R.string.first_name,
    GroupMemberFormStateValidator::lastName.name to R.string.last_name,
    GroupMemberFormStateValidator::picture.name to R.string.image_url,
)

fun GroupFormStateValidator.labels(): Map<String, Int> = mapOf(
    GroupFormStateValidator::name.name to R.string.group_name,
    GroupFormStateValidator::description.name to R.string.group_description,
    GroupFormStateValidator::users.name to R.string.members,
    GroupFormStateValidator::imageUrl.name to R.string.image_url,
)

fun ShoppingItemFormStateValidator.labels(): Map<String, Int> = mapOf(
    ShoppingItemFormStateValidator::name.name to R.string.name,
    ShoppingItemFormStateValidator::amountStr.name to R.string.amount,
    ShoppingItemFormStateValidator::priceStr.name to R.string.price,
    ShoppingItemFormStateValidator::amount.name to R.string.amount,
    ShoppingItemFormStateValidator::price.name to R.string.price,
)

fun ExpenseFormStateValidator.labels(): Map<String, Int> = mapOf(
    ExpenseFormStateValidator::totalAmount.name to R.string.amount,
    ExpenseFormStateValidator::totalAmountMoney.name to R.string.amount,
    ExpenseFormStateValidator::title.name to R.string.title,
    ExpenseFormStateValidator::description.name to R.string.description,
    ExpenseFormStateValidator::category.name to R.string.category,
    ExpenseFormStateValidator::payer.name to R.string.payed_by,
)

fun LoginFormStateValidator.labels(): Map<String, Int> = mapOf(
    LoginFormStateValidator::username.name to R.string.username,
    LoginFormStateValidator::password.name to R.string.password,
)

fun UserPartValidator.labels(): Map<String, Int> = mapOf(
    UserPartValidator::amount.name to R.string.amount,
    UserPartValidator::amountDouble.name to R.string.amount,
)

/**
 * The message of the first error of the form, or `null` when the form is valid.
 */
fun GroupMemberFormStateValidator.errorUiText(): UiText? = errors.firstUiText(labels())

fun GroupFormStateValidator.errorUiText(): UiText? = errors.firstUiText(labels())

fun ShoppingItemFormStateValidator.errorUiText(): UiText? = errors.firstUiText(labels())

fun UserPartValidator.errorUiText(): UiText? = errors.firstUiText(labels())

fun LoginFormStateValidator.errorUiText(): UiText? = errors.firstUiText(labels())

fun ExpenseFormStateValidator.errorUiText(): UiText? {
    val (name, error) = errors.firstOrNull() ?: return null

    // partsEqualTotal is an invariant over the whole form, it has no field of its own, so it gets
    // its own message instead of the generic "<label> is not valid".
    if (name == ExpenseFormStateValidator::partsEqualTotal.name) {
        return UiText.Res(R.string.the_sum_of_the_money_should_equal_the_total_amount)
    }

    return error.toUiText(labels()[name])
}

private fun Sequence<Pair<String, ValidationError<*>>>.firstUiText(
    labels: Map<String, Int>,
): UiText? = firstOrNull()?.let { (name, error) -> error.toUiText(labels[name]) }
