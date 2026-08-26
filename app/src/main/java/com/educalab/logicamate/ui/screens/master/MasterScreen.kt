package com.educalab.logicamate.ui.screens.master

import androidx.compose.runtime.Composable
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.LogicCategory
import com.educalab.logicamate.ui.common.ChamberScreenScaffold
import com.educalab.logicamate.ui.common.CompletionLayout
import com.educalab.logicamate.ui.common.PieceCompletionBoard
import com.educalab.logicamate.ui.screens.classification.ClassificationBoard
import com.educalab.logicamate.ui.screens.constructor.ConstructorBoard
import com.educalab.logicamate.ui.screens.deduction.DeductionBoard
import com.educalab.logicamate.ui.screens.relations.RelationsBoard

/**
 * La Cámara Maestra no tiene una mecánica propia: mezcla las ocho
 * categorías de contenido a dificultad avanzada (sección 18/40). El niño no
 * sabe de antemano qué tipo de razonamiento le tocará — decide la
 * estrategia al ver cada desafío, exactamente como pide la sección 18.
 */
@Composable
fun MasterScreen(onBack: () -> Unit) {
    ChamberScreenScaffold(
        chamberId = ChamberId.MASTER,
        title = "Cámara Maestra",
        onBack = onBack,
        mascotIntro = "Aquí no te digo qué tipo de reto es. Usa todo lo que has aprendido en el templo.",
    ) { challenge, onSubmit ->
        when (challenge.category) {
            LogicCategory.PATTERN -> PieceCompletionBoard(challenge, CompletionLayout.ROW_MOSAIC) { onSubmit(ChallengeValidator.Submission.Pieces(it)) }
            LogicCategory.SEQUENCE -> PieceCompletionBoard(challenge, CompletionLayout.PASSAGE) { onSubmit(ChallengeValidator.Submission.Pieces(it)) }
            LogicCategory.ANALOGY -> PieceCompletionBoard(challenge, CompletionLayout.MECHANISM) { onSubmit(ChallengeValidator.Submission.Pieces(it)) }
            LogicCategory.MATRIX -> PieceCompletionBoard(challenge, CompletionLayout.GRID_MURAL) { onSubmit(ChallengeValidator.Submission.Pieces(it)) }
            LogicCategory.CLASSIFICATION -> ClassificationBoard(challenge) { onSubmit(ChallengeValidator.Submission.Groups(it)) }
            LogicCategory.RELATION -> RelationsBoard(challenge) { onSubmit(ChallengeValidator.Submission.Order(it)) }
            LogicCategory.DEDUCTION -> DeductionBoard(challenge) { onSubmit(ChallengeValidator.Submission.Assignment(it)) }
            LogicCategory.CONSTRUCTION -> ConstructorBoard(challenge) { onSubmit(ChallengeValidator.Submission.Pieces(it)) }
        }
    }
}
