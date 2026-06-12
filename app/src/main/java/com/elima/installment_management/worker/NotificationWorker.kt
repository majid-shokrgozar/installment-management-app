package com.elima.installment_management.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elima.installment_management.data.AppDatabase
import com.elima.installment_management.data.SettingsManager
import com.elima.installment_management.data.model.PaymentStatus
import com.elima.installment_management.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settingsManager = SettingsManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        val notificationHelper = NotificationHelper(applicationContext)
        
        notificationHelper.createNotificationChannel()

        val daysBefore = settingsManager.notificationDaysBefore
        val targetDate = LocalDate.now().plusDays(daysBefore.toLong())

        val loansWithInstallments = database.loanDao().getLoansWithInstallments().first()

        for (item in loansWithInstallments) {
            val upcomingInstallment = item.installments.find { 
                it.paymentStatus != PaymentStatus.PAID && it.dueDate == targetDate 
            }

            if (upcomingInstallment != null) {
                val message = if (daysBefore == 0) {
                    "امروز موعد پرداخت قسط وام «${item.loan.title}» است."
                } else {
                    "$daysBefore روز تا موعد قسط وام «${item.loan.title}» باقی مانده است."
                }
                
                notificationHelper.showNotification(
                    id = upcomingInstallment.id,
                    title = "سررسید قسط",
                    message = message
                )
            }
        }

        return Result.success()
    }
}
