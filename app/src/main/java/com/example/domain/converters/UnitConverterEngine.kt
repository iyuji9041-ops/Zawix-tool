package com.example.domain.converters

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume"),
    DATA_STORAGE("Data Storage")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    // Factor relative to base unit of category
    val toBaseFactor: BigDecimal = BigDecimal.ONE
)

object UnitConverterEngine {
    private val mc = MathContext(10, RoundingMode.HALF_UP)

    val unitsByCategory: Map<UnitCategory, List<ConversionUnit>> = mapOf(
        UnitCategory.LENGTH to listOf(
            ConversionUnit("m", "Meter", "m", UnitCategory.LENGTH, BigDecimal("1")),
            ConversionUnit("km", "Kilometer", "km", UnitCategory.LENGTH, BigDecimal("1000")),
            ConversionUnit("cm", "Centimeter", "cm", UnitCategory.LENGTH, BigDecimal("0.01")),
            ConversionUnit("mm", "Millimeter", "mm", UnitCategory.LENGTH, BigDecimal("0.001")),
            ConversionUnit("mi", "Mile", "mi", UnitCategory.LENGTH, BigDecimal("1609.344")),
            ConversionUnit("yd", "Yard", "yd", UnitCategory.LENGTH, BigDecimal("0.9144")),
            ConversionUnit("ft", "Foot", "ft", UnitCategory.LENGTH, BigDecimal("0.3048")),
            ConversionUnit("in", "Inch", "in", UnitCategory.LENGTH, BigDecimal("0.0254"))
        ),
        UnitCategory.WEIGHT to listOf(
            ConversionUnit("kg", "Kilogram", "kg", UnitCategory.WEIGHT, BigDecimal("1")),
            ConversionUnit("g", "Gram", "g", UnitCategory.WEIGHT, BigDecimal("0.001")),
            ConversionUnit("mg", "Milligram", "mg", UnitCategory.WEIGHT, BigDecimal("0.000001")),
            ConversionUnit("lb", "Pound", "lb", UnitCategory.WEIGHT, BigDecimal("0.45359237")),
            ConversionUnit("oz", "Ounce", "oz", UnitCategory.WEIGHT, BigDecimal("0.028349523125")),
            ConversionUnit("t", "Metric Ton", "t", UnitCategory.WEIGHT, BigDecimal("1000"))
        ),
        UnitCategory.TEMPERATURE to listOf(
            ConversionUnit("c", "Celsius", "°C", UnitCategory.TEMPERATURE),
            ConversionUnit("f", "Fahrenheit", "°F", UnitCategory.TEMPERATURE),
            ConversionUnit("k", "Kelvin", "K", UnitCategory.TEMPERATURE)
        ),
        UnitCategory.AREA to listOf(
            ConversionUnit("sqm", "Square Meter", "m²", UnitCategory.AREA, BigDecimal("1")),
            ConversionUnit("sqkm", "Square Kilometer", "km²", UnitCategory.AREA, BigDecimal("1000000")),
            ConversionUnit("sqft", "Square Foot", "ft²", UnitCategory.AREA, BigDecimal("0.09290304")),
            ConversionUnit("sqmi", "Square Mile", "mi²", UnitCategory.AREA, BigDecimal("2589988.110336")),
            ConversionUnit("ac", "Acre", "ac", UnitCategory.AREA, BigDecimal("4046.8564224")),
            ConversionUnit("ha", "Hectare", "ha", UnitCategory.AREA, BigDecimal("10000"))
        ),
        UnitCategory.VOLUME to listOf(
            ConversionUnit("l", "Liter", "L", UnitCategory.VOLUME, BigDecimal("1")),
            ConversionUnit("ml", "Milliliter", "mL", UnitCategory.VOLUME, BigDecimal("0.001")),
            ConversionUnit("cum", "Cubic Meter", "m³", UnitCategory.VOLUME, BigDecimal("1000")),
            ConversionUnit("gal", "Gallon (US)", "gal", UnitCategory.VOLUME, BigDecimal("3.785411784")),
            ConversionUnit("qt", "Quart (US)", "qt", UnitCategory.VOLUME, BigDecimal("0.946352946")),
            ConversionUnit("pt", "Pint (US)", "pt", UnitCategory.VOLUME, BigDecimal("0.473176473")),
            ConversionUnit("floz", "Fluid Ounce (US)", "fl oz", UnitCategory.VOLUME, BigDecimal("0.0295735295625"))
        ),
        UnitCategory.DATA_STORAGE to listOf(
            ConversionUnit("b", "Byte", "B", UnitCategory.DATA_STORAGE, BigDecimal("1")),
            ConversionUnit("bit", "Bit", "bit", UnitCategory.DATA_STORAGE, BigDecimal("0.125")),
            // Decimal storage units (SI standard: powers of 10)
            ConversionUnit("kb", "Kilobyte (KB)", "KB", UnitCategory.DATA_STORAGE, BigDecimal("1000")),
            ConversionUnit("mb", "Megabyte (MB)", "MB", UnitCategory.DATA_STORAGE, BigDecimal("1000000")),
            ConversionUnit("gb", "Gigabyte (GB)", "GB", UnitCategory.DATA_STORAGE, BigDecimal("1000000000")),
            ConversionUnit("tb", "Terabyte (TB)", "TB", UnitCategory.DATA_STORAGE, BigDecimal("1000000000000")),
            // Binary storage units (IEC standard: powers of 2)
            ConversionUnit("kib", "Kibibyte (KiB)", "KiB", UnitCategory.DATA_STORAGE, BigDecimal("1024")),
            ConversionUnit("mib", "Mebibyte (MiB)", "MiB", UnitCategory.DATA_STORAGE, BigDecimal("1048576")),
            ConversionUnit("gib", "Gibibyte (GiB)", "GiB", UnitCategory.DATA_STORAGE, BigDecimal("1073741824")),
            ConversionUnit("tib", "Tebibyte (TiB)", "TiB", UnitCategory.DATA_STORAGE, BigDecimal("1099511627776")),
            // Megabit = 1,000,000 bits = 125,000 Bytes
            ConversionUnit("mbit", "Megabit (Mb)", "Mb", UnitCategory.DATA_STORAGE, BigDecimal("125000"))
        )
    )

    fun convert(value: BigDecimal, fromUnit: ConversionUnit, toUnit: ConversionUnit): BigDecimal {
        if (fromUnit == toUnit) return value

        if (fromUnit.category == UnitCategory.TEMPERATURE) {
            return convertTemperature(value, fromUnit.id, toUnit.id)
        }

        // Value in base unit = value * from.toBaseFactor
        val baseValue = value.multiply(fromUnit.toBaseFactor, mc)
        // Target value = baseValue / to.toBaseFactor
        val result = baseValue.divide(toUnit.toBaseFactor, mc)
        val stripped = result.stripTrailingZeros()
        return if (stripped.scale() < 0) stripped.setScale(0) else stripped
    }

    private fun convertTemperature(value: BigDecimal, fromId: String, toId: String): BigDecimal {
        // Convert to Celsius first
        val celsius = when (fromId) {
            "c" -> value
            "f" -> value.subtract(BigDecimal("32")).multiply(BigDecimal("5")).divide(BigDecimal("9"), mc)
            "k" -> value.subtract(BigDecimal("273.15"))
            else -> value
        }

        // Convert Celsius to target
        val result = when (toId) {
            "c" -> celsius
            "f" -> celsius.multiply(BigDecimal("9")).divide(BigDecimal("5"), mc).add(BigDecimal("32"))
            "k" -> celsius.add(BigDecimal("273.15"))
            else -> celsius
        }
        val stripped = result.stripTrailingZeros()
        return if (stripped.scale() < 0) stripped.setScale(0) else stripped
    }
}
