package fr.charleselie.logique

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.charleselie.logique.components.*
import fr.charleselie.logique.screens.AboutScreen
import fr.charleselie.logique.ui.theme.LogiqueTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.runtime.collectAsState
import fr.charleselie.logique.data.UserPreferences
import fr.charleselie.logique.data.GuessState
import fr.charleselie.logique.data.Guess
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import fr.charleselie.logique.ui.theme.buttonHoverAnimation
import fr.charleselie.logique.ui.theme.enterTransition
import fr.charleselie.logique.ui.theme.exitTransition
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.AnimatedVisibility

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LogiqueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    var showAbout by remember { mutableStateOf(false) }
    var guessState by remember { mutableStateOf(GuessState()) }
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val bestStreak by userPreferences.bestStreak.collectAsState(initial = 0)
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = showAbout,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        AboutScreen(onBack = { showAbout = false })
    }

    AnimatedVisibility(
        visible = !showAbout,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAbout = true }
                ) {
                    Text(stringResource(R.string.about))
                }
                
                Button(
                    onClick = { 
                        guessState = if (guessState.isGuessing) {
                            GuessState()
                        } else {
                            GuessState(isGuessing = true, bestStreak = bestStreak)
                        }
                    },
                    colors = if (guessState.isGuessing) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
    Text(
                        if (guessState.isGuessing)
                            stringResource(R.string.quit_prediction_mode)
                        else
                            stringResource(R.string.prediction_mode)
                    )
                }
            }
            
            RandomGeneratorScreen(
                guessState = guessState,
                onGuessStateChange = { newState ->
                    guessState = newState
                    if (newState.bestStreak > bestStreak) {
                        coroutineScope.launch {
                            userPreferences.updateBestStreak(newState.bestStreak)
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RandomGeneratorScreen(
    guessState: GuessState,
    onGuessStateChange: (GuessState) -> Unit
) {
    val diceInteractionSource = remember { MutableInteractionSource() }
    val coinInteractionSource = remember { MutableInteractionSource() }
    val cardInteractionSource = remember { MutableInteractionSource() }
    
    val isDiceHovered by diceInteractionSource.collectIsHoveredAsState()
    val isCoinHovered by coinInteractionSource.collectIsHoveredAsState()
    val isCardHovered by cardInteractionSource.collectIsHoveredAsState()
    
    val diceButtonScale by animateFloatAsState(
        targetValue = if (isDiceHovered) 1.1f else 1f,
        animationSpec = buttonHoverAnimation,
        label = "dice_button_scale"
    )
    
    val coinButtonScale by animateFloatAsState(
        targetValue = if (isCoinHovered) 1.1f else 1f,
        animationSpec = buttonHoverAnimation,
        label = "coin_button_scale"
    )

    val generator = remember { RandomGenerator() }
    var result by remember { mutableStateOf<RandomResult?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    var showCustomizeDialog by remember { mutableStateOf<CustomizeType?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    
    val diceSides by userPreferences.diceSides.collectAsState(initial = 6)
    val coinTexts by userPreferences.coinTexts.collectAsState(initial = Pair("Pile", "Face"))
    val customCards by userPreferences.customCards.collectAsState(initial = emptySet())
    var currentGuess by remember { mutableStateOf<Guess?>(null) }
    var previousStreak by remember { mutableStateOf(0) }
    var showResultMessage by remember { mutableStateOf(false) }
    var bestStreak by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = guessState.isGuessing,
            enter = enterTransition(),
            exit = exitTransition()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.guess_streak, guessState.currentStreak),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.best_streak, guessState.bestStreak),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (result) {
                is RandomResult.DiceRoll -> {
                    AnimatedDice(
                        value = (result as RandomResult.DiceRoll).value,
                        isRolling = isAnimating
                    )
                }
                is RandomResult.CoinFlip -> {
                    AnimatedCoin(
                        isHeads = (result as RandomResult.CoinFlip).isHeads,
                        isFlipping = isAnimating
                    )
                }
                is RandomResult.CardDraw -> {
                    AnimatedCard(
                        card = (result as RandomResult.CardDraw).card,
                        isDrawing = isAnimating
                    )
                }
                null -> {
                    Text(
                        text = stringResource(R.string.initial_message),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (guessState.isGuessing && currentGuess == null) {
                        showCustomizeDialog = CustomizeType.DiceGuess(maxSides = diceSides)
                    } else {
                        coroutineScope.launch {
                            isAnimating = true
                            val newResult = generator.rollDice(diceSides)
                            result = newResult
                            
                            if (guessState.isGuessing && currentGuess != null) {
                                val guessValue = (currentGuess as? Guess.DiceGuess)?.value
                                val wasCorrect = guessValue == (result as? RandomResult.DiceRoll)?.value
                                
                                if (!wasCorrect) {
                                    previousStreak = guessState.currentStreak
                                }
                                
                                val newStreak = if (wasCorrect) guessState.currentStreak + 1 else 0
                                
                                val newGuessState = guessState.copy(
                                    currentStreak = newStreak,
                                    bestStreak = maxOf(bestStreak, newStreak),
                                    wasCorrect = wasCorrect
                                )
                                
                                bestStreak = maxOf(bestStreak, newStreak)
                                showResultMessage = true
                                onGuessStateChange(newGuessState)
                            }
                            currentGuess = null
                            
                            delay(1000)
                            isAnimating = false
                            
                            if (showResultMessage) {
                                delay(2000)
                                showResultMessage = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = diceButtonScale
                        scaleY = diceButtonScale
                    },
                interactionSource = diceInteractionSource,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                    hoveredElevation = 8.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (guessState.isGuessing && currentGuess == null) 
                        stringResource(R.string.guess_dice)
                    else 
                        stringResource(R.string.roll_dice)
                )
            }
            
            IconButton(
                onClick = { showCustomizeDialog = CustomizeType.Dice(diceSides) }
            ) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.customize))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (guessState.isGuessing && currentGuess == null) {
                        showCustomizeDialog = CustomizeType.CoinGuess
                    } else {
                        coroutineScope.launch {
                            isAnimating = true
                            val newResult = generator.flipCoin()
                            result = newResult
                            
                            if (guessState.isGuessing && currentGuess != null) {
                                val guessValue = (currentGuess as? Guess.CoinGuess)?.isHeads
                                val wasCorrect = guessValue == (result as? RandomResult.CoinFlip)?.isHeads
                                
                                if (!wasCorrect) {
                                    previousStreak = guessState.currentStreak
                                }
                                
                                val newStreak = if (wasCorrect) guessState.currentStreak + 1 else 0
                                
                                val newGuessState = guessState.copy(
                                    currentStreak = newStreak,
                                    bestStreak = maxOf(bestStreak, newStreak),
                                    wasCorrect = wasCorrect
                                )
                                
                                bestStreak = maxOf(bestStreak, newStreak)
                                showResultMessage = true
                                onGuessStateChange(newGuessState)
                            }
                            currentGuess = null
                            
                            delay(1000)
                            isAnimating = false
                            
                            if (showResultMessage) {
                                delay(2000)
                                showResultMessage = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = coinButtonScale
                        scaleY = coinButtonScale
                    },
                interactionSource = coinInteractionSource,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                    hoveredElevation = 8.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (guessState.isGuessing && currentGuess == null) 
                        stringResource(R.string.guess_coin)
                    else 
                        stringResource(R.string.flip_coin)
                )
            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    result = null
                    isAnimating = true
                    delay(375)
                    result = generator.drawCard()
                    delay(375)
                    isAnimating = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = if (isCardHovered) 1.1f else 1f
                    scaleY = if (isCardHovered) 1.1f else 1f
                },
            interactionSource = cardInteractionSource,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp,
                hoveredElevation = 8.dp
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.draw_card))
        }

        if (showResultMessage && guessState.isGuessing) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = if (guessState.wasCorrect) 
                        stringResource(R.string.correct)
                    else if (previousStreak > 0)
                        stringResource(R.string.incorrect_with_streak, previousStreak)
                    else
                        stringResource(R.string.incorrect_no_streak),
                    color = if (guessState.wasCorrect) Color.Green else Color.Red,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .background(
                            color = if (guessState.wasCorrect) 
                                Color.Green.copy(alpha = 0.1f)
                            else 
                                Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    showCustomizeDialog?.let { type ->
        CustomizeDialog(
            type = type,
            onDismiss = { showCustomizeDialog = null },
            onSave = { result ->
                when (type) {
                    is CustomizeType.DiceGuess -> {
                        if (result is Int) {
                            currentGuess = Guess.DiceGuess(result)
                            showResultMessage = false
                        }
                    }
                    is CustomizeType.Dice -> {
                        if (result is Int) {
                            coroutineScope.launch {
                                userPreferences.updateDiceSides(result)
                            }
                        }
                    }
                    is CustomizeType.Coin -> {
                        if (result is Pair<*, *> && result.first is String && result.second is String) {
                            coroutineScope.launch {
                                userPreferences.updateCoinTexts(
                                    result.first as String,
                                    result.second as String
                                )
                            }
                        }
                    }
                    is CustomizeType.Cards -> {
                        if (result is Set<*>) {
                            @Suppress("UNCHECKED_CAST")
                            val cards = result as Set<String>
                            coroutineScope.launch {
                                userPreferences.updateCustomCards(cards)
                            }
                        }
                    }
                    is CustomizeType.CoinGuess -> {
                        if (result is Boolean) {
                            currentGuess = Guess.CoinGuess(result)
                            showResultMessage = false
                        }
                    }
                    else -> {}
                }
                showCustomizeDialog = null
            }
        )
    }
}