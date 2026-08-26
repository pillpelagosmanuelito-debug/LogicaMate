package com.educalab.logicamate.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HintEngineTest {

    @Test
    fun `first hint can always be revealed`() {
        assertTrue(HintEngine.canReveal(1, emptySet()))
    }

    @Test
    fun `second hint cannot be revealed before the first`() {
        assertFalse(HintEngine.canReveal(2, emptySet()))
    }

    @Test
    fun `third hint requires the first two already revealed`() {
        assertTrue(HintEngine.canReveal(3, setOf(1, 2)))
        assertFalse(HintEngine.canReveal(3, setOf(1)))
    }

    @Test
    fun `re-revealing an already-seen hint is always allowed`() {
        assertTrue(HintEngine.canReveal(1, setOf(1, 2)))
    }

    @Test
    fun `level outside 1 to 3 is never revealable`() {
        assertFalse(HintEngine.canReveal(0, emptySet()))
        assertFalse(HintEngine.canReveal(4, setOf(1, 2, 3)))
    }

    @Test
    fun `nextRevealableLevel returns the first unseen level in order`() {
        assertEquals(1, HintEngine.nextRevealableLevel(emptySet()))
        assertEquals(2, HintEngine.nextRevealableLevel(setOf(1)))
        assertEquals(3, HintEngine.nextRevealableLevel(setOf(1, 2)))
    }

    @Test
    fun `nextRevealableLevel returns null once all hints are seen`() {
        assertNull(HintEngine.nextRevealableLevel(setOf(1, 2, 3)))
    }
}
