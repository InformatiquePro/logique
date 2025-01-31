package fr.charleselie.logique

import kotlin.random.Random

sealed class RandomResult {
    data class DiceRoll(val value: Int) : RandomResult()
    data class CoinFlip(val isHeads: Boolean) : RandomResult()
    data class CardDraw(val card: String) : RandomResult()
}

class RandomGenerator {
    fun rollDice(sides: Int = 6): RandomResult.DiceRoll {
        val actualSides = maxOf(1, sides)
        return RandomResult.DiceRoll((1..actualSides).random())
    }

    fun flipCoin(): RandomResult.CoinFlip {
        return RandomResult.CoinFlip(Random.nextBoolean())
    }

    fun drawCard(): RandomResult.CardDraw {
        val suits = listOf("♠", "♥", "♦", "♣")
        val values = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        return RandomResult.CardDraw("${values.random()}${suits.random()}")
    }
} 