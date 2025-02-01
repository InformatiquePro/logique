package fr.charleselie.logique.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun AnimatedDice(value: Int, isRolling: Boolean) {
    var currentRotation by remember { mutableStateOf(0f) }
    var displayedValue by remember { mutableStateOf(value) }
    val maxValue = remember(value) { if (value < 1) 1 else value }
    
    val rotation = animateFloatAsState(
        targetValue = if (isRolling) currentRotation + 360f * 5 else currentRotation,
        animationSpec = tween(1000),
        label = "dice_rotation"
    )

    LaunchedEffect(isRolling) {
        if (isRolling) {
            currentRotation += 360f * 5
            // Animation des nombres pendant le roulement
            repeat(10) {
                displayedValue = (1..maxValue).random()
                delay(100)
            }
            displayedValue = value
        }
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .rotate(rotation.value),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.2f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(10f, 10f),
                topLeft = Offset(2f, 2f)
            )
            drawRoundRect(
                color = Color.White,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }
        Text(
            text = displayedValue.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun AnimatedCoin(isHeads: Boolean, isFlipping: Boolean) {
    var currentRotation by remember { mutableStateOf(0f) }
    var showResult by remember(isFlipping) { mutableStateOf(!isFlipping) }
    val rotation = animateFloatAsState(
        targetValue = if (isFlipping) currentRotation + 360f * 3 else currentRotation,
        animationSpec = tween(1000),
        label = "coin_rotation"
    )

    LaunchedEffect(isFlipping) {
        if (isFlipping) {
            showResult = false
            currentRotation += 360f * 3
            delay(1000)
            showResult = true
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(1f, if (rotation.value % 360 > 90 && rotation.value % 360 < 270) -1f else 1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = size.minDimension / 2
                )
            }
            Text(
                text = if (isHeads) "P" else "F",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isHeads) "Pile" else "Face",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.alpha(if (!isFlipping || showResult) 1f else 0f)
        )
    }
}

@Composable
fun AnimatedCard(card: String, isDrawing: Boolean) {
    Box(
        modifier = Modifier
            .size(120.dp, 180.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(15.dp),
            color = if (!isDrawing) Color.White else Color(0xFF1E90FF)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!isDrawing) {
                    // Face avec le résultat
                    Text(
                        text = card,
                        fontSize = 40.sp,
                        color = if (card.contains("♥") || card.contains("♦")) 
                            Color.Red else Color.Black
                    )
                } else {
                    // Face bleue avec motif
                    Canvas(modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                    ) {
                        drawRect(
                            color = Color.White,
                            style = Stroke(width = 4f)
                        )
                        drawRect(
                            color = Color.White,
                            style = Stroke(width = 2f),
                            size = Size(size.width * 0.8f, size.height * 0.8f),
                            topLeft = Offset(size.width * 0.1f, size.height * 0.1f)
                        )
                    }
                }
            }
        }
    }
} 