package com.educalab.logicamate.ui.screens.entrance

import androidx.compose.runtime.Composable
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.common.CompletionLayout
import com.educalab.logicamate.ui.common.PieceCompletionBoard

@Composable
fun EntranceScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.ENTRANCE,
        title = "Entrada al Templo",
        onBack = onBack,
        mascotIntro = "Bienvenido, explorador. Observa los símbolos de la puerta antes de tocar nada.",
    ) { challenge, onSubmit ->
        PieceCompletionBoard(
            challenge = challenge,
            layout = CompletionLayout.ROW_MOSAIC,
            onSubmit = { pieces -> onSubmit(ChallengeValidator.Submission.Pieces(pieces)) },
        )
    }
}
