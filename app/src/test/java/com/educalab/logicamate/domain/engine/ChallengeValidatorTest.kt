package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.seed.SeedContent
import com.educalab.logicamate.domain.model.LogicCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeValidatorTest {

    @Test
    fun `validatePieceAnswer accepts the exact stored solution for a pattern challenge`() {
        val challenge = SeedContent.all.first { it.category == LogicCategory.PATTERN && it.rule.type == "PATTERN_CYCLE" }
        assertTrue(ChallengeValidator.validatePieceAnswer(challenge, challenge.solutionPieces))
    }

    @Test
    fun `validatePieceAnswer rejects a swapped-order answer`() {
        val challenge = SeedContent.all.first { it.category == LogicCategory.MATRIX }
        val wrong = challenge.solutionPieces.reversed() + challenge.solutionPieces // longitud distinta / orden distinto
        assertFalse(ChallengeValidator.validatePieceAnswer(challenge, wrong))
    }

    @Test
    fun `validateClassification accepts the true partition under a different grouping order`() {
        val challenge = SeedContent.all.first { it.category == LogicCategory.CLASSIFICATION }
        val property = ClassifyProperty.valueOf(challenge.rule.params.getValue("property"))
        val truePartition = ClassificationEngine.canonicalPartition(challenge.optionPool, property).map { it.toList() }
        assertTrue(ChallengeValidator.validateClassification(challenge, truePartition.reversed()))
    }

    @Test
    fun `validateRelationOrder rejects an order that violates the stored solution`() {
        val challenge = SeedContent.all.first { it.category == LogicCategory.RELATION }
        val trueSolution = challenge.rule.params.getValue("solution").split(",")
        val scrambled = trueSolution.reversed()
        assertFalse(ChallengeValidator.validateRelationOrder(challenge, scrambled))
    }

    @Test
    fun `validateDeductionAssignment accepts only the exact stored assignment`() {
        val challenge = SeedContent.all.first { it.category == LogicCategory.DEDUCTION }
        val trueSolution = challenge.rule.params.getValue("solution").split(",").associate {
            val (p, o) = it.split(":", limit = 2); p to o
        }
        assertTrue(ChallengeValidator.validateDeductionAssignment(challenge, trueSolution))
    }

    @Test
    fun `validateConstruction accepts a strip that satisfies the decoded goal`() {
        val challenge = SeedContent.all.first {
            it.category == LogicCategory.CONSTRUCTION && it.rule.type == "COUNT_INCREASES_BY"
        }
        val step = challenge.rule.params.getValue("step").toInt()
        val built = listOf(1, 1 + step, 1 + 2 * step).map { com.educalab.logicamate.domain.model.PieceSpec(count = it) }
        assertTrue(ChallengeValidator.validateConstruction(challenge, built))
    }
}
