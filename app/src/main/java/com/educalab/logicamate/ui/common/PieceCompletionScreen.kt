package com.educalab.logicamate.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneDeep
import com.educalab.logicamate.ui.theme.SurfaceCard
import com.educalab.logicamate.ui.theme.TextPrimary
import com.educalab.logicamate.ui.theme.TextSecondary

enum class CompletionLayout { ROW_MOSAIC, PASSAGE, MECHANISM, GRID_MURAL }

/**
 * Un desafío "completa el hueco" (Patrones, Secuencias, Analogías, Matrices):
 * el niño elige piezas del banco y las coloca sobre el/los hueco(s) marcados
 * con "?" antes de comprobar. No es una lista de 4 botones de opción: el
 * banco de piezas se dibuja con su forma real y el niño manipula cuál va
 * en cada hueco.
 */
@Composable
fun PieceCompletionBoard(
    challenge: Challenge,
    layout: CompletionLayout,
    onSubmit: (List<PieceSpec>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedForBlank by remember(challenge.id) { mutableStateOf<PieceSpec?>(null) }
    val blankCount = challenge.items.count { it.isBlank }

    Column(modifier = modifier.fillMaxSize()) {
        Text(challenge.prompt, style = MaterialTheme.typography.titleMedium, color = TextPrimary, modifier = Modifier.padding(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(layoutBackground(layout))
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (layout) {
                CompletionLayout.MECHANISM -> AnalogyRow(challenge, selectedForBlank)
                CompletionLayout.GRID_MURAL -> MatrixGrid(challenge, selectedForBlank)
                else -> PassageOrMosaicRow(challenge, selectedForBlank)
            }
        }

        Text(
            "Elige la pieza correcta del banco:",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(challenge.optionPool) { option ->
                PieceView(
                    piece = option,
                    selected = option == selectedForBlank,
                    onClick = { selectedForBlank = option },
                )
            }
        }

        Button(
            onClick = { selectedForBlank?.let { onSubmit(List(blankCount) { _ -> it }) } },
            enabled = selectedForBlank != null,
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            Text("Colocar pieza")
        }
    }
}

@Composable
private fun PassageOrMosaicRow(challenge: Challenge, selected: PieceSpec?) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(challenge.items) { piece ->
            PieceView(piece = if (piece.isBlank && selected != null) selected else piece, boxSize = 52.dp)
        }
    }
}

@Composable
private fun AnalogyRow(challenge: Challenge, selected: PieceSpec?) {
    val items = challenge.items // [A, A', B, ?]
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Un mecanismo transforma esta pieza...", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Box(Modifier.padding(vertical = 8.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(listOf(items.getOrNull(0), null, items.getOrNull(1)).filterNotNull()) { p ->
                    if (p.isBlank) Text("→", style = MaterialTheme.typography.headlineMedium, color = RuneGold) else PieceView(p)
                }
            }
        }
        Text("...en esto. ¿Y esta otra pieza?", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Box(Modifier.padding(vertical = 8.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                val b = items.getOrNull(2)
                val blank = items.getOrNull(3)
                if (b != null) PieceView(b)
                Text("→", style = MaterialTheme.typography.headlineMedium, color = RuneGold)
                PieceView(if (selected != null) selected else (blank ?: PieceSpec.BLANK))
            }
        }
    }
}

@Composable
private fun MatrixGrid(challenge: Challenge, selected: PieceSpec?) {
    val cols = challenge.rule.params["cols"]?.toIntOrNull() ?: 2
    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(challenge.items) { piece ->
            PieceView(piece = if (piece.isBlank && selected != null) selected else piece, boxSize = 60.dp)
        }
    }
}

private fun layoutBackground(layout: CompletionLayout) = when (layout) {
    CompletionLayout.ROW_MOSAIC -> SurfaceCard
    CompletionLayout.PASSAGE -> StoneDeep
    CompletionLayout.MECHANISM -> SurfaceCard
    CompletionLayout.GRID_MURAL -> StoneDeep
}
