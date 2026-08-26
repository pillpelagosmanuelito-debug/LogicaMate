package com.educalab.logicamate.ui.screens.classification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.common.PieceView
import com.educalab.logicamate.ui.theme.CrystalTeal
import com.educalab.logicamate.ui.theme.EmberCoral
import com.educalab.logicamate.ui.theme.MossGreen
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.SurfaceCard
import com.educalab.logicamate.ui.theme.TextMuted
import com.educalab.logicamate.ui.theme.TextSecondary

private val PortalColors = listOf(RuneGold, CrystalTeal, EmberCoral, MossGreen)

@Composable
fun ClassificationScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.CLASSIFICATION,
        title = "Sala de Clasificación",
        onBack = onBack,
        mascotIntro = "Cada portal solo acepta un tipo de símbolo. Toca una pieza y luego un portal.",
    ) { challenge, onSubmit ->
        ClassificationBoard(challenge) { groups -> onSubmit(ChallengeValidator.Submission.Groups(groups)) }
    }
}

@Composable
fun ClassificationBoard(challenge: Challenge, onSubmit: (List<List<Int>>) -> Unit) {
    // assignment: índice de pieza -> índice de portal (0..3), o null si aún no se asignó.
    var assignment by remember(challenge.id) { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var selectedPieceIndex by remember(challenge.id) { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(challenge.prompt, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        Text(
            "Toca una pieza del cofre y luego el portal donde crees que pertenece.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )

        // Portales
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PortalColors.forEachIndexed { portalIndex, color ->
                val piecesHere = assignment.filterValues { it == portalIndex }.keys.toList()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(6.dp)
                        .then(
                            Modifier.clickableIfSelected(selectedPieceIndex != null) {
                                selectedPieceIndex?.let { idx ->
                                    assignment = assignment + (idx to portalIndex)
                                    selectedPieceIndex = null
                                }
                            },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Portal ${portalIndex + 1}", style = MaterialTheme.typography.labelSmall, color = color)
                    Row {
                        piecesHere.take(3).forEach { idx -> PieceView(challenge.optionPool[idx], boxSize = 32.dp) }
                    }
                    if (piecesHere.size > 3) Text("+${piecesHere.size - 3}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Text("Cofre de símbolos:", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 20.dp, bottom = 6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(challenge.optionPool.indices.filter { it !in assignment.keys }.toList()) { idx ->
                PieceView(
                    piece = challenge.optionPool[idx],
                    selected = idx == selectedPieceIndex,
                    onClick = { selectedPieceIndex = idx },
                )
            }
        }

        Button(
            onClick = {
                val groups = (0 until PortalColors.size).map { portalIndex ->
                    assignment.filterValues { it == portalIndex }.keys.toList()
                }
                onSubmit(groups)
            },
            enabled = assignment.size == challenge.optionPool.size,
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
        ) {
            Text("Comprobar clasificación")
        }
    }
}

private fun Modifier.clickableIfSelected(enabled: Boolean, onClick: () -> Unit): Modifier =
    if (enabled) this.then(androidx.compose.foundation.clickable(onClick = onClick)) else this
