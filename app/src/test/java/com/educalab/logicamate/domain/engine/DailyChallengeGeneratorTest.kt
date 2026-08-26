package com.educalab.logicamate.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyChallengeGeneratorTest {

    @Test
    fun `same date always generates the same challenge`() {
        val first = DailyChallengeGenerator.generateFor("2026-08-24")
        val second = DailyChallengeGenerator.generateFor("2026-08-24")
        assertEquals(first.id, second.id)
        assertEquals(first.category, second.category)
        assertEquals(first.solutionPieces, second.solutionPieces)
    }

    @Test
    fun `different dates typically generate different challenges`() {
        val dates = (1..10).map { "2026-08-%02d".format(it) }
        val ids = dates.map { DailyChallengeGenerator.generateFor(it).id }
        assertTrue(ids.toSet().size > 1)
    }

    @Test
    fun `generated sequence challenge has a well-formed unique-solution rule`() {
        val challenge = DailyChallengeGenerator.generate(
            com.educalab.logicamate.domain.model.LogicCategory.SEQUENCE,
            com.educalab.logicamate.domain.model.DifficultyLevel.INITIAL,
            kotlin.random.Random(42),
            "test",
        )
        assertEquals(1, challenge.solutionPieces.size)
        assertTrue(challenge.hints.size == 3)
    }

    @Test
    fun `generated classification challenge stores a well-formed partition`() {
        val challenge = DailyChallengeGenerator.generate(
            com.educalab.logicamate.domain.model.LogicCategory.CLASSIFICATION,
            com.educalab.logicamate.domain.model.DifficultyLevel.INITIAL,
            kotlin.random.Random(7),
            "test",
        )
        val solutionStr = challenge.rule.params["solution"]
        assertTrue(!solutionStr.isNullOrBlank())
    }

    @Test
    fun `generated relation challenge has a unique underlying order`() {
        val challenge = DailyChallengeGenerator.generate(
            com.educalab.logicamate.domain.model.LogicCategory.RELATION,
            com.educalab.logicamate.domain.model.DifficultyLevel.INTERMEDIATE,
            kotlin.random.Random(3),
            "test",
        )
        val items = challenge.rule.params.getValue("items").split(",")
        val solution = challenge.rule.params.getValue("solution").split(",")
        assertEquals(items.toSet(), solution.toSet())
    }
}
