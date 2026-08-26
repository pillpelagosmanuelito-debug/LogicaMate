package com.educalab.logicamate.domain.engine

/**
 * Motor de deducción (Cámara "Sala de Deducción"): asigna un elemento único
 * de [objects] a cada elemento de [people] de forma que se cumplan todas las
 * [clues]. Usa fuerza bruta sobre todas las biyecciones posibles — válido
 * porque el prompt limita estos retos a 3-4 personajes/objetos.
 */
object DeductionEngine {

    fun interface Clue {
        /** [assignment] mapea persona -> objeto para una asignación candidata completa. */
        fun holds(assignment: Map<String, String>): Boolean
    }

    // --- Fábricas de pistas comunes, expresadas en términos legibles ---
    object Clues {
        fun doesNotHave(person: String, obj: String) = Clue { it[person] != obj }
        fun has(person: String, obj: String) = Clue { it[person] == obj }
        fun sameAs(personA: String, personB: String) = Clue { it[personA] == it[personB] } // no debería usarse (biyección) pero queda como bloque de construcción
        fun differentPeopleHaveDifferentTraitGroup(person: String, objGroup: Set<String>) =
            Clue { it[person] in objGroup }
    }

    private fun <T> permutations(items: List<T>): Sequence<List<T>> = sequence {
        if (items.isEmpty()) {
            yield(emptyList())
        } else {
            for (i in items.indices) {
                val rest = items.toMutableList().also { it.removeAt(i) }
                for (p in permutations(rest)) yield(listOf(items[i]) + p)
            }
        }
    }

    /** Todas las biyecciones persona->objeto que satisfacen todas las pistas. */
    fun allValidAssignments(people: List<String>, objects: List<String>, clues: List<Clue>): List<Map<String, String>> {
        require(people.size == objects.size) { "Debe existir el mismo número de personas y objetos para una biyección válida." }
        return permutations(objects)
            .map { perm -> people.zip(perm).toMap() }
            .filter { assignment -> clues.all { it.holds(assignment) } }
            .toList()
    }

    /** Devuelve la solución solo si es única — un caso de deducción válido siempre debe serlo. */
    fun uniqueSolution(people: List<String>, objects: List<String>, clues: List<Clue>): Map<String, String>? =
        allValidAssignments(people, objects, clues).singleOrNull()

    fun validateAnswer(
        people: List<String>,
        objects: List<String>,
        clues: List<Clue>,
        candidate: Map<String, String>,
    ): Boolean {
        val solution = uniqueSolution(people, objects, clues) ?: return false
        return solution == candidate
    }
}
