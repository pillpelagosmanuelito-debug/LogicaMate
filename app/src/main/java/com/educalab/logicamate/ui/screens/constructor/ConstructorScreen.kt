package com.educalab.logicamate.ui.screens.constructor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.common.PieceView
import com.educalab.logicamate.ui.theme.StoneDeep
import com.educalab.logicamate.ui.theme.TextMuted
import com.educalab.logicamate.ui.theme.TextPrimary
import com.educalab.logicamate.ui.theme.TextSecondary

@Composable
fun ConstructorScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.CONSTRUCTOR,
        title = "Taller Constructor",
        onBack = onBack,
        mascotIntro = "Aquí no resuelves: construyes tú la regla. Toca piezas para añadirlas a tu tira.",
    ) { challenge, onSubmit ->
        ConstructorBoard(challenge) { built -> onSubmit(ChallengeValidator.Submission.Pieces(built)) }
    }
}

@Composable
fun ConstructorBoard(challenge: Challenge, onSubmit: (List<PieceSpec>) -> Unit) {
    var built by remember(challenge.id) { mutableStateOf<List<PieceSpec>>(emptyList()) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(challenge.prompt, style = MaterialTheme.typography.titleMedium, color = TextPrimary)

        Text("Tu tira construida:", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(StoneDeep)
                .padding(8.dp),
        ) {
            if (built.isEmpty()) {
                Text("Toca piezas del taller para empezar a construir...", color = TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(built.indices.toList()) { idx -> PieceView(built[idx], boxSize = 48.dp) }
                }
            }
        }

        Text("Piezas del taller:", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(challenge.optionPool) { option ->
                PieceView(piece = option, onClick = { built = built + option })
            }
        }

        Row(Modifier.padding(top = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { built = built.dropLast(1) }, enabled = built.isNotEmpty(), modifier = Modifier.weight(1f)) {
                Text("Quitar última")
            }
            OutlinedButton(onClick = { built = emptyList() }, enabled = built.isNotEmpty(), modifier = Modifier.weight(1f)) {
                Text("Vaciar tira")
            }
        }
        Button(
            onClick = { onSubmit(built) },
            enabled = built.size >= 3,
            modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
        ) {
            Text("Comprobar mi construcción")
        }
    }
}
