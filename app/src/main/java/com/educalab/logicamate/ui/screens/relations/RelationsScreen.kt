package com.educalab.logicamate.ui.screens.relations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.theme.CrystalTeal
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneMuted
import com.educalab.logicamate.ui.theme.SurfaceCard
import com.educalab.logicamate.ui.theme.TextMuted
import com.educalab.logicamate.ui.theme.TextPrimary
import com.educalab.logicamate.ui.theme.TextSecondary

@Composable
fun RelationsScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.RELATIONS,
        title = "Sala de Relaciones",
        onBack = onBack,
        mascotIntro = "Un mapa de posiciones espera. Toca a los exploradores en el orden correcto.",
    ) { challenge, onSubmit ->
        RelationsBoard(challenge) { order -> onSubmit(ChallengeValidator.Submission.Order(order)) }
    }
}

@Composable
fun RelationsBoard(challenge: Challenge, onSubmit: (List<String>) -> Unit) {
    val explorerNames = remember(challenge.id) { challenge.rule.params.getValue("items").split(",") }
    val clues = remember(challenge.id) { challenge.rule.params["clues"]?.split(". ")?.filter { it.isNotBlank() } ?: emptyList() }
    var order by remember(challenge.id) { mutableStateOf(listOf<String>()) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(challenge.prompt, style = MaterialTheme.typography.titleMedium, color = TextPrimary)

        Text("Pistas:", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        Column {
            clues.forEach { clue ->
                Text("• $clue.".replace("..", "."), style = MaterialTheme.typography.bodyMedium, color = TextMuted, modifier = Modifier.padding(vertical = 2.dp))
            }
        }

        Text("Camino de posiciones:", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (i in explorerNames.indices) {
                val name = order.getOrNull(i)
                Column(
                    modifier = Modifier
                        .size(width = 72.dp, height = 64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (name != null) RuneGold.copy(alpha = 0.25f) else StoneMuted.copy(alpha = 0.15f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("${i + 1}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(name ?: "—", style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Exploradores:", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(explorerNames.filter { it !in order }) { name ->
                Column(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CrystalTeal.copy(alpha = 0.25f))
                        .clickable { order = order + name }
                        .padding(14.dp),
                ) {
                    Text(name, color = CrystalTeal, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(Modifier.padding(top = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { order = order.dropLast(1) }, enabled = order.isNotEmpty(), modifier = Modifier.weight(1f)) {
                Text("Deshacer")
            }
            Button(
                onClick = { onSubmit(order) },
                enabled = order.size == explorerNames.size,
                modifier = Modifier.weight(1f),
            ) { Text("Comprobar orden") }
        }
    }
}
