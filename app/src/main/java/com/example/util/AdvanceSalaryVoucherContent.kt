package com.example.util

import com.example.ui.AdvanceSalaryFormState

/**
 * Single place that decides the wording / rows of the voucher for the two modes
 * (Advance Salary and Current Salary). Used by both the PDF and the image exporter
 * so the two outputs always stay identical.
 */
object AdvanceSalaryVoucherContent {

    const val TYPE_ADVANCE = "ADVANCE"
    const val TYPE_CURRENT = "CURRENT_SALARY"

    fun isCurrent(state: AdvanceSalaryFormState): Boolean = state.applicationType == TYPE_CURRENT

    fun title(state: AdvanceSalaryFormState): String =
        if (isCurrent(state)) "CURRENT SALARY REQUISITION VOUCHER" else "ADVANCE SALARY REQUISITION VOUCHER"

    fun screenTitle(state: AdvanceSalaryFormState): String =
        if (isCurrent(state)) "Current Salary Application" else "Advance Salary Application"

    fun voucherName(state: AdvanceSalaryFormState): String =
        if (isCurrent(state)) "Current Salary Requisition Voucher" else "Advance Salary Requisition Voucher"

    fun sectionBadge(state: AdvanceSalaryFormState): String =
        if (isCurrent(state)) "SALARY PAYMENT DETAILS" else "ADVANCE & REPAYMENT TERMS"

    /** Used in file names, e.g. Advance_Salary_Rahim_01-09-2026 */
    fun filePrefix(state: AdvanceSalaryFormState): String =
        if (isCurrent(state)) "Current_Salary" else "Advance_Salary"

    /** Used in share subjects. */
    fun shareLabel(state: AdvanceSalaryFormState): String =
        if (isCurrent(state)) "Current Salary Application" else "Advance Salary Application"

    fun declarationLines(state: AdvanceSalaryFormState): Pair<String, String> =
        if (isCurrent(state)) {
            "I hereby request the management to release my current salary," to
                "which is still unpaid, as per the details mentioned above."
        } else {
            "I hereby authorize the management to deduct the stated advance amount" to
                "from my monthly salary in accordance with the terms mentioned above."
        }

    /** Rows of the second table (label to value). The Reason row is skipped when showReason = false. */
    fun section2Rows(state: AdvanceSalaryFormState): List<Pair<String, String>> {
        val rows = mutableListOf<Pair<String, String>>()
        val amountStr = if (state.advanceAmount > 0) "Tk. ${EnglishUtils.formatEnglishCurrency(state.advanceAmount)}/-" else "Tk. 5,000/-"
        val inWords = state.advanceAmountInWords.ifBlank {
            if (state.advanceAmount > 0) EnglishUtils.amountToEnglishWords(state.advanceAmount) else "Five Thousand Taka Only"
        }

        if (isCurrent(state)) {
            rows += "Salary Amount" to amountStr
            rows += "In Words" to inWords
            if (state.previousAdvancePending > 0.0) {
                val net = (state.advanceAmount - state.previousAdvancePending).coerceAtLeast(0.0)
                rows += "Previously Taken" to "Tk. ${EnglishUtils.formatEnglishCurrency(state.previousAdvancePending)}/-"
                rows += "Net Payable" to "Tk. ${EnglishUtils.formatEnglishCurrency(net)}/-"
            }
            rows += "Salary Month" to state.salaryMonth.ifBlank { "Current Month" }
            if (state.showReason) {
                rows += "Reason" to state.reason.ifBlank { "Salary not yet received" }
            }
        } else {
            rows += "Advance Amount" to amountStr
            rows += "In Words" to inWords
            val remaining = if (state.monthlySalary > 0) {
                (state.monthlySalary - state.previousAdvancePending - state.advanceAmount).coerceAtLeast(0.0)
            } else 0.0
            rows += "Remaining Salary" to (if (state.monthlySalary > 0) "Tk. ${EnglishUtils.formatEnglishCurrency(remaining)}/-" else "Tk. 0/-")
            if (state.showReason) {
                rows += "Reason" to state.reason.ifBlank { "Personal Emergency" }
            }
            rows += "Repayment" to (
                if (state.repaymentType == "installments") {
                    "Monthly Installments: ${state.installmentCount} Months (Tk. ${EnglishUtils.formatEnglishCurrency(state.installmentAmountPerMonth)}/mo)"
                } else {
                    "One-time full deduction from salary"
                }
                )
            rows += "Deduction Starts" to state.deductionStartMonth.ifBlank { "Next Month" }
        }
        return rows
    }
}
