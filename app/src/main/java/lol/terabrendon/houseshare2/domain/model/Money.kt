@file:Suppress("unused")

package lol.terabrendon.houseshare2.domain.model

import android.icu.text.NumberFormat
import androidx.compose.runtime.Immutable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@Immutable
class Money internal constructor(
    private val euro: BigDecimal,
) : Number(), Comparable<Money> {
    companion object {
        val ZERO = Money(0.toBigDecimal().setScale(2, RoundingMode.FLOOR))

        fun fromCompact(compact: Long): Money =
            Money((compact.toBigDecimal().setScale(2) / 100.toBigDecimal()).setScale(2))

        fun fromCompact(compact: Int): Money = fromCompact(compact.toLong())
    }

    val compact: Long
        get() = (euro * 100.toBigDecimal()).toLong()

    override fun toDouble(): Double = euro.toDouble()
    override fun toFloat(): Float = euro.toFloat()
    override fun toLong(): Long = euro.toLong()
    override fun toInt(): Int = euro.toInt()
    override fun toShort(): Short = euro.toShort()
    override fun toByte(): Byte = euro.toByte()

    override fun compareTo(other: Money): Int = euro.compareTo(other.euro)
    operator fun compareTo(other: Double): Int = compareTo(other.toMoney())
    operator fun compareTo(other: Float): Int = compareTo(other.toMoney())
    operator fun compareTo(other: Long): Int = compareTo(other.toMoney())
    operator fun compareTo(other: Int): Int = compareTo(other.toMoney())

    operator fun plus(other: Money): Money = Money(euro + other.euro)
    operator fun plus(other: Double): Money = plus(other.toMoney())
    operator fun plus(other: Float): Money = plus(other.toMoney())
    operator fun plus(other: Long): Money = plus(other.toMoney())
    operator fun plus(other: Int): Money = plus(other.toMoney())

    operator fun minus(other: Money): Money = Money(euro - other.euro)
    operator fun minus(other: Double): Money = minus(other.toMoney())
    operator fun minus(other: Float): Money = minus(other.toMoney())
    operator fun minus(other: Long): Money = minus(other.toMoney())
    operator fun minus(other: Int): Money = minus(other.toMoney())

    operator fun times(other: Money): Money = Money(euro * other.euro)
    operator fun times(other: Double): Money = times(other.toMoney())
    operator fun times(other: Float): Money = times(other.toMoney())
    operator fun times(other: Long): Money = times(other.toMoney())
    operator fun times(other: Int): Money = times(other.toMoney())

    operator fun div(other: Money): Money = Money(euro / other.euro)
    operator fun div(other: Double): Money = div(other.toMoney())
    operator fun div(other: Float): Money = div(other.toMoney())
    operator fun div(other: Long): Money = div(other.toMoney())
    operator fun div(other: Int): Money = div(other.toMoney())

    operator fun rem(other: Money): Money = Money(euro % other.euro)
    operator fun rem(other: Double): Money = rem(other.toMoney())
    operator fun rem(other: Float): Money = rem(other.toMoney())
    operator fun rem(other: Long): Money = rem(other.toMoney())
    operator fun rem(other: Int): Money = rem(other.toMoney())

    fun divRem(other: Money): Pair<Money, Money> =
        euro.divideAndRemainder(other.euro).let {
            Money(it[0].setScale(2, RoundingMode.FLOOR)) to Money(
                it[1].setScale(
                    2,
                    RoundingMode.FLOOR
                )
            )
        }

    fun divRem(other: Double): Pair<Money, Money> = divRem(other.toMoney())
    fun divRem(other: Float): Pair<Money, Money> = divRem(other.toMoney())
    fun divRem(other: Long): Pair<Money, Money> = divRem(other.toMoney())
    fun divRem(other: Int): Pair<Money, Money> = divRem(other.toMoney())

    operator fun unaryPlus() = Money(euro)
    operator fun unaryMinus() = Money(euro.unaryMinus())


    override fun toString(): String {
        val whole = euro.toLong().toString()
        val decimal = ((euro % 1.toBigDecimal()) * 100.toBigDecimal()).toLong().toString()

        return if (decimal.length == 1) "$whole.0$decimal"
        else "$whole.$decimal"
    }

    override fun equals(other: Any?): Boolean {
        if (other === null) return false
        if (other !is Money) return false
        return euro == other.euro
    }

    override fun hashCode(): Int {
        return euro.hashCode()
    }

    fun toCurrency(locale: Locale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(euro)
    }

    fun toCurrency() = toCurrency(Locale.getDefault())
}

fun BigDecimal.toMoney() = Money(this.setScale(2, RoundingMode.FLOOR))
fun Double.toMoney() = Money(toBigDecimal().setScale(2, RoundingMode.FLOOR))
fun Float.toMoney() = Money(toBigDecimal().setScale(2, RoundingMode.FLOOR))
fun Long.toMoney() = Money(toBigDecimal().setScale(2, RoundingMode.FLOOR))
fun Int.toMoney() = Money(toBigDecimal().setScale(2, RoundingMode.FLOOR))


fun String.toMoneyOrNull(): Money? {
    val b = toBigDecimalOrNull() ?: return null
    return Money(b.setScale(2, RoundingMode.FLOOR))
}

fun String.toMoney() =
    toMoneyOrNull() ?: throw NumberFormatException("Cannot parse string \"$this\" as Money!")

val String.money: Money get() = toMoney()
