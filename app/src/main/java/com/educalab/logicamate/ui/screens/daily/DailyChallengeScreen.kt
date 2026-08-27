package com.educalab.logicamate.ui.screens.daily

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.logicamate.ServiceLocator
import com.educalab.logicamate.data.local.entity.DailyChallengeEntity
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.engine.DailyChallengeGenerator
import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.domain.model.LogicCategory
import com.educalab.logicamate.ui.common.ChamberTopBar
import com.educalab.logicamate.ui.common.CompletionLayout
import com.educalab.logicamate.ui.common.FeedbackBanner
import com.educalab.logicamate.ui.common.FeedbackKind
import com.educalab.logicamate.ui.common.MascotLine
import com.educalab.logicamate.ui.common.PieceCompletionBoard
import com.educalab.logicamate.ui.screens.classification.ClassificationBoard
import com.educalab.logicamate.ui.screens.deduction.DeductionBoard
import com.educalab.logicamate.ui.screens.relations.RelationsBoard
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneDeep
import com.educalab.logicamate.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyUiState(
    val isLoading: Boolean = true,
    val challenge: Challenge? = null,
    val alreadyCompleted: Boolean = false,
    val feedback: FeedbackKind? = null,
    val feedbackMessage: String = "",
)

class DailyViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(DailyUiState())
    val state: StateFlow<DailyUiState> = _state.asStateFlow()
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    init {
        viewModelScope.launch {
            val db = ServiceLocator.database(getApplication())
            val existing = db.dailyChallengeDao().getForDate(today)
            val challenge = DailyChallengeGenerator.generateFor(today)
            if (existing == null) {
                db.dailyChallengeDao().upsert(DailyChallengeEntity(date = today, challengeId = challenge.id, completed = false, completedAtMillis = null))
            }
            _state.value = DailyUiState(isLoading = false, challenge = challenge, alreadyCompleted = existing?.completed == true)
        }
    }

    fun submit(submission: ChallengeValidator.Submission) {
        val challenge = _state.value.challenge ?: return
        val isCorrect = try { ChallengeValidator.validateAny(challenge, submission) } catch (e: Exception) { false }
        viewModelScope.launch {
            if (isCorrect) {
                val db = ServiceLocator.database(getApplication())
                db.dailyChallengeDao().upsert(DailyChallengeEntity(date = today, challengeId = challenge.id, completed = true, completedAtMillis = System.currentTimeMillis()))
            }
            _state.value = _state.value.copy(
                feedback = if (isCorrect) FeedbackKind.CORRECT else FeedbackKind.INCORRECT,
                feedbackMessage = if (isCorrect) challenge.explanation else "Todavía no. Repasa el reto y vuelve a intentarlo.",
                alreadyCompleted = _state.value.alreadyCompleted || isCorrect,
            )
        }
    }

    fun dismiss() { _state.value = _state.value.copy(feedback = null) }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DailyViewModel(app) as T
    }
}

@Composable
fun DailyChallengeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: DailyViewModel = viewModel(factory = DailyViewModel.Factory(context.applicationContext as Application))
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(StoneDeep).navigationBarsPadding()) {
        ChamberTopBar(title = "Reto Diario", onBack = onBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RuneGold) }
            state.alreadyCompleted && state.feedback == null -> Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¡Ya completaste el reto de hoy!", style = MaterialTheme.typography.headlineMedium, color = TextSecondary, modifier = Modifier.padding(top = 40.dp))
                Text("Vuelve mañana para un nuevo desafío del templo.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
            }
            state.challenge != null -> {
                val challenge = state.challenge!!
                MascotLine("Un reto distinto cada día. Nada de vidas ni esperas: inténtalo cuando quieras.")
                Box(Modifier.weight(1f)) {
                    when (challenge.category) {
                        LogicCategory.SEQUENCE -> PieceCompletionBoard(challenge, CompletionLayout.PASSAGE) { viewModel.submit(ChallengeValidator.Submission.Pieces(it)) }
                        LogicCategory.PATTERN -> PieceCompletionBoard(challenge, CompletionLayout.ROW_MOSAIC) { viewModel.submit(ChallengeValidator.Submission.Pieces(it)) }
                        LogicCategory.MATRIX -> PieceCompletionBoard(challenge, CompletionLayout.GRID_MURAL) { viewModel.submit(ChallengeValidator.Submission.Pieces(it)) }
                        LogicCategory.CLASSIFICATION -> ClassificationBoard(challenge) { viewModel.submit(ChallengeValidator.Submission.Groups(it)) }
                        LogicCategory.RELATION -> RelationsBoard(challenge) { viewModel.submit(ChallengeValidator.Submission.Order(it)) }
                        LogicCategory.DEDUCTION -> DeductionBoard(challenge) { viewModel.submit(ChallengeValidator.Submission.Assignment(it)) }
                        else -> Text("Reto no disponible hoy.", color = TextSecondary, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
        FeedbackBanner(kind = state.feedback, message = state.feedbackMessage, onDismiss = { viewModel.dismiss() })
    }
}
