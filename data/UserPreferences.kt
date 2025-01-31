package fr.charleselie.logique.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    companion object {
        val DICE_SIDES = intPreferencesKey("dice_sides")
        val COIN_HEADS = stringPreferencesKey("coin_heads")
        val COIN_TAILS = stringPreferencesKey("coin_tails")
        val CUSTOM_CARDS = stringSetPreferencesKey("custom_cards")
        val BEST_STREAK = intPreferencesKey("best_streak")
    }

    val diceSides: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DICE_SIDES] ?: 6
    }

    val coinTexts: Flow<Pair<String, String>> = context.dataStore.data.map { preferences ->
        Pair(
            preferences[COIN_HEADS] ?: "Pile",
            preferences[COIN_TAILS] ?: "Face"
        )
    }

    val customCards: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_CARDS] ?: emptySet()
    }

    val bestStreak: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[BEST_STREAK] ?: 0
    }

    suspend fun updateDiceSides(sides: Int) {
        context.dataStore.edit { preferences ->
            preferences[DICE_SIDES] = sides
        }
    }

    suspend fun updateCoinTexts(heads: String, tails: String) {
        context.dataStore.edit { preferences ->
            preferences[COIN_HEADS] = heads
            preferences[COIN_TAILS] = tails
        }
    }

    suspend fun updateCustomCards(cards: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_CARDS] = cards
        }
    }

    suspend fun updateBestStreak(streak: Int) {
        context.dataStore.edit { preferences ->
            preferences[BEST_STREAK] = streak
        }
    }
} 