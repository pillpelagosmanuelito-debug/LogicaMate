package com.educalab.logicamate.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.educalab.logicamate.domain.model.Hint
import com.educalab.logicamate.ui.theme.CrystalTeal
import com.educalab.logicamate.ui.theme.EmberCoral
import com.educalab.logicamate.ui.theme.MossGreen
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneMid
import com.educalab.logicamate.ui.theme.SurfaceCard
import com.educalab.logicamate.ui.theme.SurfaceCardElevated
import com.educalab.logicamate.ui.theme.TextMuted
import com.educalab.logicamate.ui.theme.TextPrimary
import com.educalab.logicamate.ui.theme.TextSecondary

/** Cabecera común a toda cámara: volver, nombre de la cámara y una línea de Grafi (mascota). */
@Composable
fun ChamberTopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StoneMid)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver al mapa", tint = TextPrimary)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        actions()
    }
}

/** Bocadillo breve de Grafi/el explorador — nunca más de una frase (sección 5). */
@Composable
fun MascotLine(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarDot(CrystalTeal)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun AvatarDot(color: Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color),
    )
}

/**
 * Panel de pistas progresivas (sección 22-23): un botón por nivel, en orden,
 * que revela texto de estrategia — nunca la respuesta directa.
 */
@Composable
fun ProgressiveHintPanel(
    hints: List<Hint>,
    revealedLevels: Set<Int>,
    onRevealNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        revealedLevels.sorted().forEach { level ->
            val hint = hints.firstOrNull { it.level == level } ?: return@forEach
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCardElevated)
                    .padding(10.dp),
            ) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = RuneGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Pista $level: ${hint.text}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
        if (revealedLevels.size < 3) {
            val nextLevel = revealedLevels.size + 1
            Text(
                text = if (revealedLevels.isEmpty()) "¿Necesitas una pista?" else "Ver pista $nextLevel",
                color = CrystalTeal,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(onClick = onRevealNext),
            )
        }
    }
}

enum class FeedbackKind { CORRECT, INCORRECT }

/** Banner de feedback integrado (sección 24): nunca solo "Correcto/Incorrecto", siempre con explicación. */
@Composable
fun FeedbackBanner(kind: FeedbackKind?, message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = kind != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        val color = if (kind == FeedbackKind.CORRECT) MossGreen else EmberCoral
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.18f))
                .padding(14.dp)
                .clickable(onClick = onDismiss),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (kind == FeedbackKind.CORRECT) "✓" else "✕",
                color = color,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.width(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
    }
}
