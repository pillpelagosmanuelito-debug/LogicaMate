package com.educalab.logicamate.ui.screens.patterns

import androidx.compose.runtime.Composable
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.common.CompletionLayout
import com.educalab.logicamate.ui.common.PieceCompletionBoard

@Composable
fun PatternsScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.PATTERNS,
        title = "Galería de Patrones",
        onBack = onBack,
        mascotIntro = "Los mosaicos del templo esconden ritmos que se repiten. Observa antes de elegir.",
    ) { challenge, onSubmit ->
        PieceCompletionBoard(
            challenge = challenge,
            layout = CompletionLayout.ROW_MOSAIC,
            onSubmit = { pieces -> onSubmit(ChallengeValidator.Submission.Pieces(pieces)) },
        )
    }
}
