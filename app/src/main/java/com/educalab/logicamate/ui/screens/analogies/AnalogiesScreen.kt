package com.educalab.logicamate.ui.screens.analogies

import androidx.compose.runtime.Composable
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.common.CompletionLayout
import com.educalab.logicamate.ui.common.PieceCompletionBoard

@Composable
fun AnalogiesScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.ANALOGIES,
        title = "Sala de Analogías",
        onBack = onBack,
        mascotIntro = "Un mecanismo transforma cada pieza siempre de la misma forma. Descúbrela.",
    ) { challenge, onSubmit ->
        PieceCompletionBoard(
            challenge = challenge,
            layout = CompletionLayout.MECHANISM,
            onSubmit = { pieces -> onSubmit(ChallengeValidator.Submission.Pieces(pieces)) },
        )
    }
}
