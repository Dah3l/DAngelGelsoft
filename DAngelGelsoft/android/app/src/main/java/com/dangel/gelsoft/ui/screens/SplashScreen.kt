package com.dangel.gelsoft.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated splash: the logo scales in with a fade while the slogan
 * appears. Short and elegant (~1.6 s), then [onDone].
 */
@Composable
fun SplashScreen(slogan: String, onDone: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
        launch {
            alpha.animateTo(1f, tween(700))
        }
        delay(500)
        textAlpha.animateTo(1f, tween(600))
        delay(700)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_dangel),
                contentDescription = "D'Angel Gelsoft",
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = slogan,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha.value),
            )
        }
    }
}
