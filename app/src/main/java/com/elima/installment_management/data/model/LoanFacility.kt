package com.elima.installment_management.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "loan_facilities")
data class LoanFacility(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val providerName: String,
    val facilityType: String?,
    val contractNumber: String?,
    val principalAmount: Double,
    val receivedAmount: Double?,
    val interestRate: Double?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val installmentCount: Int,
    val installmentAmount: Double?,
    val description: String?,
    val createdAtUtc: LocalDateTime = LocalDateTime.now(),
    val userId: String,
    @Ignore val installments: List<Installment> = emptyList()
) {
    // Secondary constructor for Room to use (ignores the installments list)
    constructor(
        id: Int,
        title: String,
        providerName: String,
        facilityType: String?,
        contractNumber: String?,
        principalAmount: Double,
        receivedAmount: Double?,
        interestRate: Double?,
        startDate: LocalDate,
        endDate: LocalDate?,
        installmentCount: Int,
        installmentAmount: Double?,
        description: String?,
        createdAtUtc: LocalDateTime,
        userId: String
    ) : this(
        id, title, providerName, facilityType, contractNumber, principalAmount,
        receivedAmount, interestRate, startDate, endDate, installmentCount,
        installmentAmount, description, createdAtUtc, userId, emptyList()
    )
}

data class LoanWithInstallments(
    @androidx.room.Embedded val loan: LoanFacility,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "loanFacilityId"
    )
    val installments: List<Installment>
)

@Entity(tableName = "installments")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanFacilityId: Int,
    val sequenceNumber: Int,
    val amount: Double,
    val dueDate: LocalDate,
    val paymentDate: LocalDate?,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val dueStatus: DueStatus = DueStatus.UPCOMING,
    val note: String?,
    val createdAtUtc: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus(val value: Int) {
    PENDING(0),
    PAID(1),
    CANCELLED(2)
}

enum class DueStatus(val value: Int) {
    UPCOMING(0),
    DUE_TODAY(1),
    OVERDUE(2),
    SETTLED(3)
}
