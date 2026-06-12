package com.elima.installment_management

import android.app.Application
import com.elima.installment_management.data.AppDatabase
import com.elima.installment_management.data.repository.LoanRepository
import com.elima.installment_management.worker.NotificationScheduler

class MyApplication : Application() {
    // استفاده از getter برای اطمینان از دریافت آخرین اینستنس بعد از ری‌استور
    val database: AppDatabase get() = AppDatabase.getDatabase(this)
    val repository: LoanRepository get() = LoanRepository(database.loanDao())

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.scheduleDailyNotification(this)
    }
}
