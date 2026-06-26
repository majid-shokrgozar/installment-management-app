package com.elima.installment_management.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elima.installment_management.data.model.Installment
import com.elima.installment_management.data.model.LoanFacility
import com.elima.installment_management.data.repository.LoanRepository
import com.elima.installment_management.data.model.PaymentStatus
import ir.huri.jcal.JalaliCalendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.GregorianCalendar

class LoanViewModel(private val repository: LoanRepository) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val allLoans: StateFlow<List<LoanFacility>> = repository.allLoans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val loansWithInstallments: StateFlow<List<com.elima.installment_management.data.model.LoanWithInstallments>> = 
        repository.loansWithInstallments
            .map { list ->
                list.sortedBy { loanWithIns ->
                    // پیدا کردن نزدیک‌ترین سررسید پرداخت نشده
                    val nextDue = loanWithIns.installments
                        .filter { it.paymentStatus != PaymentStatus.PAID }
                        .minByOrNull { it.dueDate }?.dueDate
                    
                    // اگر همه پرداخت شده باشند، به انتهای لیست می‌روند
                    nextDue ?: LocalDate.MAX
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // در اینجا می‌توان منطق به‌روزرسانی داده‌ها را قرار داد
            // برای مثال اگر داده‌ها از سرور می‌آمدند در اینجا فراخوانی می‌شدند
            // در این برنامه چون داده‌ها محلی هستند، صرفاً یک تأخیر کوتاه برای نمایش انیمیشن رفرش قرار می‌دهیم
            delay(1000)
            _isRefreshing.value = false
        }
    }

    fun addLoan(loan: LoanFacility) {
        addLoanWithPaidInstallments(loan, 0)
    }

    fun addLoanWithPaidInstallments(loan: LoanFacility, paidCount: Int) {
        viewModelScope.launch {
            val loanId = repository.insertLoan(loan).toInt()
            val installments = mutableListOf<Installment>()
            
            val startDate = loan.startDate
            val startJalali = JalaliCalendar(GregorianCalendar(startDate.year, startDate.monthValue - 1, startDate.dayOfMonth))
            
            for (i in 1..loan.installmentCount) {
                val monthsToAdd = i - 1
                var newMonth = startJalali.month + monthsToAdd
                var newYear = startJalali.year
                
                while (newMonth > 12) {
                    newMonth -= 12
                    newYear++
                }
                
                val maxDaysInNewMonth = when {
                    newMonth <= 6 -> 31
                    newMonth <= 11 -> 30
                    else -> {
                        val leapDays = ((newYear + 38) * 31) % 128
                        if (leapDays < 30) 30 else 29
                    }
                }
                
                val newDay = if (startJalali.day > maxDaysInNewMonth) maxDaysInNewMonth else startJalali.day
                
                val currentInstallmentJalali = JalaliCalendar(newYear, newMonth, newDay)
                val gregorian = currentInstallmentJalali.toGregorian()
                
                val dueDate = LocalDate.of(
                    gregorian.get(java.util.Calendar.YEAR),
                    gregorian.get(java.util.Calendar.MONTH) + 1,
                    gregorian.get(java.util.Calendar.DAY_OF_MONTH)
                )

                val isPaid = i <= paidCount
                val status = if (isPaid) com.elima.installment_management.data.model.PaymentStatus.PAID else com.elima.installment_management.data.model.PaymentStatus.PENDING

                installments.add(
                    Installment(
                        loanFacilityId = loanId,
                        sequenceNumber = i,
                        amount = loan.installmentAmount ?: (loan.principalAmount / loan.installmentCount),
                        dueDate = dueDate,
                        paymentDate = if (isPaid) dueDate else null,
                        paymentStatus = status,
                        note = "قسط شماره $i"
                    )
                )
            }
            repository.insertInstallments(installments)
        }
    }

    fun getInstallments(loanId: Int) = repository.getInstallments(loanId)

    suspend fun getLoanById(loanId: Int): LoanFacility? {
        return repository.getLoanById(loanId)
    }

    fun updateLoan(loan: LoanFacility) {
        viewModelScope.launch {
            repository.insertLoan(loan)
        }
    }

    fun payInstallment(installmentId: Int, paymentDate: LocalDate) {
        viewModelScope.launch {
            repository.payInstallment(installmentId, paymentDate)
        }
    }

    fun unpayInstallment(installmentId: Int) {
        viewModelScope.launch {
            repository.unpayInstallment(installmentId)
        }
    }

    fun deleteLoan(loan: LoanFacility) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }
}

class LoanViewModelFactory(private val repository: LoanRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
