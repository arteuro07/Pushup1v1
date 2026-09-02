package com.artemis.pushup1v1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onStartDuel: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("PUSHUP", fontSize = 46.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text("1v1", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
        Text("DUEL DE POMPES", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))

        Spacer(Modifier.height(28.dp))
        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Comment jouer", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("• Joueur 1 : 30 secondes
• Joueur 2 : 30 secondes
• Les pompes sont comptées automatiquement
• Le meilleur score gagne", modifier = Modifier.padding(top = 10.dp), lineHeight = 22.sp)
            }
        }
        Button(onClick = onStartDuel, Modifier.fillMaxWidth().padding(top = 26.dp)) {
            Text("Lancer le duel", fontSize = 17.sp)
        }
    }
}
