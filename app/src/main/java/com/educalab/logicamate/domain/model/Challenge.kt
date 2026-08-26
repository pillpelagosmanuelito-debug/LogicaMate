package com.educalab.logicamate.domain.model

/**
 * Representación de dominio de un desafío del templo — independiente de Room.
 * `data/local` mapea esto hacia/desde las entidades ChallengeEntity + ChallengeItemEntity
 * + ChallengeRuleEntity (ver DATA_MAPPING en ChallengeMapper.kt).
 */
data class Hint(val level: Int, val text: String)

data class LogicRule(
    val type: String,        // p.ej. "ALTERNATION", "ARITHMETIC", "GEOMETRIC", "SORT_BY_PROPERTY"
    val params: Map<String, String> = emptyMap(),
)

data class Challenge(
    val id: String,
    val chamberId: ChamberId,
    val category: LogicCategory,
    val difficulty: DifficultyLevel,
    val interactionType: InteractionType,
    val prompt: String,
    val items: List<PieceSpec>,     // secuencia de piezas mostradas, incluye BLANK donde aplica
    val optionPool: List<PieceSpec> = emptyList(), // piezas arrastrables/seleccionables disponibles
    val solutionIndices: List<Int> = emptyList(),  // índices en items que se completan
    val solutionPieces: List<PieceSpec> = emptyList(), // piezas correctas para esas posiciones (mismo orden)
    val rule: LogicRule,
    val hints: List<Hint>,
    val explanation: String,
    val isSeed: Boolean = true,
) {
    init {
        require(hints.size <= 3) { "Máximo 3 niveles de pista por desafío." }
    }
}
