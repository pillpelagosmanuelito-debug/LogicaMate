package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceSpec

/**
 * Motor de analogías (Cámara "Sala de Analogías"): A se transforma en A',
 * ¿en qué se transforma B? Reutiliza el vocabulario de transformaciones de
 * [MatrixEngine.CellTransform] (multiplicar cantidad, ciclar tamaño/color/
 * forma) porque una analogía es, estructuralmente, la misma transformación
 * aplicada a una base distinta — evita duplicar lógica ya probada.
 */
object AnalogyEngine {

    /** Comprueba que [transform] explica realmente A -> A' antes de usarlo. */
    fun transformExplainsPair(a: PieceSpec, aPrime: PieceSpec, transform: MatrixEngine.CellTransform): Boolean =
        transform.apply(a) == aPrime

    fun solve(b: PieceSpec, transform: MatrixEngine.CellTransform): PieceSpec = transform.apply(b)

    fun validateAnswer(
        a: PieceSpec,
        aPrime: PieceSpec,
        transform: MatrixEngine.CellTransform,
        b: PieceSpec,
        candidate: PieceSpec,
    ): Boolean = transformExplainsPair(a, aPrime, transform) && solve(b, transform) == candidate
}
