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
                    var showAbout by remember { mutableStateOf(false) }
                    var guessState by remember { mutableStateOf(GuessState()) }
                    val context = LocalContext.current
                    val userPreferences = remember { UserPreferences(context) }
                    val bestStreak by userPreferences.bestStreak.collectAsState(initial = 0)
                    val coroutineScope = rememberCoroutineScope()

                    if (showAbout) {
                        AboutScreen(onBack = { showAbout = false })
                    } else {
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
            }
        }
    }
}

@Composable
fun RandomGeneratorScreen(
    guessState: GuessState,
    onGuessStateChange: (GuessState) -> Unit
) {
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
            .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (guessState.isGuessing) {
            Text(
                text = stringResource(R.string.guess_streak, guessState.currentStreak),
                fontSize = 18.sp
            )
            Text(
                text = stringResource(R.string.best_streak, guessState.bestStreak),
                fontSize = 14.sp
            )
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
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
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
                    isAnimating = true
                    result = generator.drawCard()
                    delay(500)
                    isAnimating = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.draw_card))
        }

        if (showResultMessage && guessState.isGuessing) {
            Text(
                text = if (guessState.wasCorrect) 
                    stringResource(R.string.correct)
                else if (previousStreak > 0)
                    stringResource(R.string.incorrect_with_streak, previousStreak)
                else
                    stringResource(R.string.incorrect_no_streak),
                color = if (guessState.wasCorrect) Color.Green else Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
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