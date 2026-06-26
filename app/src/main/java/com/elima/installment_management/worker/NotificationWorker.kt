package com.elima.installment_management.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elima.installment_management.data.AppDatabase
import com.elima.installment_management.data.SettingsManager
import com.elima.installment_management.data.model.PaymentStatus
import com.elima.installment_management.util.DateUtils
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
        val today = LocalDate.now()
        val targetDate = today.plusDays(daysBefore.toLong())

        val loansWithInstallments = database.loanDao().getLoansWithInstallments().first()

        for (item in loansWithInstallments) {
            val unpaidInstallments = item.installments.filter { it.paymentStatus != PaymentStatus.PAID }

            for (installment in unpaidInstallments) {
                val dueDate = installment.dueDate
                val daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)

                if (daysUntilDue == daysBefore.toLong()) {
                    // ۱. یادآوری دقیقاً بر اساس تنظیمات (مثلاً ۳ روز قبل)
                    val message = if (daysBefore == 0) {
                        "امروز موعد پرداخت قسط وام «${item.loan.title}» است."
                    } else {
                        "${DateUtils.formatNumber(daysBefore)} روز تا موعد قسط وام «${item.loan.title}» باقی مانده است."
                    }
                    notificationHelper.showNotification(
                        id = installment.id,
                        title = "یادآور سررسید",
                        message = message
                    )
                } else if (daysUntilDue == 0L && daysBefore != 0) {
                    // ۲. یادآوری در روز سررسید (حتی اگر تنظیمات روی روزهای قبل باشد)
                    notificationHelper.showNotification(
                        id = installment.id,
                        title = "موعد پرداخت قسط",
                        message = "امروز موعد پرداخت قسط وام «${item.loan.title}» است."
                    )
                } else if (daysUntilDue < 0) {
                    // ۳. یادآوری اقساط معوقه (تکرار هر روز تا زمان پرداخت)
                    notificationHelper.showNotification(
                        id = installment.id + 1000000,
                        title = "قسط معوقه",
                        message = "قسط وام «${item.loan.title}» مورخ ${DateUtils.toPersianDate(dueDate)} معوق شده است."
                    )
                }
            }
        }

        return Result.success()
    }
}
