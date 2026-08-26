package com.educalab.logicamate.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.educalab.logicamate.domain.model.PieceColor
import com.educalab.logicamate.domain.model.PieceSize
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape
import com.educalab.logicamate.ui.theme.CrystalTeal
import com.educalab.logicamate.ui.theme.EmberCoral
import com.educalab.logicamate.ui.theme.MossGreen
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneMuted
import com.educalab.logicamate.ui.theme.SurfaceCardElevated
import com.educalab.logicamate.ui.theme.TextMuted
import kotlin.math.cos
import kotlin.math.sin

fun PieceColor.toComposeColor(): Color = when (this) {
    PieceColor.GOLD -> RuneGold
    PieceColor.CRYSTAL -> CrystalTeal
    PieceColor.CORAL -> EmberCoral
    PieceColor.STONE -> StoneMuted
    PieceColor.EMBER -> Color(0xFFE0432B)
    PieceColor.MOSS -> MossGreen
}

private fun PieceSize.scaleFactor(): Float = when (this) {
    PieceSize.SMALL -> 0.62f
    PieceSize.MEDIUM -> 0.82f
    PieceSize.LARGE -> 1.0f
}

/**
 * Dibuja la forma real de una pieza sobre un Canvas — nada de emojis ni
 * iconos genéricos: son las mismas seis formas (triángulo, círculo,
 * cuadrado, estrella, hexágono, diamante) que definen el vocabulario visual
 * de todo el templo (ver domain/model/PieceSpec.kt).
 */
private fun DrawScope.drawShape(shape: Shape, color: Color, count: Int) {
    val side = kotlin.math.min(size.width, size.height)
    val center = Offset(size.width / 2, size.height / 2)
    val r = side / 2

    fun polygon(sides: Int, rotationDeg: Float): Path {
        val path = Path()
        for (i in 0 until sides) {
            val angle = Math.toRadians((rotationDeg + i * 360f / sides).toDouble())
            val point = Offset(center.x + r * cos(angle).toFloat(), center.y + r * sin(angle).toFloat())
            if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        path.close()
        return path
    }

    fun star(points: Int = 5): Path {
        val path = Path()
        val outerR = r
        val innerR = r * 0.45f
        for (i in 0 until points * 2) {
            val rad = if (i % 2 == 0) outerR else innerR
            val angle = Math.toRadians((-90f + i * 180f / points).toDouble())
            val point = Offset(center.x + rad * cos(angle).toFloat(), center.y + rad * sin(angle).toFloat())
            if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        path.close()
        return path
    }

    when (shape) {
        Shape.CIRCLE -> drawCircle(color = color, radius = r, center = center)
        Shape.SQUARE -> drawPath(polygon(4, -45f), color = color)
        Shape.TRIANGLE -> drawPath(polygon(3, -90f), color = color)
        Shape.HEXAGON -> drawPath(polygon(6, -90f), color = color)
        Shape.DIAMOND -> drawPath(polygon(4, 0f), color = color)
        Shape.STAR -> drawPath(star(), color = color)
        Shape.NONE -> drawCircle(color = color.copy(alpha = 0.3f), radius = r * 0.3f, center = center)
    }
    if (count > 1 && shape != Shape.NONE) {
        // Un pequeño anillo indica "cantidad" sin depender solo de un número escrito.
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = r * 0.2f,
            center = Offset(center.x + r * 0.6f, center.y - r * 0.6f),
            style = Stroke(width = 3f),
        )
    }
}

/** Renderiza una [PieceSpec]: forma real dibujada, número si aplica, o hueco "?" si está en blanco. */
@Composable
fun PieceView(
    piece: PieceSpec,
    modifier: Modifier = Modifier,
    boxSize: Dp = 56.dp,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.08f else 1f, animationSpec = spring(), label = "pieceScale")
    val interactionSource = remember { MutableInteractionSource() }
    val clickModifier = if (onClick != null) {
        Modifier.clickable(interactionSource = interactionSource, indication = ripple(bounded = false), onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .size(boxSize)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (piece.isBlank) SurfaceCardElevated else Color.Transparent)
            .border(
                width = if (piece.isBlank) 2.dp else if (selected) 3.dp else 0.dp,
                color = if (selected) RuneGold else StoneMuted.copy(alpha = if (piece.isBlank) 0.6f else 0f),
                shape = RoundedCornerShape(14.dp),
            )
            .then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        when {
            piece.isBlank -> Text("?", color = TextMuted, fontWeight = FontWeight.Black, fontSize = 24.sp)
            piece.value != null -> Text(
                piece.value.toString(),
                color = piece.color.toComposeColor(),
                fontWeight = FontWeight.Black,
                fontSize = (18 * piece.size.scaleFactor() + 10).sp,
            )
            else -> Canvas(modifier = Modifier.size(boxSize * piece.size.scaleFactor())) {
                drawShape(piece.shape, piece.color.toComposeColor(), piece.count)
            }
        }
    }
}
