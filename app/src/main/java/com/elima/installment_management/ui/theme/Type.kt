package com.elima.installment_management.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.elima.installment_management.R

// تعریف FontFamily برای ایران سنس
val IranSans = FontFamily(
    Font(R.font.iransans_light, FontWeight.Light),
    Font(R.font.iransans, FontWeight.Normal),
    Font(R.font.iransans_medium, FontWeight.Medium),
    Font(R.font.iransans_bold, FontWeight.Bold),
    Font(R.font.iransans_black, FontWeight.Black)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = IranSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = IranSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = IranSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = IranSans,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = IranSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
