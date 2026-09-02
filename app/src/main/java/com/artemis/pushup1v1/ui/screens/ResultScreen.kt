package com.artemis.pushup1v1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artemis.pushup1v1.PushupViewModel

@Composable
fun ResultScreen(viewModel: PushupViewModel, onRematch: () -> Unit, onHome: () -> Unit) {
    val p1 = viewModel.player1Reps
    val p2 = viewModel.player2Reps
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("RÉSULTAT", color = MaterialTheme.colorScheme.secondary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(viewModel.winnerText(), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
        Row(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Score("JOUEUR 1", p1)
            Text("VS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Score("JOUEUR 2", p2)
        }
        Button(onClick = onRematch, Modifier.fillMaxWidth()) { Text("Revanche") }
        OutlinedButton(onClick = onHome, Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Accueil") }
    }
}

@Composable
private fun Score(label: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("$score", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text("pompes", fontSize = 13.sp)
    }
}
