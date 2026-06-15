package com.elima.installment_management.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.elima.installment_management.data.model.LoanWithInstallments
import com.elima.installment_management.data.model.PaymentStatus
import com.elima.installment_management.ui.viewmodel.LoanViewModel
import com.elima.installment_management.util.DateUtils
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanListScreen(
    viewModel: LoanViewModel,
    onAddLoanClick: () -> Unit,
    onLoanClick: (Int) -> Unit,
    onEditLoanClick: (Int) -> Unit
) {
    val loansWithInstallments by viewModel.loansWithInstallments.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var selectedLoan by remember { mutableStateOf<LoanWithInstallments?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val lifecycleOwner = LocalLifecycleOwner.current

    // رفرش کردن لیست هنگام باز شدن برنامه و بازگشت از پس‌زمینه
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLoanClick) {
                Icon(Icons.Default.Add, contentDescription = "افزودن تسهیلات")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loansWithInstallments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "هیچ تسهیلاتی ثبت نشده است", fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(loansWithInstallments) { item ->
                        LoanItem(
                            item,
                            onInstallmentsClick = { onLoanClick(item.loan.id) },
                            onShowClick = {
                                selectedLoan = item
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }

        if (showBottomSheet && selectedLoan != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                LoanDetailsContent(
                    loanWithInstallments = selectedLoan!!,
                    onEditClick = {
                        showBottomSheet = false
                        onEditLoanClick(selectedLoan!!.loan.id)
                    },
                    onDeleteClick = {
                        showDeleteDialog = true
                    }
                )
            }
        }

        if (showDeleteDialog && selectedLoan != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("حذف تسهیلات") },
                text = { Text("آیا از حذف «${selectedLoan!!.loan.title}» و تمامی اقساط آن اطمینان دارید؟") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteLoan(selectedLoan!!.loan)
                            showDeleteDialog = false
                            showBottomSheet = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }
    }
}

@Composable
fun LoanDetailsContent(
    loanWithInstallments: LoanWithInstallments,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val loan = loanWithInstallments.loan
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "جزئیات تسهیلات",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        
        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))
        
        DetailRow("عنوان:", loan.title)
        DetailRow("نام بانک/ارائه‌دهنده:", loan.providerName)
        DetailRow("مبلغ اصل وام:", formatCurrency(loan.principalAmount))
        DetailRow("مبلغ قسط:", formatCurrency(loan.installmentAmount ?: 0.0))
        DetailRow("تعداد کل اقساط:", loan.installmentCount.toString())
        DetailRow("تاریخ دریافت:", DateUtils.toPersianDate(loan.startDate))
        loan.description?.let {
            if (it.isNotBlank()) {
                DetailRow("توضیحات:", it)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LoanItem(
    loanWithInstallments: LoanWithInstallments,
    onInstallmentsClick: () -> Unit,
    onShowClick: () -> Unit
) {
    val loan = loanWithInstallments.loan
    val installments = loanWithInstallments.installments
    val today = LocalDate.now()
    
    val paidCount = installments.count { it.paymentStatus == PaymentStatus.PAID }
    val totalCount = loan.installmentCount
    val progress = if (totalCount > 0) paidCount.toFloat() / totalCount else 0f

    // پیدا کردن تاریخ سررسید بعدی (اولین قسط پرداخت نشده)
    val nextInstallment = installments
        .filter { it.paymentStatus != PaymentStatus.PAID }
        .minByOrNull { it.dueDate }
    
    val nextDueDate = nextInstallment?.dueDate
    
    // تشخیص اینکه آیا وام قسط معوقه دارد یا خیر
    val hasOverdue = installments.any { it.paymentStatus != PaymentStatus.PAID && it.dueDate.isBefore(today) }
    
    val isDarkTheme = isSystemInDarkTheme()
    val overdueColor = if (isDarkTheme) Color(0xFFE57373) else Color(0xFFC62828)
    val overdueBackground = if (isDarkTheme) Color(0xFF311B1B) else Color(0xFFFFEBEE)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasOverdue) overdueBackground else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(45.dp),
                        color = if (hasOverdue) overdueColor else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasOverdue) overdueColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // نام تسهیلات و نام بانک در پرانتز
                    Text(
                        text = "${loan.title} (${loan.providerName})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (hasOverdue) overdueColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (nextDueDate != null) {
                        val remainingDays = DateUtils.getRemainingDaysText(nextDueDate)
                        if (hasOverdue) {
                            // نمایش تاریخ سررسید در یک چیپسی قرمز برای وام‌های معوقه
                            Surface(
                                color = overdueColor.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small,
                                border = androidx.compose.foundation.BorderStroke(1.dp, overdueColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${DateUtils.toPersianDate(nextDueDate)} $remainingDays",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 12.sp,
                                    color = overdueColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "${DateUtils.toPersianDate(nextDueDate)} $remainingDays",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مبلغ قسط:", 
                            fontSize = 14.sp,
                            color = if (hasOverdue) overdueColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val installmentAmount = loan.installmentAmount ?: (if (totalCount > 0) loan.principalAmount / totalCount else 0.0)
                        Text(
                            text = formatCurrency(installmentAmount), 
                            fontWeight = FontWeight.Medium,
                            color = if (hasOverdue) overdueColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تعداد اقساط:", 
                            fontSize = 14.sp,
                            color = if (hasOverdue) overdueColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$paidCount از $totalCount پرداخت شده",
                            fontWeight = FontWeight.Medium,
                            color = if (hasOverdue) overdueColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onShowClick,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نمایش")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onInstallmentsClick,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اقساط")
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getInstance(Locale("fa", "IR"))
    return format.format(amount) + " ریال"
}
