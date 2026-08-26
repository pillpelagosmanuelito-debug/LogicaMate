package com.educalab.logicamate.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneDeep
import com.educalab.logicamate.ui.theme.TextPrimary
import com.educalab.logicamate.ui.theme.TextSecondary

@Composable
fun ChamberScreenScaffold(
    chamberId: ChamberId,
    title: String,
    onBack: () -> Unit,
    mascotIntro: String,
    content: @Composable (challenge: Challenge, onSubmit: (ChallengeValidator.Submission) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ChamberViewModel = viewModel(
        factory = ChamberViewModel.Factory(context.applicationContext as android.app.Application, chamberId),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(StoneDeep)) {
        ChamberTopBar(title = title, onBack = onBack)

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RuneGold)
            }
            state.chamberComplete -> ChamberCompleteView(onBack)
            state.currentChallenge != null -> {
                val challenge = state.currentChallenge!!
                MascotLine(if (state.queueIndex == 0) mascotIntro else "¡Sigamos! Observa antes de tocar nada.")
                Box(Modifier.weight(1f)) {
                    content(challenge) { submission -> viewModel.submit(submission) }
                }
                ProgressiveHintPanel(
                    hints = challenge.hints,
                    revealedLevels = state.revealedHintLevels,
                    onRevealNext = { viewModel.revealNextHint() },
                )
                Text(
                    "Desafío ${state.queueIndex + 1} de ${state.queue.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp),
                )
            }
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Esta cámara todavía no tiene desafíos.", color = TextSecondary)
            }
        }

        FeedbackBanner(
            kind = state.feedback,
            message = buildString {
                append(state.feedbackMessage)
                if (state.feedback == FeedbackKind.CORRECT) {
                    append(" (+${state.lastXpAwarded} XP)")
                    if (state.fragmentUnlocked) append(" · ¡Fragmento de la Llave obtenido!")
                    if (state.newlyUnlockedBadgeNames.isNotEmpty()) append(" · ¡Nueva insignia desbloqueada!")
                }
            },
            onDismiss = { viewModel.dismissFeedbackAndAdvance() },
        )
    }
}

@Composable
private fun ChamberCompleteView(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("¡Cámara completada!", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
        Text(
            "El templo reacciona ante tu logro. Vuelve al mapa para ver tu progreso.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )
        Button(onClick = onBack) { Text("Volver al mapa") }
    }
}
