package lol.terabrendon.houseshare2.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class MoneyTest {
    companion object {
        lateinit var locale: Locale

        @BeforeClass
        @JvmStatic
        fun before() {
            locale = Locale.getDefault()
        }

        @AfterClass
        @JvmStatic
        fun after() {
            Locale.setDefault(locale)
        }
    }

    @Test
    fun `Money prints to the correct string`() {
        val money = "1234.56".toMoney()
        assertThat(money.toString()).isEqualTo("1234.56")
    }

    @Test
    fun `Trailing zeros are shown`() {
        val money = "1234.00".toMoney()
        assertThat(money.toString()).isEqualTo("1234.00")
    }

    @Test
    fun `Decimal part over the 1e-2 are truncated`() {
        val str = "1234.5678".toMoney()
        val big = BigDecimal("1234.5678").toMoney()
        val double = 1234.5678.toMoney()
        val float = 1234.5677f.toMoney()

        assertThat(str.toString()).isEqualTo("1234.56")
        assertThat(big.toString()).isEqualTo("1234.56")
        assertThat(double.toString()).isEqualTo("1234.56")
        assertThat(float.toString()).isEqualTo("1234.56")
    }

    @Test
    fun `Integers cannot have decimals`() {
        val long = 10L.toMoney()
        val int = 10.toMoney()

        assertThat(long).isEqualTo("10".toMoney())
        assertThat(int).isEqualTo("10".toMoney())
    }

    @Test
    fun `Reminder and division result`() {
        val money = 10.toMoney()
        val (div, rem) = money.divRem(3.toMoney())
        assertThat(div).isEqualTo("3".toMoney())
        assertThat(rem).isEqualTo("1".toMoney())
    }

    @Test
    fun `Compact should be ok`() {
        val zero = "0".toMoney().compact
        val comp = "123.45".toMoney().compact
        val noTrailing = "123.00".toMoney().compact
        val onlyCents = "0.05".toMoney().compact

        assertThat(zero).isEqualTo(0)
        assertThat(comp).isEqualTo(12345)
        assertThat(noTrailing).isEqualTo(12300)
        assertThat(onlyCents).isEqualTo(5)
    }

    @Test
    fun `From compact should be ok`() {
        val params = listOf(0 to "0", 12345 to "123.45", 12300 to "123.00", 5 to "0.05")

        for ((int, expected) in params) {
            val m = Money.fromCompact(int)

            assertThat(m).isEqualTo(expected.toMoney())
        }
    }
}