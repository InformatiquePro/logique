package fr.charleselie.logique.data

data class GuessState(
    val isGuessing: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val showResult: Boolean = false,
    val wasCorrect: Boolean = false
)

sealed class Guess {
    data class DiceGuess(val value: Int) : Guess()
    data class CoinGuess(val isHeads: Boolean) : Guess()
    data class CardGuess(val card: String) : Guess()
} 