package com.hmx.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculatorEngineTest {
    private fun eq(expected: Double, expression: String) = assertEquals(expected, CalculatorEngine.evaluate(expression), 1e-9)
    @Test fun additionSubtraction() { eq(7.0, "10 - 2 + -1") }
    @Test fun multiplicationDivision() { eq(5.0, "20 / 2 * 0.5") }
    @Test fun percentage() { eq(25.0, "200 * 12.5%") }
    @Test fun decimals() { eq(3.75, "1.5 + 2.25") }
    @Test fun negatives() { eq(-6.0, "-2 * 3") }
    @Test fun parentheses() { eq(14.0, "2 * (3 + 4)") }
    @Test fun divisionByZero() { assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("4 / 0") } }
    @Test fun invalid() { assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("2 +") } }
    @Test fun repeatedOperatorsRejected() { assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("2 ** 3") } }
    @Test fun largeNumbers() { eq(1e18, "1000000000 * 1000000000") }
}
