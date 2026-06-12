package com.elima.installment_management.data.dao

import androidx.room.*
import com.elima.installment_management.data.model.Installment
import com.elima.installment_management.data.model.LoanFacility
import com.elima.installment_management.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface LoanDao {
    @Query("SELECT * FROM loan_facilities")
    fun getAllLoans(): Flow<List<LoanFacility>>

    @Transaction
    @Query("SELECT * FROM loan_facilities")
    fun getLoansWithInstallments(): Flow<List<com.elima.installment_management.data.model.LoanWithInstallments>>

    @Query("SELECT * FROM loan_facilities WHERE id = :id")
    suspend fun getLoanById(id: Int): LoanFacility?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanFacility): Long

    @Delete
    suspend fun deleteLoan(loan: LoanFacility)

    @Query("SELECT * FROM installments WHERE loanFacilityId = :loanId ORDER BY sequenceNumber ASC")
    fun getInstallmentsByLoanId(loanId: Int): Flow<List<Installment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<Installment>)

    @Update
    suspend fun updateInstallment(installment: Installment)

    @Query("UPDATE installments SET paymentStatus = :status, paymentDate = :paymentDate WHERE id = :installmentId")
    suspend fun updateInstallmentStatus(installmentId: Int, status: PaymentStatus, paymentDate: LocalDate?)
}
