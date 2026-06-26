package com.elima.installment_management.data.repository

import com.elima.installment_management.data.dao.LoanDao
import com.elima.installment_management.data.model.Installment
import com.elima.installment_management.data.model.LoanFacility
import com.elima.installment_management.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class LoanRepository(private val loanDao: LoanDao) {
    val allLoans: Flow<List<LoanFacility>> = loanDao.getAllLoans()
    val loansWithInstallments: Flow<List<com.elima.installment_management.data.model.LoanWithInstallments>> = loanDao.getLoansWithInstallments()

    suspend fun insertLoan(loan: LoanFacility): Long {
        return loanDao.insertLoan(loan)
    }

    suspend fun getLoanById(id: Int): LoanFacility? {
        return loanDao.getLoanById(id)
    }

    fun getInstallments(loanId: Int): Flow<List<Installment>> {
        return loanDao.getInstallmentsByLoanId(loanId)
    }

    suspend fun insertInstallments(installments: List<Installment>) {
        loanDao.insertInstallments(installments)
    }

    suspend fun payInstallment(installmentId: Int, paymentDate: LocalDate) {
        loanDao.updateInstallmentStatus(installmentId, PaymentStatus.PAID, paymentDate)
    }

    suspend fun unpayInstallment(installmentId: Int) {
        loanDao.updateInstallmentStatus(installmentId, PaymentStatus.PENDING, null)
    }

    suspend fun deleteLoan(loan: LoanFacility) {
        loanDao.deleteLoan(loan)
    }
}
