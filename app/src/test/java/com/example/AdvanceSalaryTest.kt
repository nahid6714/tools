package com.example

import com.example.ui.AdvanceSalaryFormState
import com.example.util.AdvanceSalaryVoucherContent
import com.example.util.EnglishUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class AdvanceSalaryTest {

    @Test
    fun `test English amount in words`() {
        assertEquals("Fifteen Thousand Taka Only", EnglishUtils.amountToEnglishWords(15000.0))
        assertEquals("Five Hundred Taka Only", EnglishUtils.amountToEnglishWords(500.0))
        assertEquals("One Lakh Twenty-Five Thousand Taka Only".replace("-", " "), EnglishUtils.amountToEnglishWords(125000.0))
        assertEquals("Twenty-Five Thousand Four Hundred Fifty Taka Only".replace("-", " "), EnglishUtils.amountToEnglishWords(25450.0))
    }

    @Test
    fun `test currency formatting`() {
        assertEquals("15,000", EnglishUtils.formatEnglishCurrency(15000.0))
        assertEquals("25,450", EnglishUtils.formatEnglishCurrency(25450.0))
    }

    @Test
    fun `advance voucher rows include reason only when enabled`() {
        val withReason = AdvanceSalaryFormState(advanceAmount = 5000.0, reason = "Medical", showReason = true)
        val withoutReason = withReason.copy(showReason = false)
        assertEquals(6, AdvanceSalaryVoucherContent.section2Rows(withReason).size)
        assertEquals(5, AdvanceSalaryVoucherContent.section2Rows(withoutReason).size)
        assertEquals(false, AdvanceSalaryVoucherContent.section2Rows(withoutReason).any { it.first == "Reason" })
    }

    @Test
    fun `current salary voucher has no repayment rows`() {
        val state = AdvanceSalaryFormState(
            applicationType = "CURRENT_SALARY",
            advanceAmount = 12000.0,
            salaryMonth = "October 2026",
            showReason = false
        )
        val labels = AdvanceSalaryVoucherContent.section2Rows(state).map { it.first }
        assertEquals(listOf("Salary Amount", "In Words", "Salary Month"), labels)
        assertEquals("CURRENT SALARY REQUISITION VOUCHER", AdvanceSalaryVoucherContent.title(state))
    }
}
