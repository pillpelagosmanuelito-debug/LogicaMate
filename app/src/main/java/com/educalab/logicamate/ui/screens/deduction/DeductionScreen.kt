package com.educalab.logicamate.ui.screens.deduction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun DeductionScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.DEDUCTION,
        title = "Sala de Deducción",
        onBack = onBack,
        mascotIntro = "Investiga las pistas. Marca primero lo que descartan, no lo que confirman.",
    ) { challenge, onSubmit ->
        DeductionBoard(challenge) { assignment -> onSubmit(ChallengeValidator.Submission.Assignment(assignment)) }
    }
}

@Composable
fun DeductionBoard(challenge: Challenge, onSubmit: (Map<String, String>) -> Unit) {
    val people = remember(challenge.id) { challenge.rule.params.getValue("people").split(",") }
    val objects = remember(challenge.id) { challenge.rule.params.getValue("objects").split(",") }
    val clues = remember(challenge.id) { challenge.rule.params["clues"]?.split(". ")?.filter { it.isNotBlank() } ?: emptyList() }
    var assignment by remember(challenge.id) { mutableStateOf<Map<String, String>>(emptyMap()) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(challenge.prompt, style = MaterialTheme.typography.titleMedium, color = TextPrimary)

        Text("Pistas de la investigación:", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        clues.forEach { clue ->
            Text("• $clue.".replace("..", "."), style = MaterialTheme.typography.bodyMedium, color = TextMuted, modifier = Modifier.padding(vertical = 2.dp))
        }

        Text("Tablero de investigación:", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
        people.forEach { person ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .padding(10.dp),
            ) {
                Text(person, style = MaterialTheme.typography.titleMedium, color = RuneGold, fontWeight = FontWeight.Bold)
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    objects.forEach { obj ->
                        val isSelected = assignment[person] == obj
                        val takenByOther = assignment.any { it.key != person && it.value == obj }
                        Text(
                            text = obj,
                            color = if (isSelected) CrystalTeal else if (takenByOther) TextMuted.copy(alpha = 0.4f) else TextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CrystalTeal.copy(alpha = 0.2f) else StoneMuted.copy(alpha = 0.12f))
                                .clickable(enabled = !takenByOther) {
                                    assignment = assignment.filterValues { it != obj } + (person to obj)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onSubmit(assignment) },
            enabled = assignment.size == people.size,
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
        ) {
            Text("Presentar conclusión")
        }
    }
}
