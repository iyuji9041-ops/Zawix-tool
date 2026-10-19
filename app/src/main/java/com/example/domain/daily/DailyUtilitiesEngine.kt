package com.example.domain.daily

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

enum class BmiCategory(val label: String) {
    UNDERWEIGHT("Underweight"),
    NORMAL("Normal Weight"),
    OVERWEIGHT("Overweight"),
    OBESE("Obese")
}

data class BmiResult(
    val bmiValue: BigDecimal,
    val category: BmiCategory,
    val healthyWeightMinKg: BigDecimal,
    val healthyWeightMaxKg: BigDecimal
)

data class TipResult(
    val tipAmount: BigDecimal,
    val totalBill: BigDecimal,
    val perPersonTip: BigDecimal,
    val perPersonTotal: BigDecimal,
    val leftoverCents: Int = 0,
    val perPersonAmounts: List<BigDecimal> = emptyList()
)

object DailyUtilitiesEngine {
    private val mc = MathContext(4, RoundingMode.HALF_UP)

    fun calculateBmiMetric(heightCm: BigDecimal, weightKg: BigDecimal): BmiResult {
        require(heightCm > BigDecimal.ZERO) { "Height must be positive" }
        require(weightKg > BigDecimal.ZERO) { "Weight must be positive" }

        val heightM = heightCm.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)
        val heightSquared = heightM.multiply(heightM, mc)
        val bmi = weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP)

        val minHealthyKg = BigDecimal("18.5").multiply(heightSquared, mc).setScale(1, RoundingMode.HALF_UP)
        val maxHealthyKg = BigDecimal("24.9").multiply(heightSquared, mc).setScale(1, RoundingMode.HALF_UP)

        val category = when {
            bmi < BigDecimal("18.5") -> BmiCategory.UNDERWEIGHT
            bmi < BigDecimal("25.0") -> BmiCategory.NORMAL
            bmi < BigDecimal("30.0") -> BmiCategory.OVERWEIGHT
            else -> BmiCategory.OBESE
        }

        return BmiResult(bmi, category, minHealthyKg, maxHealthyKg)
    }

    fun calculateBmiImperial(feet: BigDecimal, inches: BigDecimal, weightLbs: BigDecimal): BmiResult {
        val totalInches = feet.multiply(BigDecimal("12")).add(inches)
        val heightCm = totalInches.multiply(BigDecimal("2.54"))
        val weightKg = weightLbs.multiply(BigDecimal("0.45359237"))
        return calculateBmiMetric(heightCm, weightKg)
    }

    fun calculateTip(
        billAmount: BigDecimal,
        tipPercentage: BigDecimal,
        splitCount: Int
    ): TipResult {
        require(billAmount >= BigDecimal.ZERO) { "Bill amount cannot be negative" }
        require(splitCount >= 1) { "Split count must be at least 1" }

        val tipFraction = tipPercentage.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP)
        val tip = billAmount.multiply(tipFraction).setScale(2, RoundingMode.HALF_UP)
        val total = billAmount.add(tip).setScale(2, RoundingMode.HALF_UP)

        // Convert to cents to distribute remainder cents fairly without losing fractions
        val totalCents = total.multiply(BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toLong()
        val baseCentsPerPerson = totalCents / splitCount
        val remainderCents = (totalCents % splitCount).toInt()

        val perPersonList = (0 until splitCount).map { i ->
            val cents = if (i < remainderCents) baseCentsPerPerson + 1 else baseCentsPerPerson
            BigDecimal(cents).divide(BigDecimal("100"), 2, RoundingMode.UNNECESSARY)
        }

        val basePerPerson = BigDecimal(baseCentsPerPerson).divide(BigDecimal("100"), 2, RoundingMode.UNNECESSARY)
        val perPersonTip = tip.divide(BigDecimal(splitCount), 2, RoundingMode.HALF_UP)

        return TipResult(
            tipAmount = tip,
            totalBill = total,
            perPersonTip = perPersonTip,
            perPersonTotal = basePerPerson,
            leftoverCents = remainderCents,
            perPersonAmounts = perPersonList
        )
    }
}
