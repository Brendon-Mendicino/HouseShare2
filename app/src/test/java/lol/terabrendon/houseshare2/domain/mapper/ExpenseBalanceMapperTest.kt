package lol.terabrendon.houseshare2.domain.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import lol.terabrendon.houseshare2.domain.model.BillingBalanceModel
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExpenseBalanceMapperTest {
    private val mapper = ExpenseBalanceMapper()

    @ParameterizedTest
    @MethodSource("expenseList")
    fun `fatti i cazzi toi`(input: List<ExpenseModel>, output: List<BillingBalanceModel>) {
        val result = mapper.map(input)

        assertThat(result).isEqualTo(output)
    }

    @Test
    fun sanity() {
        assertThat(true).isTrue()
    }

    companion object {
        @JvmStatic
        private fun expenseList(): Stream<Arguments> = Stream.of(
            emptyLists()
        )

        private fun emptyLists() =
            Arguments.of(emptyList<ExpenseModel>(), emptyList<BillingBalanceModel>())
    }
}