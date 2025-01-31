package fr.charleselie.logique.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import fr.charleselie.logique.R

sealed class CustomizeType {
    data class Dice(val currentSides: Int) : CustomizeType()
    data class Coin(val heads: String, val tails: String) : CustomizeType()
    data class Cards(val currentCards: Set<String>) : CustomizeType()
    data class DiceGuess(val maxSides: Int) : CustomizeType()
    object CoinGuess : CustomizeType()
    object CardGuess : CustomizeType()
}

@Composable
fun CustomizeDialog(
    type: CustomizeType,
    onDismiss: () -> Unit,
    onSave: (Any) -> Unit
) {
    var diceSides by remember { mutableStateOf("") }
    var heads by remember { mutableStateOf("") }
    var tails by remember { mutableStateOf("") }
    var newCard by remember { mutableStateOf("") }
    var customCards by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(type) {
        when (type) {
            is CustomizeType.Dice -> diceSides = type.currentSides.toString()
            is CustomizeType.Coin -> {
                heads = type.heads
                tails = type.tails
            }
            is CustomizeType.Cards -> customCards = type.currentCards
            is CustomizeType.DiceGuess -> {
                diceSides = ""
            }
            is CustomizeType.CoinGuess -> {}
            is CustomizeType.CardGuess -> {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (type) {
                    is CustomizeType.Dice -> stringResource(R.string.custom_dice)
                    is CustomizeType.Coin -> stringResource(R.string.custom_coin)
                    is CustomizeType.Cards -> stringResource(R.string.custom_card)
                    is CustomizeType.DiceGuess -> stringResource(R.string.guess_dice)
                    is CustomizeType.CoinGuess -> stringResource(R.string.guess_coin)
                    is CustomizeType.CardGuess -> stringResource(R.string.guess_card)
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (type) {
                    is CustomizeType.Dice -> {
                        OutlinedTextField(
                            value = diceSides,
                            onValueChange = { diceSides = it },
                            label = { Text(stringResource(R.string.dice_sides)) }
                        )
                    }
                    is CustomizeType.Coin -> {
                        OutlinedTextField(
                            value = heads,
                            onValueChange = { heads = it },
                            label = { Text(stringResource(R.string.coin_heads)) }
                        )
                        OutlinedTextField(
                            value = tails,
                            onValueChange = { tails = it },
                            label = { Text(stringResource(R.string.coin_tails)) }
                        )
                    }
                    is CustomizeType.Cards -> {
                        customCards.forEach { card ->
                            Text(card)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCard,
                                onValueChange = { newCard = it },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newCard.isNotBlank()) {
                                        customCards = customCards + newCard
                                        newCard = ""
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.add_card))
                            }
                        }
                    }
                    is CustomizeType.DiceGuess -> {
                        OutlinedTextField(
                            value = diceSides,
                            onValueChange = { 
                                val number = it.toIntOrNull()
                                if (number != null && number in 1..type.maxSides) {
                                    diceSides = it
                                }
                            },
                            label = { Text(stringResource(R.string.make_guess)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    is CustomizeType.CoinGuess -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { onSave(true) }
                            ) {
                                Text("Pile")
                            }
                            Button(
                                onClick = { onSave(false) }
                            ) {
                                Text("Face")
                            }
                        }
                    }
                    is CustomizeType.CardGuess -> {
                        // À implémenter plus tard
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (type) {
                        is CustomizeType.Dice -> {
                            diceSides.toIntOrNull()?.let { sides ->
                                onSave(sides)
                            }
                        }
                        is CustomizeType.Coin -> {
                            if (heads.isNotBlank() && tails.isNotBlank()) {
                                onSave(Pair(heads, tails))
                            }
                        }
                        is CustomizeType.Cards -> {
                            if (customCards.isNotEmpty()) {
                                onSave(customCards)
                            }
                        }
                        is CustomizeType.DiceGuess -> {
                            diceSides.toIntOrNull()?.let { guess ->
                                if (guess in 1..type.maxSides) {
                                    onSave(guess)
                                }
                            }
                        }
                        is CustomizeType.CoinGuess -> {
                            // À implémenter plus tard
                        }
                        is CustomizeType.CardGuess -> {
                            // À implémenter plus tard
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 