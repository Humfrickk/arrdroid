package com.example.arrdroid.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.arrdroid.ui.theme.DarkBackground
import com.example.arrdroid.ui.theme.Lila
import com.example.arrdroid.ui.theme.Orange
import com.example.arrdroid.ui.theme.RobotoMono
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(800))
        delay(1200)
        alpha.animateTo(0f, animationSpec = tween(400))
        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Arrdroid",
            fontFamily = RobotoMono,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            modifier = Modifier.alpha(alpha.value),
            style = androidx.compose.ui.text.TextStyle(
                brush = Brush.linearGradient(colors = listOf(Orange, Lila))
            )
        )
    }
}

