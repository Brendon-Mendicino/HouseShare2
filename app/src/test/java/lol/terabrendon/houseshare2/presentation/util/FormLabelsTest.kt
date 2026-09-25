package lol.terabrendon.houseshare2.presentation.util

import com.google.common.truth.Truth.assertThat
import lol.terabrendon.houseshare2.domain.form.ExpenseFormState
import lol.terabrendon.houseshare2.domain.form.GroupFormState
import lol.terabrendon.houseshare2.domain.form.GroupMemberFormState
import lol.terabrendon.houseshare2.domain.form.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.form.UserPart
import lol.terabrendon.houseshare2.domain.form.toValidator
import org.junit.Test

/**
 * The messages of the form errors are built from a label, which is looked up by property name.
 * These tests make sure that no validated field is missing from its `labels()` map, otherwise its
 * error would be reported without naming the field.
 */
class FormLabelsTest {
    @Test
    fun `group member labels cover every error`() {
        val validator = GroupMemberFormState(
            firstName = "",
            lastName = "l".repeat(300),
            picture = "not an url",
        ).toValidator()

        assertThat(validator.errors.map { it.first }.toList()).isNotEmpty()
        assertThat(validator.labels().keys).containsAtLeastElementsIn(validator.errors.map { it.first }
            .toList())
    }

    @Test
    fun `group labels cover every error`() {
        val validator = GroupFormState(
            name = "",
            description = "",
            imageUrl = "not an url",
        ).toValidator()

        assertThat(validator.errors.map { it.first }.toList()).isNotEmpty()
        assertThat(validator.labels().keys).containsAtLeastElementsIn(validator.errors.map { it.first }
            .toList())
    }

    @Test
    fun `shopping item labels cover every error`() {
        val validator = ShoppingItemFormState(
            name = "",
            amountStr = "not a number",
            priceStr = "not a number",
        ).toValidator()

        assertThat(validator.errors.map { it.first }.toList()).isNotEmpty()
        assertThat(validator.labels().keys).containsAtLeastElementsIn(validator.errors.map { it.first }
            .toList())
    }

    @Test
    fun `expense labels cover every error`() {
        val validator = ExpenseFormState(
            totalAmount = "not a number",
            description = "",
            title = "",
            category = null,
            payer = null,
        ).toValidator()

        assertThat(validator.errors.map { it.first }.toList()).isNotEmpty()
        assertThat(validator.labels().keys).containsAtLeastElementsIn(validator.errors.map { it.first }
            .toList())
    }

    @Test
    fun `user part labels cover every error`() {
        val validator = UserPart(amount = "not a number").toValidator()

        assertThat(validator.errors.map { it.first }.toList()).isNotEmpty()
        assertThat(validator.labels().keys).containsAtLeastElementsIn(validator.errors.map { it.first }
            .toList())
    }

    @Test
    fun `a valid form has no message`() {
        val validator = GroupMemberFormState(
            firstName = "Brendon",
            lastName = null,
            picture = "https://example.com/image.jpg",
        ).toValidator()

        assertThat(validator.errorUiText()).isNull()
    }
}
