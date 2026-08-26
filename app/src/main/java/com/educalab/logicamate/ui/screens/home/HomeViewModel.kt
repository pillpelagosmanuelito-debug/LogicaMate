package com.educalab.logicamate.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.educalab.logicamate.ServiceLocator
import com.educalab.logicamate.domain.engine.ProgressEngine
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.ChamberStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class ChamberMapNode(
    val chamberId: ChamberId,
    val displayName: String,
    val status: ChamberStatus,
    val completedChallenges: Int,
    val totalChallenges: Int,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val alias: String = "Explorador",
    val avatarId: Int = 0,
    val totalXp: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val fragmentsCollected: Int = 0,
    val totalFragments: Int = 8,
    val overallProgressPercent: Int = 0,
    val nodes: List<ChamberMapNode> = emptyList(),
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        val db = ServiceLocator.database(getApplication())
        combine(
            db.chamberDao().observeAll(),
            db.progressDao().observeAll(),
            db.gamificationDao().observeStats(),
            db.userProfileDao().observe(),
        ) { chambers, progressRows, stats, profile ->
            val progressByChamber = progressRows.associateBy { it.chamberId }
            val nodes = chambers.sortedBy { it.orderIndex }.map { chamber ->
                val p = progressByChamber[chamber.id]
                ChamberMapNode(
                    chamberId = ChamberId.valueOf(chamber.id),
                    displayName = chamber.displayName,
                    status = p?.let { ChamberStatus.valueOf(it.status) } ?: ChamberStatus.LOCKED,
                    completedChallenges = p?.challengesCompleted ?: 0,
                    totalChallenges = p?.totalChallenges ?: 0,
                )
            }
            val overallPercent = ProgressEngine.overallProgressPercent(
                progressRows.map {
                    ProgressEngine.ChamberProgressInput(
                        ChamberId.valueOf(it.chamberId), it.totalChallenges, it.challengesCompleted, it.perfectChallenges, null,
                    )
                },
            )
            val fragmentsCollected = db.keyFragmentDao().getUnlockedIds().size
            HomeUiState(
                isLoading = false,
                alias = profile?.alias ?: "Explorador",
                avatarId = profile?.avatarId ?: 0,
                totalXp = stats?.totalXp ?: 0,
                level = com.educalab.logicamate.domain.engine.GamificationEngine.levelForXp(stats?.totalXp ?: 0),
                currentStreak = stats?.currentStreak ?: 0,
                fragmentsCollected = fragmentsCollected,
                overallProgressPercent = overallPercent,
                nodes = nodes,
            )
        }.onEach { _state.value = it }.launchIn(viewModelScope)
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(app) as T
    }
}
