package com.example.domain.password

import java.security.SecureRandom
import kotlin.math.log2

enum class PasswordStrength(val label: String, val score: Int) {
    VERY_WEAK("Very Weak", 0),
    WEAK("Weak", 1),
    FAIR("Fair", 2),
    STRONG("Strong", 3),
    VERY_STRONG("Very Strong", 4)
}

data class PasswordStrengthResult(
    val strength: PasswordStrength,
    val entropyBits: Double,
    val suggestions: List<String>
)

object PasswordGeneratorEngine {
    private val secureRandom = SecureRandom()

    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?"

    private val AMBIGUOUS_CHARS = setOf('1', 'l', 'I', '|', '0', 'O', 'o', '8', 'B')

    fun generate(
        length: Int,
        includeUpper: Boolean,
        includeLower: Boolean,
        includeDigits: Boolean,
        includeSymbols: Boolean,
        excludeAmbiguous: Boolean
    ): String {
        require(length in 4..64) { "Password length must be between 4 and 64" }

        val charPools = mutableListOf<String>()
        val requiredChars = mutableListOf<Char>()

        fun filterAmbiguous(source: String): String {
            return if (excludeAmbiguous) {
                source.filter { it !in AMBIGUOUS_CHARS }
            } else source
        }

        if (includeUpper) {
            val pool = filterAmbiguous(UPPER)
            if (pool.isNotEmpty()) {
                charPools.add(pool)
                requiredChars.add(pool[secureRandom.nextInt(pool.length)])
            }
        }
        if (includeLower) {
            val pool = filterAmbiguous(LOWER)
            if (pool.isNotEmpty()) {
                charPools.add(pool)
                requiredChars.add(pool[secureRandom.nextInt(pool.length)])
            }
        }
        if (includeDigits) {
            val pool = filterAmbiguous(DIGITS)
            if (pool.isNotEmpty()) {
                charPools.add(pool)
                requiredChars.add(pool[secureRandom.nextInt(pool.length)])
            }
        }
        if (includeSymbols) {
            val pool = filterAmbiguous(SYMBOLS)
            if (pool.isNotEmpty()) {
                charPools.add(pool)
                requiredChars.add(pool[secureRandom.nextInt(pool.length)])
            }
        }

        if (charPools.isEmpty()) {
            throw IllegalArgumentException("At least one character type must be selected")
        }

        val allChars = charPools.joinToString("")
        val passwordChars = ArrayList<Char>(length)
        passwordChars.addAll(requiredChars)

        while (passwordChars.size < length) {
            val idx = secureRandom.nextInt(allChars.length)
            passwordChars.add(allChars[idx])
        }

        // Shuffle using Fisher-Yates with SecureRandom to avoid bias
        for (i in passwordChars.indices.reversed()) {
            val j = secureRandom.nextInt(i + 1)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = temp
        }

        return passwordChars.joinToString("")
    }

    fun assessStrength(password: String): PasswordStrengthResult {
        if (password.isEmpty()) {
            return PasswordStrengthResult(PasswordStrength.VERY_WEAK, 0.0, listOf("Enter or generate a password"))
        }

        var poolSize = 0
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        if (hasUpper) poolSize += 26
        if (hasLower) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSymbol) poolSize += 32

        val entropy = if (poolSize > 0) password.length * log2(poolSize.toDouble()) else 0.0

        val suggestions = mutableListOf<String>()
        if (password.length < 12) suggestions.add("Use at least 12 characters")
        if (!hasUpper) suggestions.add("Add uppercase letters")
        if (!hasLower) suggestions.add("Add lowercase letters")
        if (!hasDigit) suggestions.add("Add numbers")
        if (!hasSymbol) suggestions.add("Add special symbols")

        val strength = when {
            entropy < 28 || password.length < 6 -> PasswordStrength.VERY_WEAK
            entropy < 40 || password.length < 8 -> PasswordStrength.WEAK
            entropy < 60 || password.length < 12 -> PasswordStrength.FAIR
            entropy < 85 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }

        return PasswordStrengthResult(strength, entropy, suggestions)
    }
}
