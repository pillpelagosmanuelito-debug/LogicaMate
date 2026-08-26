package com.educalab.logicamate.ui.screens.sequences

import androidx.compose.runtime.Composable
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.common.CompletionLayout
import com.educalab.logicamate.ui.common.PieceCompletionBoard

@Composable
fun SequencesScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.SEQUENCES,
        title = "Pasadizo de Secuencias",
        onBack = onBack,
        mascotIntro = "Un pasadizo de casillas avanza siguiendo una regla. ¿Cuál es?",
    ) { challenge, onSubmit ->
        PieceCompletionBoard(
            challenge = challenge,
            layout = CompletionLayout.PASSAGE,
            onSubmit = { pieces -> onSubmit(ChallengeValidator.Submission.Pieces(pieces)) },
        )
    }
}
