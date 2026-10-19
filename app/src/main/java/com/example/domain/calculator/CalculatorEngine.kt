package com.example.domain.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.ArrayDeque

object CalculatorEngine {
    private val mathContext = MathContext(12, RoundingMode.HALF_UP)
    private const val OP_UNARY_MINUS = "NEG"
    private const val OP_PERCENT = "%"

    fun evaluate(expression: String): Result<BigDecimal> {
        return try {
            val sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .trim()

            if (sanitized.isEmpty()) {
                return Result.failure(IllegalArgumentException("Expression is empty"))
            }

            val tokens = tokenize(sanitized)
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn)
            val stripped = result.stripTrailingZeros()
            val normalized = if (stripped.scale() < 0) stripped.setScale(0) else stripped
            Result.success(normalized)
        } catch (e: ArithmeticException) {
            Result.failure(IllegalArgumentException("Division by zero"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException(e.message ?: "Invalid expression"))
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        var expectUnary = true

        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++

                c in "0123456789." -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    val tokenStr = sb.toString()
                    // Reject malformed numbers like "1.2.3" or single "."
                    if (tokenStr.count { it == '.' } > 1 || tokenStr == ".") {
                        throw IllegalArgumentException("Invalid number")
                    }
                    tokens.add(tokenStr)
                    expectUnary = false
                }

                c == '-' && expectUnary -> {
                    // Dedicated unary negation operator with high precedence
                    tokens.add(OP_UNARY_MINUS)
                    i++
                    expectUnary = true
                }

                c == '%' -> {
                    // Postfix percent operator (e.g. 50% = 0.5)
                    if (expectUnary) {
                        throw IllegalArgumentException("Unexpected % operator")
                    }
                    tokens.add(OP_PERCENT)
                    i++
                    expectUnary = false
                }

                c in "+-*/" -> {
                    if (expectUnary && c == '+') {
                        // Unary plus is a no-op, ignore
                        i++
                    } else {
                        tokens.add(c.toString())
                        i++
                        expectUnary = true
                    }
                }

                c == '(' -> {
                    tokens.add("(")
                    i++
                    expectUnary = true
                }

                c == ')' -> {
                    tokens.add(")")
                    i++
                    expectUnary = false
                }

                else -> throw IllegalArgumentException("Unexpected character: '$c'")
            }
        }
        return tokens
    }

    private fun precedence(op: String): Int = when (op) {
        "+", "-" -> 1
        "*", "/" -> 2
        OP_UNARY_MINUS -> 3 // Higher than multiplication and division
        OP_PERCENT -> 4     // Postfix percentage has highest operator precedence
        else -> 0
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            val isNumber = token.toBigDecimalOrNull() != null
            if (isNumber) {
                output.add(token)
            } else if (token == OP_PERCENT) {
                // Postfix percentage applies immediately to preceding operand
                output.add(token)
            } else if (token == OP_UNARY_MINUS) {
                // Prefix unary operator
                stack.push(token)
            } else if (token in listOf("+", "-", "*", "/")) {
                while (stack.isNotEmpty() && stack.peek() != "(" && precedence(stack.peek()!!) >= precedence(token)) {
                    output.add(stack.pop())
                }
                stack.push(token)
            } else if (token == "(") {
                stack.push(token)
            } else if (token == ")") {
                while (stack.isNotEmpty() && stack.peek() != "(") {
                    output.add(stack.pop())
                }
                if (stack.isEmpty() || stack.peek() != "(") {
                    throw IllegalArgumentException("Mismatched parentheses")
                }
                stack.pop() // discard '('
                // If there's a unary minus immediately preceding the parenthesis, pop it
                if (stack.isNotEmpty() && stack.peek() == OP_UNARY_MINUS) {
                    output.add(stack.pop())
                }
            }
        }
        while (stack.isNotEmpty()) {
            val top = stack.pop()
            if (top == "(" || top == ")") {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }
        return output
    }

    private fun evaluateRpn(rpn: List<String>): BigDecimal {
        val stack = ArrayDeque<BigDecimal>()
        for (token in rpn) {
            val num = token.toBigDecimalOrNull()
            if (num != null) {
                stack.push(num)
            } else if (token == OP_UNARY_MINUS) {
                if (stack.isEmpty()) throw IllegalArgumentException("Malformed expression")
                val a = stack.pop()
                stack.push(a.negate(mathContext))
            } else if (token == OP_PERCENT) {
                if (stack.isEmpty()) throw IllegalArgumentException("Malformed expression")
                val a = stack.pop()
                stack.push(a.divide(BigDecimal("100"), mathContext))
            } else {
                if (stack.size < 2) throw IllegalArgumentException("Malformed expression")
                val b = stack.pop()
                val a = stack.pop()
                val res = when (token) {
                    "+" -> a.add(b, mathContext)
                    "-" -> a.subtract(b, mathContext)
                    "*" -> a.multiply(b, mathContext)
                    "/" -> {
                        if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                        a.divide(b, mathContext)
                    }
                    else -> throw IllegalArgumentException("Unknown operator: $token")
                }
                stack.push(res)
            }
        }
        if (stack.size != 1) throw IllegalArgumentException("Malformed expression")
        return stack.pop()
    }
}
