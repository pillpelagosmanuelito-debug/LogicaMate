package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceColor
import com.educalab.logicamate.domain.model.PieceSize
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape

/**
 * Motor de matrices lógicas (Cámara "Sala de Matrices"). Una matriz se
 * construye a partir de una fila base y una transformación consistente que
 * se aplica repetidamente fila a fila (transform^r sobre la fila base). Esto
 * garantiza que exista una única celda correcta para cualquier posición
 * marcada como incógnita, sin importar cuál sea.
 */
object MatrixEngine {

    sealed class CellTransform {
        abstract fun apply(p: PieceSpec): PieceSpec
        abstract fun applyTimes(p: PieceSpec, times: Int): PieceSpec

        data class MultiplyCount(val factor: Int) : CellTransform() {
            override fun apply(p: PieceSpec) = p.copy(count = p.count * factor)
            override fun applyTimes(p: PieceSpec, times: Int): PieceSpec {
                var cur = p
                repeat(times) { cur = apply(cur) }
                return cur
            }
        }

        data class CycleSize(val order: List<PieceSize> = listOf(PieceSize.SMALL, PieceSize.MEDIUM, PieceSize.LARGE)) : CellTransform() {
            override fun apply(p: PieceSpec): PieceSpec {
                val idx = (order.indexOf(p.size) + 1) % order.size
                return p.copy(size = order[idx])
            }
            override fun applyTimes(p: PieceSpec, times: Int): PieceSpec {
                val idx = (order.indexOf(p.size) + times).mod(order.size)
                return p.copy(size = order[idx])
            }
        }

        data class CycleColor(val order: List<PieceColor>) : CellTransform() {
            override fun apply(p: PieceSpec): PieceSpec {
                val idx = (order.indexOf(p.color) + 1) % order.size
                return p.copy(color = order[idx])
            }
            override fun applyTimes(p: PieceSpec, times: Int): PieceSpec {
                val idx = (order.indexOf(p.color) + times).mod(order.size)
                return p.copy(color = order[idx])
            }
        }

        data class CycleShape(val order: List<Shape>) : CellTransform() {
            override fun apply(p: PieceSpec): PieceSpec {
                val idx = (order.indexOf(p.shape) + 1) % order.size
                return p.copy(shape = order[idx])
            }
            override fun applyTimes(p: PieceSpec, times: Int): PieceSpec {
                val idx = (order.indexOf(p.shape) + times).mod(order.size)
                return p.copy(shape = order[idx])
            }
        }
    }

    /** Construye una matriz de [rows] x baseRow.size aplicando [transform] r veces a cada fila. */
    fun buildMatrix(baseRow: List<PieceSpec>, transform: CellTransform, rows: Int): List<List<PieceSpec>> =
        (0 until rows).map { r -> baseRow.map { transform.applyTimes(it, r) } }

    fun solveCell(baseRow: List<PieceSpec>, transform: CellTransform, row: Int, col: Int): PieceSpec =
        transform.applyTimes(baseRow[col], row)

    fun validateAnswer(baseRow: List<PieceSpec>, transform: CellTransform, row: Int, col: Int, candidate: PieceSpec): Boolean =
        solveCell(baseRow, transform, row, col) == candidate
}
