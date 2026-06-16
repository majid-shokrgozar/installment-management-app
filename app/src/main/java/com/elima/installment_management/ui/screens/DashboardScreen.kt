package com.elima.installment_management.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elima.installment_management.data.model.PaymentStatus
import com.elima.installment_management.ui.viewmodel.LoanViewModel
import ir.huri.jcal.JalaliCalendar
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: LoanViewModel) {
    val loansWithInstallments by viewModel.loansWithInstallments.collectAsState()
    val today = LocalDate.now()
    val todayJalali = JalaliCalendar()

    val allInstallments = loansWithInstallments.flatMap { it.installments }

    // --- محاسبات کلی ---
    val paidOverall = allInstallments.filter { it.paymentStatus == PaymentStatus.PAID }
    val remainingOverall = allInstallments.filter { it.paymentStatus != PaymentStatus.PAID }
    val overdueOverall = allInstallments.filter { it.paymentStatus != PaymentStatus.PAID && it.dueDate.isBefore(today) }
    val nearDueOverall = allInstallments.filter {
        it.paymentStatus != PaymentStatus.PAID && !it.dueDate.isBefore(today) && it.dueDate.isBefore(today.plusDays(8))
    }

    // --- محاسبات ماه جاری ---
    val thisMonthInstallments = allInstallments.filter {
        val installmentJalali = JalaliCalendar(GregorianCalendar(it.dueDate.year, it.dueDate.monthValue - 1, it.dueDate.dayOfMonth))
        installmentJalali.year == todayJalali.year && installmentJalali.month == todayJalali.month
    }
    val paidThisMonth = thisMonthInstallments.filter { it.paymentStatus == PaymentStatus.PAID }
    val remainingThisMonth = thisMonthInstallments.filter { it.paymentStatus != PaymentStatus.PAID }
    val overdueThisMonth = thisMonthInstallments.filter { it.paymentStatus != PaymentStatus.PAID && it.dueDate.isBefore(today) }
    val nearDueThisMonth = thisMonthInstallments.filter {
        it.paymentStatus != PaymentStatus.PAID && !it.dueDate.isBefore(today) && it.dueDate.isBefore(today.plusDays(8))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("داشبورد", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ۱. باکس ماه جاری
            item {
                DashboardSection(
                    title = "وضعیت ماه جاری (${formatPersianMonthName(todayJalali.month)})",
                    dateText = formatPersianMonth(todayJalali.year, todayJalali.month),
                    paidAmount = paidThisMonth.sumOf { it.amount },
                    paidCount = paidThisMonth.size,
                    remainingAmount = remainingThisMonth.sumOf { it.amount },
                    remainingCount = remainingThisMonth.size,
                    overdueAmount = overdueThisMonth.sumOf { it.amount },
                    overdueCount = overdueThisMonth.size,
                    nearDueAmount = nearDueThisMonth.sumOf { it.amount },
                    nearDueCount = nearDueThisMonth.size
                )
            }

            // ۲. باکس کلی
            item {
                DashboardSection(
                    title = "وضعیت کلی اقساط",
                    dateText = formatPersianDate(todayJalali.year, todayJalali.month, todayJalali.day),
                    paidAmount = paidOverall.sumOf { it.amount },
                    paidCount = paidOverall.size,
                    remainingAmount = remainingOverall.sumOf { it.amount },
                    remainingCount = remainingOverall.size,
                    overdueAmount = overdueOverall.sumOf { it.amount },
                    overdueCount = overdueOverall.size,
                    nearDueAmount = nearDueOverall.sumOf { it.amount },
                    nearDueCount = nearDueOverall.size,
                    loanCount = loansWithInstallments.size
                )
            }
        }
    }
}

@Composable
fun DashboardSection(
    title: String,
    dateText: String,
    paidAmount: Double,
    paidCount: Int,
    remainingAmount: Double,
    remainingCount: Int,
    overdueAmount: Double,
    overdueCount: Int,
    nearDueAmount: Double,
    nearDueCount: Int,
    loanCount: Int? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (loanCount != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${formatNumber(loanCount)} وام",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Text(text = dateText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "پرداخت شده",
                    amount = paidAmount,
                    count = paidCount,
                    emoji = "✅",
                    color = Color(0xFF4CAF50)
                )
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "مانده",
                    amount = remainingAmount,
                    count = remainingCount,
                    emoji = "🧐",
                    color = Color(0xFFFFA000)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "عقب افتاده",
                    amount = overdueAmount,
                    count = overdueCount,
                    emoji = "🤦‍♂️",
                    color = Color(0xFFE53935)
                )
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "نزدیک سررسید",
                    amount = nearDueAmount,
                    count = nearDueCount,
                    emoji = "⏰",
                    color = Color(0xFF1E88E5)
                )
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    amount: Double,
    count: Int,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Column {
                Text(
                    text = "${formatCurrencyValue(amount / 10)} تومان",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${formatNumber(count)} مورد",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun formatCurrencyValue(amount: Double): String {
    val format = NumberFormat.getInstance(Locale("fa", "IR"))
    return format.format(amount)
}

private fun formatNumber(number: Int): String {
    val format = NumberFormat.getInstance(Locale("fa", "IR"))
    return format.format(number)
}

private fun formatPersianDate(year: Int, month: Int, day: Int): String {
    val format = NumberFormat.getInstance(Locale("fa", "IR"))
    format.isGroupingUsed = false
    val y = format.format(year)
    val m = String.format(Locale("fa", "IR"), "%02d", month)
    val d = String.format(Locale("fa", "IR"), "%02d", day)
    return "$y/$m/$d"
}

private fun formatPersianMonth(year: Int, month: Int): String {
    val format = NumberFormat.getInstance(Locale("fa", "IR"))
    format.isGroupingUsed = false
    val y = format.format(year)
    val m = String.format(Locale("fa", "IR"), "%02d", month)
    return "$y/$m"
}

private fun formatPersianMonthName(month: Int): String {
    return when (month) {
        1 -> "فروردین"
        2 -> "اردیبهشت"
        3 -> "خرداد"
        4 -> "تیر"
        5 -> "مرداد"
        6 -> "شهریور"
        7 -> "مهر"
        8 -> "آبان"
        9 -> "آذر"
        10 -> "دی"
        11 -> "بهمن"
        12 -> "اسفند"
        else -> ""
    }
}
