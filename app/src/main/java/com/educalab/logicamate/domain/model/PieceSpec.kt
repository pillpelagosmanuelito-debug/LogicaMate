package com.educalab.logicamate.domain.model

/**
 * Representa una pieza manipulable del templo: triángulo, círculo, cuadrado...
 * con propiedades variables (forma, color, tamaño, cantidad, valor numérico).
 * Es el vocabulario visual común a Patrones, Secuencias, Analogías, Matrices,
 * Clasificación y el Taller Constructor — así todas las cámaras "hablan el
 * mismo idioma" visual aunque la mecánica de interacción cambie.
 */
enum class Shape { TRIANGLE, CIRCLE, SQUARE, STAR, HEXAGON, DIAMOND, NONE }

enum class PieceColor { GOLD, CRYSTAL, CORAL, STONE, EMBER, MOSS }

enum class PieceSize { SMALL, MEDIUM, LARGE }

data class PieceSpec(
    val shape: Shape = Shape.CIRCLE,
    val color: PieceColor = PieceColor.GOLD,
    val size: PieceSize = PieceSize.MEDIUM,
    val count: Int = 1,
    val value: Int? = null, // usado en secuencias numéricas
    val isBlank: Boolean = false, // "?" — la casilla a completar
) {
    /** Serialización compacta y estable usada en Room (TEXT) y en el generador semilla. */
    fun encode(): String =
        if (isBlank) "BLANK" else "$shape:$color:$size:$count:${value ?: ""}"

    companion object {
        val BLANK = PieceSpec(shape = Shape.NONE, isBlank = true)

        fun decode(raw: String): PieceSpec {
            if (raw == "BLANK") return BLANK
            val parts = raw.split(":")
            return PieceSpec(
                shape = Shape.valueOf(parts[0]),
                color = PieceColor.valueOf(parts[1]),
                size = PieceSize.valueOf(parts[2]),
                count = parts[3].toInt(),
                value = parts.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toInt(),
            )
        }
    }
}
