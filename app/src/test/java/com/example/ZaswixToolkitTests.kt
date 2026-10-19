package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ChecklistItem
import com.example.data.repository.NotesRepository
import com.example.data.repository.ToolsRepository
import com.example.domain.calculator.CalculatorEngine
import com.example.domain.calculator.PercentageEngine
import com.example.domain.converters.UnitCategory
import com.example.domain.converters.UnitConverterEngine
import com.example.domain.daily.BmiCategory
import com.example.domain.daily.DailyUtilitiesEngine
import com.example.domain.datetime.DateTimeEngine
import com.example.domain.password.PasswordGeneratorEngine
import com.example.domain.password.PasswordStrength
import com.example.domain.text.TextToolkitEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ZaswixToolkitTests {

    private lateinit var database: AppDatabase
    private lateinit var notesRepository: NotesRepository
    private lateinit var toolsRepository: ToolsRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        notesRepository = NotesRepository(database.noteDao())
        toolsRepository = ToolsRepository(database.toolUsageDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    // 1. Calculator Arithmetic, Unary Minus, and Percent Operations
    @Test
    fun testCalculatorOperatorPrecedence() {
        val result1 = CalculatorEngine.evaluate("2 + 3 * 4").getOrThrow()
        assertEquals(BigDecimal("14"), result1)

        val result2 = CalculatorEngine.evaluate("(2 + 3) * 4").getOrThrow()
        assertEquals(BigDecimal("20"), result2)

        val result3 = CalculatorEngine.evaluate("10 - 4 / 2").getOrThrow()
        assertEquals(BigDecimal("8"), result3)

        val resultDecimal = CalculatorEngine.evaluate("5.5 + 2.25").getOrThrow()
        assertEquals(BigDecimal("7.75"), resultDecimal)
    }

    @Test
    fun testCalculatorUnaryMinusAndPercent() {
        // Fix for 2/-(4) = -0.5 using dedicated unary negation operator
        val resultUnaryDiv = CalculatorEngine.evaluate("2/-(4)").getOrThrow()
        assertEquals(BigDecimal("-0.5"), resultUnaryDiv)

        val resultNegativeSimple = CalculatorEngine.evaluate("-5 + 3").getOrThrow()
        assertEquals(BigDecimal("-2"), resultNegativeSimple)

        // Percent operator (50% = 0.5)
        val resultPercent = CalculatorEngine.evaluate("50%").getOrThrow()
        assertEquals(BigDecimal("0.5"), resultPercent)

        val resultPercentInExpr = CalculatorEngine.evaluate("200 * 15%").getOrThrow()
        assertEquals(BigDecimal("30"), resultPercentInExpr)
    }

    @Test
    fun testCalculatorMalformedNumbersRejected() {
        val malformed = CalculatorEngine.evaluate("1.2.3 + 4")
        assertTrue(malformed.isFailure)
        assertEquals("Invalid number", malformed.exceptionOrNull()?.message)

        val singleDot = CalculatorEngine.evaluate(". + 5")
        assertTrue(singleDot.isFailure)
        assertEquals("Invalid number", singleDot.exceptionOrNull()?.message)
    }

    @Test
    fun testCalculatorDivisionByZero() {
        val result = CalculatorEngine.evaluate("10 / 0")
        assertTrue(result.isFailure)
        assertEquals("Division by zero", result.exceptionOrNull()?.message)
    }

    // 2. Percentage and Unit Conversions (Decimal KB vs Binary KiB & Megabit)
    @Test
    fun testPercentageCalculations() {
        val p1 = PercentageEngine.percentageOf(BigDecimal("15"), BigDecimal("200"))
        assertEquals(BigDecimal("30"), p1)

        val p2 = PercentageEngine.whatPercentage(BigDecimal("50"), BigDecimal("200"))
        assertEquals(BigDecimal("25"), p2)

        val p3 = PercentageEngine.percentageChange(BigDecimal("100"), BigDecimal("150"))
        assertEquals(BigDecimal("50"), p3)
    }

    @Test
    fun testDataStorageUnitsDecimalAndBinary() {
        val dataUnits = UnitConverterEngine.unitsByCategory[UnitCategory.DATA_STORAGE]!!
        val byteUnit = dataUnits.first { it.id == "b" }
        val kbUnit = dataUnits.first { it.id == "kb" }
        val kibUnit = dataUnits.first { it.id == "kib" }
        val mbitUnit = dataUnits.first { it.id == "mbit" }
        val bitUnit = dataUnits.first { it.id == "bit" }

        // Decimal: 1 KB = 1000 Bytes
        val kbToBytes = UnitConverterEngine.convert(BigDecimal("1"), kbUnit, byteUnit)
        assertEquals(BigDecimal("1000"), kbToBytes)

        // Binary: 1 KiB = 1024 Bytes
        val kibToBytes = UnitConverterEngine.convert(BigDecimal("1"), kibUnit, byteUnit)
        assertEquals(BigDecimal("1024"), kibToBytes)

        // Megabit = 1,000,000 bits = 125,000 Bytes
        val mbitToBytes = UnitConverterEngine.convert(BigDecimal("1"), mbitUnit, byteUnit)
        assertEquals(BigDecimal("125000"), mbitToBytes)

        val mbitToBits = UnitConverterEngine.convert(BigDecimal("1"), mbitUnit, bitUnit)
        assertEquals(BigDecimal("1000000"), mbitToBits)
    }

    // 3. Daily Utilities & Leftover Cent Distribution
    @Test
    fun testTipLeftoverCentDistribution() {
        // $10.00 total bill with 0% tip for 3 people:
        // person 0: $3.34, person 1: $3.33, person 2: $3.33
        // sum = $10.00 exactly
        val tip = DailyUtilitiesEngine.calculateTip(BigDecimal("10.00"), BigDecimal("0"), 3)
        assertEquals(BigDecimal("10.00"), tip.totalBill)
        assertEquals(3, tip.perPersonAmounts.size)
        assertEquals(1, tip.leftoverCents)
        assertEquals(BigDecimal("3.34"), tip.perPersonAmounts[0])
        assertEquals(BigDecimal("3.33"), tip.perPersonAmounts[1])
        assertEquals(BigDecimal("3.33"), tip.perPersonAmounts[2])

        val totalSum = tip.perPersonAmounts.reduce { acc, b -> acc.add(b) }
        assertEquals(tip.totalBill, totalSum)
    }

    @Test
    fun testBmiCalculation() {
        val bmi = DailyUtilitiesEngine.calculateBmiMetric(BigDecimal("175"), BigDecimal("70"))
        assertEquals(BmiCategory.NORMAL, bmi.category)
        assertTrue(bmi.bmiValue > BigDecimal("22") && bmi.bmiValue < BigDecimal("24"))
    }

    // 4. Persistence of Notes: Preserving createdAt on Edit
    @Test
    fun testNotesCreatedAtPreservedOnEdit() = runBlocking {
        val initialNoteId = notesRepository.saveNote(
            title = "Original Title",
            content = "Original Content",
            isPinned = false,
            isChecklist = false
        )
        val originalNote = notesRepository.getNoteById(initialNoteId)
        assertNotNull(originalNote)
        val originalCreatedAt = originalNote!!.createdAt

        // Wait a tiny moment to ensure timestamp would differ if newly created
        Thread.sleep(10)

        // Edit existing note
        notesRepository.saveNote(
            id = initialNoteId,
            title = "Updated Title",
            content = "Updated Content",
            isPinned = true,
            isChecklist = false
        )

        val updatedNote = notesRepository.getNoteById(initialNoteId)
        assertNotNull(updatedNote)
        assertEquals("Updated Title", updatedNote!!.title)
        assertTrue(updatedNote.isPinned)
        // Verify createdAt was preserved and not overwritten
        assertEquals(originalCreatedAt, updatedNote.createdAt)
    }

    // 5. Room Database Migration Strategy
    @Test
    fun testRoomMigrationStrategy() {
        val migration = AppDatabase.MIGRATION_1_2
        assertEquals(1, migration.startVersion)
        assertEquals(2, migration.endVersion)
    }

    // 6. Text Processing, Hindi, and Dates
    @Test
    fun testUnicodeAndHindiText() {
        val hindiText = "नमस्ते दुनिया! Welcome to Zaswix"
        val stats = TextToolkitEngine.analyze(hindiText)
        assertTrue(stats.wordCount >= 4)
    }

    @Test
    fun testDateLeapYear() {
        assertTrue(DateTimeEngine.isLeapYear(2024))
        assertFalse(DateTimeEngine.isLeapYear(2023))
    }

    @Test
    fun testPasswordGenerator() {
        val pwd = PasswordGeneratorEngine.generate(
            length = 16,
            includeUpper = true,
            includeLower = true,
            includeDigits = true,
            includeSymbols = true,
            excludeAmbiguous = true
        )
        assertEquals(16, pwd.length)
        val strength = PasswordGeneratorEngine.assessStrength(pwd)
        assertTrue(strength.strength == PasswordStrength.STRONG || strength.strength == PasswordStrength.VERY_STRONG)
    }
}
