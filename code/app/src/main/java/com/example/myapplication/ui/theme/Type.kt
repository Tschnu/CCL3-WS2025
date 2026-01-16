package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

val Quicksand = FontFamily(
    Font(R.font.quicksand, FontWeight.Normal)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Quicksand,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Brown
    ),

    bodyMedium = TextStyle(
        fontFamily = Quicksand,
        fontSize = 14.sp,
        color = Brown
    ),
    titleLarge = TextStyle(
        fontFamily = Quicksand,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Brown
    ),
    titleMedium = TextStyle(
        fontFamily = Quicksand,
        fontSize = 18.sp,
        color = Brown
    ),
    labelLarge = TextStyle(
        fontFamily = Quicksand,
        fontSize = 14.sp,
        color = Brown
    ),
    labelSmall = TextStyle(
        fontFamily = Quicksand,
        fontSize = 12.sp,
        color = Brown
    )
)