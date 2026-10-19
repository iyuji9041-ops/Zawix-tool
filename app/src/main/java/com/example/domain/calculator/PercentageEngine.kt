package com.example.domain.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object PercentageEngine {
    private val mc = MathContext(8, RoundingMode.HALF_UP)
    private val HUNDRED = BigDecimal("100")

    private fun normalize(bd: BigDecimal): BigDecimal {
        val stripped = bd.stripTrailingZeros()
        return if (stripped.scale() < 0) stripped.setScale(0) else stripped
    }

    /**
     * Calculates what is X% of Y.
     * Formula: (X / 100) * Y
     */
    fun percentageOf(percent: BigDecimal, value: BigDecimal): BigDecimal {
        return normalize(percent.divide(HUNDRED, mc).multiply(value, mc))
    }

    /**
     * Calculates X is what % of Y.
     * Formula: (X / Y) * 100
     */
    fun whatPercentage(part: BigDecimal, total: BigDecimal): BigDecimal {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            throw ArithmeticException("Total cannot be zero")
        }
        return normalize(part.divide(total, mc).multiply(HUNDRED, mc))
    }

    /**
     * Calculates percentage change from initial to final value.
     * Formula: ((final - initial) / initial) * 100
     */
    fun percentageChange(initial: BigDecimal, finalVal: BigDecimal): BigDecimal {
        if (initial.compareTo(BigDecimal.ZERO) == 0) {
            throw ArithmeticException("Initial value cannot be zero")
        }
        return normalize(
            finalVal.subtract(initial, mc)
                .divide(initial, mc)
                .multiply(HUNDRED, mc)
        )
    }
}
