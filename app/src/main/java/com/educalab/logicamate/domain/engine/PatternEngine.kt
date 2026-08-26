package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceSpec

/**
 * Motor de patrones cíclicos (Cámara "Galería de Patrones").
 * Soporta patrones de una sola propiedad (p.ej. color: oro,oro,coral,oro,oro,coral,?)
 * y patrones de doble propiedad donde dos características cambian de forma
 * independiente y cíclica (p.ej. forma y tamaño), tal como exige el prompt
 * específico (sección 10: "esto obliga a observar más de una regla").
 */
object PatternEngine {

    /**
     * Busca el período mínimo p (2..len/2) tal que items[i] == items[i+p] para
     * todo i válido. Devuelve null si no hay ciclo detectable con al menos
     * dos repeticiones completas.
     */
    fun detectCycleLength(items: List<PieceSpec>): Int? {
        val n = items.size
        for (period in 1..n / 2) {
            if (n % period != 0 && n - (n % period) < period * 2) continue
            var matches = true
            for (i in period until n) {
                if (items[i] != items[i - period]) {
                    matches = false
                    break
                }
            }
            // Exigir al menos dos ciclos completos observados para evitar falsos positivos.
            if (matches && n / period >= 2) return period
        }
        return null
    }

    fun nextInCycle(items: List<PieceSpec>): PieceSpec? {
        val period = detectCycleLength(items) ?: return null
        return items[items.size - period]
    }

    fun validateAnswer(items: List<PieceSpec>, candidate: PieceSpec): Boolean =
        nextInCycle(items) == candidate

    /**
     * Variante multi-propiedad: cada propiedad (forma, color, tamaño) se
     * evalúa como su propio flujo cíclico independiente, y la pieza
     * resultante combina el valor esperado de cada una. Permite patrones
     * como "▲ pequeño → ▲ grande → ● pequeño → ● grande → ?" donde la forma
     * cicla cada 2 posiciones y el tamaño alterna cada 1.
     */
    fun nextMultiProperty(items: List<PieceSpec>): PieceSpec? {
        val shapes = items.map { it.shape }
        val colors = items.map { it.color }
        val sizes = items.map { it.size }

        fun <T> nextOfStream(stream: List<T>): T? {
            val n = stream.size
            for (period in 1..n / 2) {
                if (n / period < 2) continue
                var ok = true
                for (i in period until n) if (stream[i] != stream[i - period]) { ok = false; break }
                if (ok) return stream[n - period]
            }
            return null
        }

        val nextShape = nextOfStream(shapes) ?: return null
        val nextColor = nextOfStream(colors) ?: return null
        val nextSize = nextOfStream(sizes) ?: return null
        return PieceSpec(shape = nextShape, color = nextColor, size = nextSize)
    }
}
