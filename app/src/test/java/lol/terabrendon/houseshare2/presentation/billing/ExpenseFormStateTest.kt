package lol.terabrendon.houseshare2.presentation.billing

import com.google.common.truth.Truth.assertThat
import lol.terabrendon.houseshare2.domain.model.sum
import lol.terabrendon.houseshare2.domain.model.toMoney
import org.junit.Test


class ExpenseFormStateTest {

    @Test
    fun `additive only sums match total`() {
        val state = ExpenseFormState(
            totalAmount = "100",
            userParts = listOf(
                UserPart(PaymentUnit.Additive, "30"),
                UserPart(PaymentUnit.Additive, "70")
            )
        )

        val converted = state.convertedValues
        assertThat(converted).containsExactly("30".toMoney(), "70".toMoney()).inOrder()
        assertThat(converted.sum()).isEqualTo("100".toMoney())
        assertThat(state.partsEqualTotal).isTrue()
    }

    @Test
    fun `percentage only sums match total`() {
        val state = ExpenseFormState(
            totalAmount = "200",
            userParts = listOf(
                UserPart(PaymentUnit.Percentage, "25"),
                UserPart(PaymentUnit.Percentage, "75")
            )
        )

        val converted = state.convertedValues
        assertThat(converted).containsExactly("50".toMoney(), "150".toMoney()).inOrder()
        assertThat(converted.sum()).isEqualTo("200".toMoney())
        assertThat(state.partsEqualTotal).isTrue()
    }

    @Test
    fun `quota only distributes remaining`() {
        val state = ExpenseFormState(
            totalAmount = "90",
            userParts = listOf(
                UserPart(PaymentUnit.Quota, "1"),
                UserPart(PaymentUnit.Quota, "2")
            )
        )

        val converted = state.convertedValues
        assertThat(converted[0]).isEqualTo("30".toMoney())
        assertThat(converted[1]).isEqualTo("60".toMoney())
        assertThat(converted.sum()).isEqualTo("90".toMoney())
        assertThat(state.partsEqualTotal).isTrue()
    }

    @Test
    fun `quota redistribution with small values`() {
        val state = ExpenseFormState(
            totalAmount = "6",
            userParts = listOf(
                UserPart(PaymentUnit.Additive, "1"),
                UserPart(PaymentUnit.Quota, "1"),
                UserPart(PaymentUnit.Quota, "2")
            )
        )

        val converted = state.convertedValues
        assertThat(converted[0]).isEqualTo("1".toMoney())
        assertThat(converted[1]).isEqualTo("1.67".toMoney())
        assertThat(converted[2]).isEqualTo("3.33".toMoney())
        assertThat(converted.sum()).isEqualTo("6".toMoney())
        assertThat(state.partsEqualTotal).isTrue()

    }

    @Test
    fun `mixed additive percentage quota correct distribution`() {
        val state = ExpenseFormState(
            totalAmount = "150",
            userParts = listOf(
                UserPart(PaymentUnit.Additive, "20"),
                UserPart(PaymentUnit.Percentage, "20"),
                UserPart(PaymentUnit.Quota, "1"),
                UserPart(PaymentUnit.Quota, "3")
            )
        )

        val converted = state.convertedValues
        assertThat(converted[0]).isEqualTo("20".toMoney())
        assertThat(converted[1]).isEqualTo("30".toMoney())
        assertThat(converted[2]).isEqualTo("25".toMoney())
        assertThat(converted[3]).isEqualTo("75".toMoney())
        assertThat(converted.sum()).isEqualTo("150".toMoney())
        assertThat(state.partsEqualTotal).isTrue()
    }

    @Test
    fun `leftovers are assigned and sum matches total`() {
        val state = ExpenseFormState(
            totalAmount = "101",
            userParts = listOf(
                UserPart(PaymentUnit.Percentage, "50"),
                UserPart(PaymentUnit.Percentage, "50")
            )
        )

        val converted = state.convertedValues
        assertThat(converted.sum()).isEqualTo(state.totalAmountMoney)
        assertThat(state.partsEqualTotal).isTrue()
    }
}