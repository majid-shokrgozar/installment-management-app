package com.elima.installment_management.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elima.installment_management.data.model.Installment
import com.elima.installment_management.data.model.PaymentStatus
import com.elima.installment_management.ui.viewmodel.LoanViewModel
import com.elima.installment_management.util.DateUtils
import ir.huri.jcal.JalaliCalendar
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentListScreen(
    loanId: Int,
    viewModel: LoanViewModel,
    onBack: () -> Unit
) {
    val installments by viewModel.getInstallments(loanId).collectAsState(initial = emptyList())
    var selectedInstallmentId by remember { mutableStateOf<Int?>(null) }
    
    val listState = rememberLazyListState()

    LaunchedEffect(installments) {
        if (installments.isNotEmpty()) {
            val firstUnpaidIndex = installments.indexOfFirst { it.paymentStatus != PaymentStatus.PAID }
            if (firstUnpaidIndex != -1) {
                listState.animateScrollToItem(firstUnpaidIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لیست اقساط") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        if (installments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "هیچ قسطی برای این تسهیلات ثبت نشده است", fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(installments) { _, installment ->
                    InstallmentItem(
                        installment = installment,
                        onPayClick = { selectedInstallmentId = installment.id }
                    )
                }
            }
        }

        if (selectedInstallmentId != null) {
            PersianPaymentDateDialog(
                onDismiss = { selectedInstallmentId = null },
                onConfirm = { date ->
                    selectedInstallmentId?.let { id ->
                        viewModel.payInstallment(id, date)
                    }
                    selectedInstallmentId = null
                }
            )
        }
    }
}

@Composable
fun InstallmentItem(
    installment: Installment,
    onPayClick: () -> Unit
) {
    val currencyFormat = NumberFormat.getInstance(Locale("fa", "IR"))
    val today = LocalDate.now()
    
    // تشخیص وضعیت سررسید گذشته
    val isOverdue = installment.paymentStatus != PaymentStatus.PAID && installment.dueDate.isBefore(today)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                installment.paymentStatus == PaymentStatus.PAID -> Color(0xFFE8F5E9)
                isOverdue -> Color(0xFFFFEBEE) // رنگ قرمز ملایم برای قسط معوقه
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "قسط شماره ${installment.sequenceNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isOverdue) Color(0xFFC62828) else Color.Unspecified
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تاریخ سررسید: ${DateUtils.toPersianDate(installment.dueDate)}",
                        fontSize = 14.sp,
                        color = if (isOverdue) Color(0xFFC62828) else Color.Gray
                    )
                    if (installment.paymentStatus == PaymentStatus.PAID && installment.paymentDate != null) {
                        Text(
                            text = "تاریخ پرداخت: ${DateUtils.toPersianDate(installment.paymentDate)}",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    if (isOverdue) {
                        Text(
                            text = "سررسید گذشته",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${currencyFormat.format(installment.amount)} ریال",
                        fontWeight = FontWeight.Medium,
                        color = if (isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(
                        status = installment.paymentStatus,
                        isOverdue = isOverdue
                    )
                }
            }

            if (installment.paymentStatus != PaymentStatus.PAID) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverdue) Color(0xFFC62828) else Color(0xFF4CAF50)
                    )
                ) {
                    Text("پرداخت", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: PaymentStatus, isOverdue: Boolean) {
    val (text, color) = when {
        status == PaymentStatus.PAID -> "پرداخت شده" to Color(0xFF4CAF50)
        isOverdue -> "معوقه" to Color(0xFFF44336)
        status == PaymentStatus.PENDING -> "در انتظار" to Color(0xFFFF9800)
        status == PaymentStatus.CANCELLED -> "لغو شده" to Color(0xFFF44336)
        else -> "" to Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PersianPaymentDateDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val today = JalaliCalendar()
    var year by remember { mutableStateOf(today.year.toString()) }
    var month by remember { mutableStateOf(today.month.toString()) }
    var day by remember { mutableStateOf(today.day.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تاریخ پرداخت را وارد کنید") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { if (it.length <= 4) year = it },
                    label = { Text("سال") },
                    modifier = Modifier.weight(1.5f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { if (it.length <= 2) month = it },
                    label = { Text("ماه") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = day,
                    onValueChange = { if (it.length <= 2) day = it },
                    label = { Text("روز") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                try {
                    val jYear = year.toInt()
                    val jMonth = month.toInt()
                    val jDay = day.toInt()
                    
                    val jalali = JalaliCalendar(jYear, jMonth, jDay)
                    val gregorian = jalali.toGregorian()
                    val localDate = LocalDate.of(
                        gregorian.get(java.util.Calendar.YEAR),
                        gregorian.get(java.util.Calendar.MONTH) + 1,
                        gregorian.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                    onConfirm(localDate)
                } catch (e: Exception) {
                    onConfirm(LocalDate.now())
                }
            }) {
                Text("تایید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
