package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.DifficultyLevel
import com.educalab.logicamate.domain.model.Hint
import com.educalab.logicamate.domain.model.InteractionType
import com.educalab.logicamate.domain.model.LogicCategory
import com.educalab.logicamate.domain.model.LogicRule
import com.educalab.logicamate.domain.model.PieceColor
import com.educalab.logicamate.domain.model.PieceSize
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape
import kotlin.random.Random

/**
 * Genera el Reto Diario (sección 19) de forma local y determinista: la
 * semilla del generador aleatorio se deriva de la fecha ("yyyy-MM-dd"), así
 * que todos los retos generados para un mismo día son siempre iguales entre
 * ejecuciones — no requiere red ni persistir la estructura completa, solo el
 * `date` + `challengeId` (ver DailyChallengeEntity). Cada desafío generado
 * se valida antes de devolverse (sección 27: solución única y no ambigua).
 */
object DailyChallengeGenerator {

    private val rotatingCategories = listOf(
        LogicCategory.SEQUENCE, LogicCategory.PATTERN, LogicCategory.CLASSIFICATION,
        LogicCategory.MATRIX, LogicCategory.RELATION, LogicCategory.DEDUCTION,
    )

    fun generateFor(dateKey: String): Challenge {
        val seed = dateKey.hashCode().toLong()
        val random = Random(seed)
        // La categoría también depende de la fecha para variar día a día de forma determinista.
        val category = rotatingCategories[Math.floorMod(dateKey.hashCode(), rotatingCategories.size)]
        val difficulty = DifficultyLevel.entries[random.nextInt(DifficultyLevel.entries.size)]
        return generate(category, difficulty, random, idSuffix = dateKey)
    }

    fun generate(category: LogicCategory, difficulty: DifficultyLevel, random: Random, idSuffix: String): Challenge {
        var attempt = 0
        while (attempt < 25) {
            attempt++
            val candidate = tryGenerate(category, difficulty, random, "$idSuffix-$attempt")
            if (candidate != null) return candidate
        }
        error("No se pudo generar un desafío diario válido para $category/$difficulty tras 25 intentos.")
    }

    private fun tryGenerate(category: LogicCategory, difficulty: DifficultyLevel, random: Random, id: String): Challenge? =
        when (category) {
            LogicCategory.SEQUENCE -> generateSequence(difficulty, random, id)
            LogicCategory.PATTERN -> generatePattern(difficulty, random, id)
            LogicCategory.CLASSIFICATION -> generateClassification(difficulty, random, id)
            LogicCategory.MATRIX -> generateMatrix(difficulty, random, id)
            LogicCategory.RELATION -> generateRelation(difficulty, random, id)
            LogicCategory.DEDUCTION -> generateDeduction(difficulty, random, id)
            else -> null
        }

    private fun generateSequence(difficulty: DifficultyLevel, random: Random, id: String): Challenge? {
        val start = random.nextInt(1, 10)
        val step = random.nextInt(2, 6)
        val values = when (difficulty) {
            DifficultyLevel.INITIAL -> (0..4).map { start + it * step }
            DifficultyLevel.INTERMEDIATE -> {
                val a = random.nextInt(1, 5); val b = random.nextInt(1, 5)
                if (a == b) return null
                val list = mutableListOf(start)
                repeat(4) { i -> list += list.last() + if (i % 2 == 0) a else b }
                list
            }
            DifficultyLevel.ADVANCED -> {
                val ratio = random.nextInt(2, 3)
                (0..3).map { start * Math.pow(ratio.toDouble(), it.toDouble()).toInt() }
            }
        }
        if (!SequenceEngine.hasUniqueSolution(values)) return null
        val answer = SequenceEngine.nextValue(values) ?: return null
        val shown = values + listOf(null)
        return Challenge(
            id = "DAILY-SEQ-$id",
            chamberId = ChamberId.SEQUENCES,
            category = LogicCategory.SEQUENCE,
            difficulty = difficulty,
            interactionType = InteractionType.COMPLETE,
            prompt = "Descubre la regla del pasadizo y completa la secuencia.",
            items = shown.map { v -> if (v == null) PieceSpec.BLANK else PieceSpec(shape = Shape.NONE, value = v) },
            optionPool = emptyList(),
            solutionPieces = listOf(PieceSpec(shape = Shape.NONE, value = answer)),
            rule = LogicRule("SEQUENCE_GENERATED"),
            hints = listOf(
                Hint(1, "Fíjate en cómo cambia cada número respecto al anterior."),
                Hint(2, "Calcula la diferencia entre dos números seguidos."),
                Hint(3, "Aplica esa misma diferencia al último número de la fila."),
            ),
            explanation = "La secuencia sigue una regla constante entre cada término y el siguiente.",
            isSeed = false,
        )
    }

    private fun generatePattern(difficulty: DifficultyLevel, random: Random, id: String): Challenge? {
        val colors = PieceColor.entries.shuffled(random).take(2)
        val cycle = when (difficulty) {
            DifficultyLevel.INITIAL -> listOf(colors[0], colors[0], colors[1])
            DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED -> listOf(colors[0], colors[1], colors[1])
        }
        val repeats = 2
        val items = (0 until cycle.size * repeats).map { PieceSpec(shape = Shape.CIRCLE, color = cycle[it % cycle.size]) }
        val answer = PatternEngine.nextInCycle(items) ?: return null
        return Challenge(
            id = "DAILY-PAT-$id",
            chamberId = ChamberId.PATTERNS,
            category = LogicCategory.PATTERN,
            difficulty = difficulty,
            interactionType = InteractionType.COMPLETE,
            prompt = "Observa el mosaico. ¿Qué pieza sigue?",
            items = items + PieceSpec.BLANK,
            optionPool = PieceColor.entries.map { PieceSpec(shape = Shape.CIRCLE, color = it) },
            solutionPieces = listOf(answer),
            rule = LogicRule("PATTERN_GENERATED"),
            hints = listOf(
                Hint(1, "Mira qué ocurre entre cada dos piezas."),
                Hint(2, "Observa los colores. Se repite una combinación."),
                Hint(3, "El patrón repite un bloque de ${cycle.size} piezas."),
            ),
            explanation = "El mosaico repite un bloque de ${cycle.size} piezas una y otra vez.",
            isSeed = false,
        )
    }

    private fun generateClassification(difficulty: DifficultyLevel, random: Random, id: String): Challenge? {
        val property = ClassifyProperty.entries[random.nextInt(ClassifyProperty.entries.size)]
        val shapes = listOf(Shape.TRIANGLE, Shape.CIRCLE, Shape.SQUARE)
        val pieces = when (property) {
            ClassifyProperty.SHAPE -> shapes.flatMap { s -> List(2) { PieceSpec(shape = s, color = PieceColor.entries.random(random)) } }
            ClassifyProperty.COLOR -> PieceColor.entries.take(3).flatMap { c -> List(2) { PieceSpec(shape = shapes.random(random), color = c) } }
            ClassifyProperty.SIZE -> PieceSize.entries.flatMap { sz -> List(2) { PieceSpec(shape = shapes.random(random), size = sz) } }
            ClassifyProperty.PARITY_COUNT -> (1..6).map { PieceSpec(shape = shapes.random(random), count = it) }
        }.shuffled(random)
        if (!ClassificationEngine.isWellFormedForChallenge(pieces, property)) return null
        val partition = ClassificationEngine.canonicalPartition(pieces, property)
        return Challenge(
            id = "DAILY-CLAS-$id",
            chamberId = ChamberId.CLASSIFICATION,
            category = LogicCategory.CLASSIFICATION,
            difficulty = difficulty,
            interactionType = InteractionType.CLASSIFY,
            prompt = "Separa estos símbolos en grupos según una regla oculta.",
            items = emptyList(),
            optionPool = pieces,
            rule = LogicRule("CLASSIFICATION_GENERATED", mapOf("property" to property.name)),
            hints = listOf(
                Hint(1, "Compara las piezas de dos en dos."),
                Hint(2, "Hay una propiedad que se repite dentro de cada grupo."),
                Hint(3, "La regla es: ${property.name.lowercase()}."),
            ),
            explanation = "Cada grupo comparte el mismo valor de ${property.name.lowercase()}.",
            isSeed = false,
        ).let { base -> base.copy(rule = base.rule.copy(params = base.rule.params + ("solution" to encodePartition(partition)))) }
    }

    private fun encodePartition(partition: Set<Set<Int>>): String =
        partition.joinToString("|") { group -> group.sorted().joinToString(",") }

    private fun generateMatrix(difficulty: DifficultyLevel, random: Random, id: String): Challenge? {
        val baseRow = listOf(
            PieceSpec(shape = Shape.TRIANGLE, count = 1),
            PieceSpec(shape = Shape.CIRCLE, count = 1),
        )
        val transform = MatrixEngine.CellTransform.MultiplyCount(2)
        val rows = if (difficulty == DifficultyLevel.INITIAL) 2 else 3
        val matrix = MatrixEngine.buildMatrix(baseRow, transform, rows)
        val blankRow = rows - 1
        val blankCol = random.nextInt(baseRow.size)
        val answer = matrix[blankRow][blankCol]
        return Challenge(
            id = "DAILY-MAT-$id",
            chamberId = ChamberId.MATRICES,
            category = LogicCategory.MATRIX,
            difficulty = difficulty,
            interactionType = InteractionType.COMPLETE,
            prompt = "Completa la celda que falta en el panel mural.",
            items = matrix.flatten().mapIndexed { idx, p -> if (idx == blankRow * baseRow.size + blankCol) PieceSpec.BLANK else p },
            optionPool = listOf(answer, answer.copy(count = answer.count + 1), answer.copy(count = (answer.count - 1).coerceAtLeast(1))),
            solutionPieces = listOf(answer),
            rule = LogicRule("MATRIX_GENERATED", mapOf("cols" to baseRow.size.toString())),
            hints = listOf(
                Hint(1, "Compara cada fila con la anterior."),
                Hint(2, "Algo se duplica de fila en fila."),
                Hint(3, "La cantidad se multiplica por 2 en cada fila."),
            ),
            explanation = "Cada fila duplica la cantidad de la fila anterior, columna a columna.",
            isSeed = false,
        )
    }

    private fun generateRelation(difficulty: DifficultyLevel, random: Random, id: String): Challenge? {
        val names = listOf("Lía", "Tomás", "Nia", "Kael").shuffled(random).take(if (difficulty == DifficultyLevel.ADVANCED) 4 else 3)
        val constraints = mutableListOf<RelationEngine.Constraint>()
        // Genera restricciones a partir de un orden secreto y comprueba unicidad.
        val secretOrder = names.shuffled(random)
        constraints += RelationEngine.Constraint.Before(secretOrder[0], secretOrder[1])
        if (names.size >= 3) constraints += RelationEngine.Constraint.Before(secretOrder[1], secretOrder[2])
        if (names.size == 4) constraints += RelationEngine.Constraint.NotAdjacent(secretOrder[0], secretOrder[3])
        val solution = RelationEngine.uniqueSolution(names, constraints) ?: return null
        return Challenge(
            id = "DAILY-REL-$id",
            chamberId = ChamberId.RELATIONS,
            category = LogicCategory.RELATION,
            difficulty = difficulty,
            interactionType = InteractionType.CONNECT,
            prompt = "Coloca a los exploradores en el orden correcto según las pistas.",
            items = emptyList(),
            optionPool = emptyList(),
            rule = LogicRule(
                "RELATION_GENERATED",
                mapOf("items" to names.joinToString(","), "solution" to solution.joinToString(",")),
            ),
            hints = listOf(
                Hint(1, "Empieza por la pista que menciona una posición fija u orden directo."),
                Hint(2, "Descarta las posiciones imposibles una a una."),
                Hint(3, "Solo hay un orden que cumple todas las pistas a la vez."),
            ),
            explanation = "Cada pista descarta posiciones hasta dejar un único orden posible.",
            isSeed = false,
        )
    }

    private fun generateDeduction(difficulty: DifficultyLevel, random: Random, id: String): Challenge? {
        val people = listOf("Lía", "Tomás", "Nia")
        val objects = listOf("cristal azul", "cristal verde", "llave dorada")
        val secretAssignment = people.zip(objects.shuffled(random)).toMap()
        val clues = mutableListOf<DeductionEngine.Clue>()
        // Construye pistas negativas suficientes para dejar una única solución.
        for (person in people) {
            for (obj in objects) {
                if (secretAssignment[person] != obj && random.nextBoolean()) {
                    clues += DeductionEngine.Clues.doesNotHave(person, obj)
                }
            }
        }
        val solution = DeductionEngine.uniqueSolution(people, objects, clues) ?: return null
        return Challenge(
            id = "DAILY-DED-$id",
            chamberId = ChamberId.DEDUCTION,
            category = LogicCategory.DEDUCTION,
            difficulty = difficulty,
            interactionType = InteractionType.INVESTIGATE,
            prompt = "Usa las pistas para descubrir qué objeto tiene cada explorador.",
            items = emptyList(),
            optionPool = emptyList(),
            rule = LogicRule(
                "DEDUCTION_GENERATED",
                mapOf("solution" to solution.entries.joinToString(",") { "${it.key}:${it.value}" }),
            ),
            hints = listOf(
                Hint(1, "Marca primero lo que cada pista descarta, no lo que confirma."),
                Hint(2, "Cuando a alguien solo le quede una opción posible, esa es la suya."),
                Hint(3, "Resuelve a la persona con menos opciones restantes primero."),
            ),
            explanation = "Cada pista elimina posibilidades hasta que solo queda una asignación válida.",
            isSeed = false,
        )
    }
}
