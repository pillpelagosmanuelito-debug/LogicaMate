package com.educalab.logicamate.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.ChamberStatus
import com.educalab.logicamate.ui.theme.CrystalTeal
import com.educalab.logicamate.ui.theme.EmberCoral
import com.educalab.logicamate.ui.theme.MossGreen
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneDeep
import com.educalab.logicamate.ui.theme.StoneMid
import com.educalab.logicamate.ui.theme.StoneMuted
import com.educalab.logicamate.ui.theme.SurfaceCard
import com.educalab.logicamate.ui.theme.TextMuted
import com.educalab.logicamate.ui.theme.TextPrimary
import com.educalab.logicamate.ui.theme.TextSecondary

@Composable
fun HomeMapScreen(onOpenChamber: (ChamberId) -> Unit, onOpenProfile: () -> Unit, onOpenCollection: () -> Unit, onOpenDaily: () -> Unit) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(context.applicationContext as android.app.Application))
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(StoneDeep)) {
        HeaderBar(state, onOpenProfile)

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RuneGold) }
            return@Column
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            StatChip("🔑 ${state.fragmentsCollected}/${state.totalFragments}", modifier = Modifier.weight(1f))
            Spacer(Modifier.size(8.dp))
            StatChip("🔥 ${state.currentStreak}", modifier = Modifier.weight(1f))
            Spacer(Modifier.size(8.dp))
            StatChip("Nv. ${state.level}", modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDaily)
                    .padding(12.dp),
            ) {
                Text("⭐ Reto diario disponible — toca para intentarlo", color = CrystalTeal, style = MaterialTheme.typography.labelLarge)
            }
        }

        Box(Modifier.weight(1f)) {
            TempleWindingMap(state.nodes, onOpenChamber)
        }

        Row(
            Modifier.fillMaxWidth().background(StoneMid).padding(12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Text(
                "Progreso del templo: ${state.overallProgressPercent}%",
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onOpenCollection),
            )
        }
    }
}

@Composable
private fun HeaderBar(state: HomeUiState, onOpenProfile: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(StoneMid).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Templo de los Patrones", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Text("Hola, ${state.alias}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        IconButton(onClick = onOpenProfile) {
            Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = RuneGold)
        }
    }
}

@Composable
private fun StatChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
    }
}

private fun statusColor(status: ChamberStatus): Color = when (status) {
    ChamberStatus.LOCKED -> StoneMuted.copy(alpha = 0.4f)
    ChamberStatus.AVAILABLE -> CrystalTeal
    ChamberStatus.STARTED -> RuneGold
    ChamberStatus.COMPLETED -> MossGreen
    ChamberStatus.MASTERED -> EmberCoral
}

/**
 * El mapa principal como camino sinuoso de cámaras conectadas (sección 6/7):
 * NO es una lista vertical de botones — los nodos alternan izquierda/centro/
 * derecha y una línea dibujada a mano (Canvas) los conecta, como un sendero
 * real dentro del templo.
 */
@Composable
private fun TempleWindingMap(nodes: List<com.educalab.logicamate.ui.screens.home.ChamberMapNode>, onOpen: (ChamberId) -> Unit) {
    val scrollState = rememberScrollState()
    val nodeSpacingDp = 130.dp
    val density = androidx.compose.ui.platform.LocalDensity.current

    Box(Modifier.fillMaxSize().verticalScroll(scrollState)) {
        val totalHeight = nodeSpacingDp * nodes.size + 60.dp
        Box(Modifier.fillMaxWidth().height(totalHeight)) {
            // Línea del sendero
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spacingPx = with(density) { nodeSpacingDp.toPx() }
                val path = androidx.compose.ui.graphics.Path()
                nodes.forEachIndexed { index, _ ->
                    val x = xForIndex(index, size.width)
                    val y = spacingPx * index + spacingPx / 2
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = StoneMuted.copy(alpha = 0.5f),
                    style = Stroke(width = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f))),
                )
            }

            nodes.forEachIndexed { index, node ->
                val xFraction = xFractionForIndex(index)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(top = nodeSpacingDp * index),
                ) {
                    Column(
                        modifier = Modifier
                            .align(when {
                                xFraction < 0.4f -> Alignment.CenterStart
                                xFraction > 0.6f -> Alignment.CenterEnd
                                else -> Alignment.Center
                            })
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ChamberNodeBubble(node, onOpen)
                        Text(
                            node.displayName,
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (node.totalChallenges > 0) {
                            Text(
                                "${node.completedChallenges}/${node.totalChallenges}",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun xFractionForIndex(index: Int): Float = when (index % 3) {
    0 -> 0.22f
    1 -> 0.5f
    else -> 0.78f
}

private fun xForIndex(index: Int, width: Float): Float = width * xFractionForIndex(index)

@Composable
private fun ChamberNodeBubble(node: com.educalab.logicamate.ui.screens.home.ChamberMapNode, onOpen: (ChamberId) -> Unit) {
    val color = statusColor(node.status)
    val enabled = node.status != ChamberStatus.LOCKED
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.22f))
            .then(
                if (enabled) Modifier.clickable { onOpen(node.chamberId) } else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            if (!enabled) {
                Icon(Icons.Filled.Lock, contentDescription = "Bloqueada", tint = TextMuted)
            } else {
                Text(
                    text = when (node.status) {
                        ChamberStatus.MASTERED -> "★"
                        ChamberStatus.COMPLETED -> "✓"
                        else -> ""
                    },
                    color = color,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}
