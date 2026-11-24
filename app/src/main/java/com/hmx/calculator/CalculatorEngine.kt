package com.hmx.calculator

import java.math.BigDecimal
import java.math.MathContext

/** Recursive-descent expression parser. Supports + - * / %, unary signs, decimals and parentheses. */
object CalculatorEngine {
    fun evaluate(expression: String): Double {
        val parser = Parser(expression)
        val result = parser.expression()
        parser.skipSpaces()
        require(parser.atEnd()) { "Invalid expression" }
        require(result.isFinite()) { "Result is not finite" }
        return if (result == 0.0) 0.0 else result
    }

    fun format(value: Double): String {
        require(value.isFinite())
        if (value == 0.0) return "0"
        val rounded = BigDecimal.valueOf(value).round(MathContext(12)).stripTrailingZeros()
        return rounded.toPlainString()
    }

    private class Parser(private val input: String) {
        private var index = 0
        fun atEnd() = index == input.length
        fun skipSpaces() { while (index < input.length && input[index].isWhitespace()) index++ }
        private fun take(c: Char): Boolean { skipSpaces(); if (index < input.length && input[index] == c) { index++; return true }; return false }

        fun expression(): Double {
            var value = term()
            while (true) value = when { take('+') -> value + term(); take('-') -> value - term(); else -> return value }
        }
        private fun term(): Double {
            var value = factor()
            while (true) {
                value = when {
                    take('*') -> value * factor()
                    take('/') -> { val divisor = factor(); require(divisor != 0.0) { "Division by zero" }; value / divisor }
                    else -> return value
                }
            }
        }
        private fun factor(): Double {
            val value = unary()
            return if (take('%')) value / 100.0 else value
        }
        private fun unary(): Double {
            if (take('+')) return unary()
            if (take('-')) return -unary()
            return primary()
        }
        private fun primary(): Double {
            if (take('(')) { val v = expression(); require(take(')')) { "Missing closing parenthesis" }; return v }
            skipSpaces()
            val start = index
            var dots = 0
            while (index < input.length && (input[index].isDigit() || input[index] == '.')) {
                if (input[index] == '.') dots++
                require(dots <= 1) { "Invalid number" }
                index++
            }
            require(index > start && input.substring(start, index).any(Char::isDigit)) { "Expected a number" }
            return input.substring(start, index).toDoubleOrNull() ?: error("Invalid number")
        }
    }
}
