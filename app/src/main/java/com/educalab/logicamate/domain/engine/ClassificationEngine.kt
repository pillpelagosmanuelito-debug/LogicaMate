package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceSpec

/** Propiedad usada como regla oculta de clasificación (Cámara "Sala de Clasificación"). */
enum class ClassifyProperty { SHAPE, COLOR, SIZE, PARITY_COUNT }

object ClassificationEngine {

    private fun keyFor(piece: PieceSpec, property: ClassifyProperty): String = when (property) {
        ClassifyProperty.SHAPE -> piece.shape.name
        ClassifyProperty.COLOR -> piece.color.name
        ClassifyProperty.SIZE -> piece.size.name
        ClassifyProperty.PARITY_COUNT -> if (piece.count % 2 == 0) "EVEN" else "ODD"
    }

    /**
     * Partición canónica: agrupa los ÍNDICES de [pieces] según [property].
     * Devuelve un conjunto de conjuntos de índices (el orden/nombre de grupo
     * no importa, solo qué piezas quedan juntas).
     */
    fun canonicalPartition(pieces: List<PieceSpec>, property: ClassifyProperty): Set<Set<Int>> =
        pieces.indices
            .groupBy { keyFor(pieces[it], property) }
            .values
            .map { it.toSet() }
            .toSet()

    /**
     * Valida la agrupación construida por el niño ([childGroups]: lista de
     * grupos, cada uno una lista de índices sobre [pieces]) comparándola con
     * la partición canónica de la regla real. Es indiferente a cómo el niño
     * nombró o coloreó cada grupo.
     */
    fun validateGrouping(
        pieces: List<PieceSpec>,
        property: ClassifyProperty,
        childGroups: List<List<Int>>,
    ): Boolean {
        val childPartition = childGroups.map { it.toSet() }.filter { it.isNotEmpty() }.toSet()
        return childPartition == canonicalPartition(pieces, property)
    }

    /** Un conjunto de piezas es válido para un reto si produce entre 2 y 4 grupos no triviales. */
    fun isWellFormedForChallenge(pieces: List<PieceSpec>, property: ClassifyProperty): Boolean {
        val groups = canonicalPartition(pieces, property)
        return groups.size in 2..4 && groups.all { it.size >= 2 }
    }
}
