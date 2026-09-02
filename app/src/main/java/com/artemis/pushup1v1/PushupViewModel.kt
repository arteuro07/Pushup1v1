package com.artemis.pushup1v1

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PushupViewModel : ViewModel() {
    enum class Phase { IDLE, PLAYER1_TURN, WAITING_FOR_PLAYER2, PLAYER2_TURN, RESULT }

    var phase by mutableStateOf(Phase.IDLE)
        private set
    var player1Reps by mutableIntStateOf(0)
        private set
    var player2Reps by mutableIntStateOf(0)
        private set
    var secondsLeft by mutableIntStateOf(DURATION_SECONDS)
        private set
    var isDown by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    fun startPlayer1() {
        startTurn(1)
    }

    fun startPlayer2() {
        startTurn(2)
    }

    private fun startTurn(player: Int) {
        timerJob?.cancel()
        secondsLeft = DURATION_SECONDS
        isDown = false
        if (player == 1) player1Reps = 0 else player2Reps = 0
        phase = if (player == 1) Phase.PLAYER1_TURN else Phase.PLAYER2_TURN

        timerJob = viewModelScope.launch {
            for (remaining in DURATION_SECONDS downTo 1) {
                secondsLeft = remaining
                delay(1_000)
            }
            secondsLeft = 0
            if (player == 1) {
                phase = Phase.WAITING_FOR_PLAYER2
            } else {
                phase = Phase.RESULT
            }
        }
    }

    fun registerRep() {
        when (phase) {
            Phase.PLAYER1_TURN -> player1Reps++
            Phase.PLAYER2_TURN -> player2Reps++
            else -> Unit
        }
    }

    fun setDownState(down: Boolean) {
        if (phase == Phase.PLAYER1_TURN || phase == Phase.PLAYER2_TURN) {
            isDown = down
        }
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        player1Reps = 0
        player2Reps = 0
        secondsLeft = DURATION_SECONDS
        isDown = false
        phase = Phase.IDLE
    }

    fun winnerText(): String = when {
        player1Reps > player2Reps -> "Joueur 1 gagne !"
        player2Reps > player1Reps -> "Joueur 2 gagne !"
        else -> "Égalité !"
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        const val DURATION_SECONDS = 30
    }
}
