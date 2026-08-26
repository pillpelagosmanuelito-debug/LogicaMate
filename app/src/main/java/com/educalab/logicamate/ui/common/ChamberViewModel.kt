package com.educalab.logicamate.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.educalab.logicamate.ServiceLocator
import com.educalab.logicamate.data.local.fromRows
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.engine.HintEngine
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.domain.model.DifficultyLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChamberUiState(
    val isLoading: Boolean = true,
    val chamberId: ChamberId = ChamberId.ENTRANCE,
    val queue: List<Challenge> = emptyList(),
    val queueIndex: Int = 0,
    val revealedHintLevels: Set<Int> = emptySet(),
    val hintsUsedThisAttempt: List<Int> = emptyList(),
    val feedback: FeedbackKind? = null,
    val feedbackMessage: String = "",
    val lastXpAwarded: Int = 0,
    val fragmentUnlocked: Boolean = false,
    val newlyUnlockedBadgeNames: List<String> = emptyList(),
    val chamberComplete: Boolean = false,
) {
    val currentChallenge: Challenge? get() = queue.getOrNull(queueIndex)
}

class ChamberViewModel(app: Application, private val chamberId: ChamberId) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ChamberUiState(chamberId = chamberId))
    val state: StateFlow<ChamberUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val db = ServiceLocator.database(getApplication())
            val entities = db.challengeDao().getByChamber(chamberId.name)
            val challenges = entities.map { entity ->
                val items = db.challengeDao().getItems(entity.id)
                val rules = db.challengeDao().getRules(entity.id)
                val hints = db.challengeDao().getHints(entity.id)
                fromRows(entity, items, rules, hints)
            }
            _state.value = _state.value.copy(isLoading = false, queue = challenges)
        }
    }

    fun revealNextHint() {
        val current = _state.value
        val next = HintEngine.nextRevealableLevel(current.revealedHintLevels) ?: return
        _state.value = current.copy(
            revealedHintLevels = current.revealedHintLevels + next,
            hintsUsedThisAttempt = current.hintsUsedThisAttempt + next,
        )
    }

    fun submit(submission: ChallengeValidator.Submission) {
        val current = _state.value
        val challenge = current.currentChallenge ?: return
        val isCorrect = try {
            ChallengeValidator.validateAny(challenge, submission)
        } catch (e: Exception) {
            false
        }
        viewModelScope.launch {
            val repo = ServiceLocator.progressRepository(getApplication())
            val result = repo.recordAttempt(
                challengeId = challenge.id,
                chamberId = challenge.chamberId,
                difficulty = challenge.difficulty,
                isCorrect = isCorrect,
                hintsUsedLevels = current.hintsUsedThisAttempt,
                submittedSolutionEncoded = submission.toString(),
            )
            _state.value = _state.value.copy(
                feedback = if (isCorrect) FeedbackKind.CORRECT else FeedbackKind.INCORRECT,
                feedbackMessage = if (isCorrect) challenge.explanation else "Todavía no encaja. Repasa las pistas y vuelve a intentarlo.",
                lastXpAwarded = result.xpAwarded,
                fragmentUnlocked = result.fragmentUnlocked,
                newlyUnlockedBadgeNames = result.newlyUnlockedBadges.map { it.name },
            )
        }
    }

    fun dismissFeedbackAndAdvance() {
        val current = _state.value
        val wasCorrect = current.feedback == FeedbackKind.CORRECT
        val nextIndex = if (wasCorrect) current.queueIndex + 1 else current.queueIndex
        _state.value = current.copy(
            feedback = null,
            feedbackMessage = "",
            revealedHintLevels = if (wasCorrect) emptySet() else current.revealedHintLevels,
            hintsUsedThisAttempt = if (wasCorrect) emptyList() else current.hintsUsedThisAttempt,
            queueIndex = nextIndex,
            fragmentUnlocked = false,
            newlyUnlockedBadgeNames = emptyList(),
            chamberComplete = nextIndex >= current.queue.size,
        )
    }

    fun dismissFeedbackOnly() {
        _state.value = _state.value.copy(feedback = null, feedbackMessage = "")
    }

    class Factory(private val app: Application, private val chamberId: ChamberId) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChamberViewModel(app, chamberId) as T
    }
}
