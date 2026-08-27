package com.educalab.logicamate.ui.screens.profile

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.logicamate.ServiceLocator
import com.educalab.logicamate.data.local.entity.UserProfileEntity
import com.educalab.logicamate.ui.common.ChamberTopBar
import com.educalab.logicamate.ui.theme.CrystalTeal
import com.educalab.logicamate.ui.theme.MossGreen
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneDeep
import com.educalab.logicamate.ui.theme.StoneMuted
import com.educalab.logicamate.ui.theme.SurfaceCard
import com.educalab.logicamate.ui.theme.TextMuted
import com.educalab.logicamate.ui.theme.TextPrimary
import com.educalab.logicamate.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val AvatarColors = listOf(RuneGold, CrystalTeal, MossGreen, StoneMuted, Color(0xFFE0432B), Color(0xFF6FBE7A), Color(0xFFC79A1F), Color(0xFF3ADBC6))
private fun Color(argb: Long) = androidx.compose.ui.graphics.Color(argb)

data class ProfileUiState(
    val alias: String = "Explorador",
    val avatarId: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val totalXp: Int = 0,
    val badgesUnlocked: Int = 0,
    val totalBadges: Int = 8,
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        val db = ServiceLocator.database(getApplication())
        combine(db.userProfileDao().observe(), db.gamificationDao().observeStats(), db.gamificationDao().observeUnlockedBadges()) { profile, stats, badges ->
            ProfileUiState(
                alias = profile?.alias ?: "Explorador",
                avatarId = profile?.avatarId ?: 0,
                soundEnabled = profile?.soundEnabled ?: true,
                hapticsEnabled = profile?.hapticsEnabled ?: true,
                totalXp = stats?.totalXp ?: 0,
                badgesUnlocked = badges.size,
            )
        }.onEach { _state.value = it }.launchIn(viewModelScope)
    }

    fun setAvatar(id: Int) = viewModelScope.launch {
        val db = ServiceLocator.database(getApplication())
        val profile = db.userProfileDao().get() ?: return@launch
        db.userProfileDao().update(profile.copy(avatarId = id))
    }

    fun toggleSound(enabled: Boolean) = viewModelScope.launch {
        val db = ServiceLocator.database(getApplication())
        val profile = db.userProfileDao().get() ?: return@launch
        db.userProfileDao().update(profile.copy(soundEnabled = enabled))
    }

    fun toggleHaptics(enabled: Boolean) = viewModelScope.launch {
        val db = ServiceLocator.database(getApplication())
        val profile = db.userProfileDao().get() ?: return@launch
        db.userProfileDao().update(profile.copy(hapticsEnabled = enabled))
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(app) as T
    }
}

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(context.applicationContext as Application))
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(StoneDeep).navigationBarsPadding()) {
        ChamberTopBar(title = "Perfil del Explorador", onBack = onBack)
        Column(Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(96.dp).clip(CircleShape).background(AvatarColors[state.avatarId % AvatarColors.size].copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.alias.take(1).uppercase(), style = MaterialTheme.typography.displayMedium, color = AvatarColors[state.avatarId % AvatarColors.size])
            }
            Text(state.alias, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, modifier = Modifier.padding(top = 8.dp))
            Text("${state.totalXp} XP · ${state.badgesUnlocked}/${state.totalBadges} insignias", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

            Text("Elige tu avatar", style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxWidth()) {
                items(AvatarColors.indices.toList()) { id ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AvatarColors[id].copy(alpha = if (id == state.avatarId) 0.5f else 0.2f))
                            .clickable { viewModel.setAvatar(id) },
                    )
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sonido", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                Switch(checked = state.soundEnabled, onCheckedChange = { viewModel.toggleSound(it) })
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vibración", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                Switch(checked = state.hapticsEnabled, onCheckedChange = { viewModel.toggleHaptics(it) })
            }
            Text(
                "Todo tu progreso se guarda solo en este dispositivo. LogicaMate no pide tu nombre real ni ningún otro dato personal.",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}
