package com.elima.installment_management.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elima.installment_management.data.model.LoanFacility
import com.elima.installment_management.ui.viewmodel.LoanViewModel
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.util.GregorianCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanScreen(
    viewModel: LoanViewModel,
    onBack: () -> Unit,
    loanId: Int? = null
) {
    val currencyVisualTransformation = remember { CurrencyVisualTransformation() }
    var title by remember { mutableStateOf("") }
    var providerName by remember { mutableStateOf("") }
    var principalAmount by remember { mutableStateOf("") }
    var installmentAmount by remember { mutableStateOf("") }
    var installmentCount by remember { mutableStateOf("") }
    var paidInstallmentCount by remember { mutableStateOf("0") }
    var description by remember { mutableStateOf("") }
    
    // تاریخ اولین سررسید (شمسی)
    val today = JalaliCalendar()
    var startYear by remember { mutableStateOf(today.year.toString()) }
    var startMonth by remember { mutableStateOf(today.month.toString()) }
    var startDay by remember { mutableStateOf(today.day.toString()) }

    var existingLoan by remember { mutableStateOf<LoanFacility?>(null) }

    LaunchedEffect(loanId) {
        if (loanId != null) {
            val loan = viewModel.getLoanById(loanId)
            if (loan != null) {
                existingLoan = loan
                title = loan.title
                providerName = loan.providerName
                principalAmount = loan.principalAmount.toLong().toString()
                installmentAmount = loan.installmentAmount?.toLong()?.toString() ?: ""
                installmentCount = loan.installmentCount.toString()
                description = loan.description ?: ""
                
                val jalali = JalaliCalendar(GregorianCalendar(loan.startDate.year, loan.startDate.monthValue - 1, loan.startDate.dayOfMonth))
                startYear = jalali.year.toString()
                startMonth = jalali.month.toString()
                startDay = jalali.day.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (loanId == null) "افزودن تسهیلات جدید" else "ویرایش تسهیلات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان تسهیلات") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = providerName,
                onValueChange = { providerName = it },
                label = { Text("نام بانک / تامین‌کننده") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = principalAmount,
                onValueChange = { if (it.all { char -> char.isDigit() }) principalAmount = it },
                label = { Text("مبلغ اصل وام (ریال)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = currencyVisualTransformation,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = installmentAmount,
                onValueChange = { if (it.all { char -> char.isDigit() }) installmentAmount = it },
                label = { Text("مبلغ هر قسط (ریال) - اختیاری") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = currencyVisualTransformation,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = installmentCount,
                onValueChange = { installmentCount = it },
                label = { Text("تعداد کل اقساط") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = loanId == null // جلوگیری از تغییر تعداد اقساط در ویرایش فعلاً
            )
            
            if (loanId == null) {
                OutlinedTextField(
                    value = paidInstallmentCount,
                    onValueChange = { paidInstallmentCount = it },
                    label = { Text("تعداد اقساط پرداخت شده از قبل") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ورودی تاریخ اولین سررسید
            Text(text = "تاریخ اولین سررسید (شمسی):", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startYear,
                    onValueChange = { if (it.length <= 4) startYear = it },
                    label = { Text("سال") },
                    modifier = Modifier.weight(1.5f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = loanId == null
                )
                OutlinedTextField(
                    value = startMonth,
                    onValueChange = { if (it.length <= 2) startMonth = it },
                    label = { Text("ماه") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = loanId == null
                )
                OutlinedTextField(
                    value = startDay,
                    onValueChange = { if (it.length <= 2) startDay = it },
                    label = { Text("روز") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = loanId == null
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("توضیحات (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Button(
                onClick = {
                    if (title.isNotBlank() && providerName.isNotBlank() && principalAmount.isNotBlank()) {
                        try {
                            val jalali = JalaliCalendar(startYear.toInt(), startMonth.toInt(), startDay.toInt())
                            val gregorian = jalali.toGregorian()
                            val firstDueDate = LocalDate.of(
                                gregorian.get(java.util.Calendar.YEAR),
                                gregorian.get(java.util.Calendar.MONTH) + 1,
                                gregorian.get(java.util.Calendar.DAY_OF_MONTH)
                            )

                            if (loanId == null) {
                                viewModel.addLoanWithPaidInstallments(
                                    loan = LoanFacility(
                                        title = title,
                                        providerName = providerName,
                                        facilityType = null,
                                        contractNumber = null,
                                        principalAmount = principalAmount.toDoubleOrNull() ?: 0.0,
                                        receivedAmount = null,
                                        interestRate = null,
                                        startDate = firstDueDate,
                                        endDate = null,
                                        installmentCount = installmentCount.toIntOrNull() ?: 0,
                                        installmentAmount = installmentAmount.toDoubleOrNull(),
                                        description = description,
                                        userId = "default_user"
                                    ),
                                    paidCount = paidInstallmentCount.toIntOrNull() ?: 0
                                )
                            } else {
                                existingLoan?.let {
                                    viewModel.updateLoan(
                                        it.copy(
                                            title = title,
                                            providerName = providerName,
                                            principalAmount = principalAmount.toDoubleOrNull() ?: 0.0,
                                            installmentAmount = installmentAmount.toDoubleOrNull(),
                                            description = description
                                        )
                                    )
                                }
                            }
                            onBack()
                        } catch (e: Exception) {
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && providerName.isNotBlank() && principalAmount.isNotBlank() && 
                          startYear.isNotBlank() && startMonth.isNotBlank() && startDay.isNotBlank()
            ) {
                Text(if (loanId == null) "ذخیره تسهیلات" else "بروزرسانی تغییرات")
            }
        }
    }
}

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedText = StringBuilder()
        for (i in originalText.indices) {
            formattedText.append(originalText[i])
            if ((originalText.length - i - 1) % 3 == 0 && i != originalText.length - 1) {
                formattedText.append(",")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var commasBefore = 0
                for (i in 0 until offset) {
                    if ((originalText.length - i - 1) % 3 == 0 && i != originalText.length - 1) {
                        commasBefore++
                    }
                }
                return offset + commasBefore
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                var originalOffset = 0
                var transformedOffset = 0
                while (transformedOffset < offset && originalOffset < originalText.length) {
                    transformedOffset++
                    if ((originalText.length - originalOffset - 1) % 3 == 0 && originalOffset != originalText.length - 1) {
                        transformedOffset++
                    }
                    originalOffset++
                }
                return originalOffset
            }
        }

        return TransformedText(AnnotatedString(formattedText.toString()), offsetMapping)
    }
}
