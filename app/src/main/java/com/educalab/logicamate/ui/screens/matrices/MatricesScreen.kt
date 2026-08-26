package com.educalab.logicamate.ui.screens.matrices

import androidx.compose.runtime.Composable
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.common.CompletionLayout
import com.educalab.logicamate.ui.common.PieceCompletionBoard

@Composable
fun MatricesScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.MATRICES,
        title = "Sala de Matrices",
        onBack = onBack,
        mascotIntro = "Un panel mural tiene una pieza que falta. Compara fila con fila.",
    ) { challenge, onSubmit ->
        PieceCompletionBoard(
            challenge = challenge,
            layout = CompletionLayout.GRID_MURAL,
            onSubmit = { pieces -> onSubmit(ChallengeValidator.Submission.Pieces(pieces)) },
        )
    }
}
