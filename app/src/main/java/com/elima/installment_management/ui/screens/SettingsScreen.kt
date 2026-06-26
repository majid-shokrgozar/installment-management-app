package com.elima.installment_management.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elima.installment_management.data.SettingsManager
import com.elima.installment_management.util.BackupManager
import com.elima.installment_management.worker.NotificationScheduler
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onThemeChanged: (Int) -> Unit = {}) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
    var themeMode by remember { mutableIntStateOf(settingsManager.themeMode) }
    var daysBefore by remember { mutableIntStateOf(settingsManager.notificationDaysBefore) }
    var hour by remember { mutableIntStateOf(settingsManager.notificationHour) }
    var minute by remember { mutableIntStateOf(settingsManager.notificationMinute) }
    
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    // Launcher برای ذخیره فایل بک‌آپ
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            val success = BackupManager.exportDatabase(context, it)
            if (success) {
                Toast.makeText(context, "فایل بک‌آپ با موفقیت ذخیره شد. می‌توانید آن را در Google Drive آپلود کنید.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "خطا در تهیه بک‌آپ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher برای انتخاب فایل بک‌آپ
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val success = BackupManager.importDatabase(context, it)
            if (success) {
                Toast.makeText(context, "اطلاعات با موفقیت بازیابی شد. برنامه در حال بازنشانی است...", Toast.LENGTH_LONG).show()
                
                // ری‌استور موفقیت‌آمیز بود، برنامه را ری‌استارت می‌کنیم تا دیتابیس جدید لود شود
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
                
                // بستن پروسه فعلی
                (context as? android.app.Activity)?.finish()
                Runtime.getRuntime().exit(0)
            } else {
                Toast.makeText(context, "خطا در بازیابی اطلاعات", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "تنظیمات",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // بخش ظاهر
        SettingsCard(title = "ظاهر برنامه") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "تم برنامه:")
                }
                
                var expanded by remember { mutableStateOf(false) }
                val themeOptions = listOf(0, 1, 2)
                val themeLabels = mapOf(0 to "پیش‌فرض سیستم", 1 to "روشن", 2 to "تیره")

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(text = themeLabels[themeMode] ?: "پیش‌فرض سیستم")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        themeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(themeLabels[option] ?: "") },
                                onClick = {
                                    themeMode = option
                                    settingsManager.themeMode = option
                                    onThemeChanged(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // بخش یادآور
        SettingsCard(title = "تنظیمات یادآور") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "یادآوری قبل از موعد:")
                
                var expanded by remember { mutableStateOf(false) }
                val options = listOf(0, 1, 2, 3, 5, 7)
                val labels = mapOf(0 to "همان روز", 1 to "۱ روز قبل", 2 to "۲ روز قبل", 3 to "۳ روز قبل", 5 to "۵ روز قبل", 7 to "۱ هفته قبل")

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(text = labels[daysBefore] ?: "$daysBefore روز قبل")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(labels[option] ?: "$option روز قبل") },
                                onClick = {
                                    daysBefore = option
                                    settingsManager.notificationDaysBefore = option
                                    NotificationScheduler.scheduleDailyNotification(context)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ساعت اعلان:")
                TextButton(onClick = { showTimePicker = true }) {
                    Text(text = String.format(Locale.US, "%02d:%02d", hour, minute))
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            val notificationHelper = remember { com.elima.installment_management.util.NotificationHelper(context) }
            OutlinedButton(
                onClick = {
                    notificationHelper.createNotificationChannel()
                    notificationHelper.showNotification(
                        id = 999,
                        title = "تست اعلان",
                        message = "این یک اعلان آزمایشی است. اگر آن را می‌بینید، یعنی سیستم اعلان فعال است."
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ارسال اعلان آزمایشی")
            }
        }

        // بخش پشتیبان‌گیری
        SettingsCard(title = "پشتیبان‌گیری و بازیابی") {
            Text(
                text = "می‌توانید از اطلاعات خود نسخه پشتیبان تهیه کنید و آن را در Google Drive ذخیره نمایید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { exportLauncher.launch("loans_backup_${System.currentTimeMillis()}.db") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تهیه بک‌آپ")
                }
                
                OutlinedButton(
                    onClick = { importLauncher.launch("*/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بازیابی")
                }
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            hour = timePickerState.hour
                            minute = timePickerState.minute
                            settingsManager.notificationHour = hour
                            settingsManager.notificationMinute = minute
                            NotificationScheduler.scheduleDailyNotification(context)
                            showTimePicker = false
                        }
                    ) { Text("تایید") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("انصراف") }
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = { content() },
        modifier = Modifier.fillMaxWidth()
    )
}
