package cn.campus.ledger.data

import java.math.BigDecimal

object Money {

    private val AMOUNT = Regex("[0-9]{1,8}(\\.[0-9]{1,2})?")

    fun parse(raw: String): Long {
        require(AMOUNT.matches(raw)) { "invalid amount" }
        val value = BigDecimal(raw).movePointRight(2).longValueExact()
        require(value > 0L) { "amount must be positive" }
        return value
    }

    fun format(cents: Long): String = BigDecimal.valueOf(cents, 2).toPlainString()
}
