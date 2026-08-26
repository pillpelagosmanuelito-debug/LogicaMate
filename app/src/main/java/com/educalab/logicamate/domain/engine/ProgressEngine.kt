package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.ChamberStatus

object ProgressEngine {

    data class ChamberProgressInput(
        val chamberId: ChamberId,
        val totalChallenges: Int,
        val completedChallenges: Int,
        val perfectChallenges: Int, // resueltos a la primera y sin pistas
        val previousChamberStatus: ChamberStatus?, // null solo para ENTRANCE
    )

    fun statusFor(input: ChamberProgressInput): ChamberStatus {
        if (input.chamberId != ChamberId.ENTRANCE) {
            val prevOk = input.previousChamberStatus == ChamberStatus.COMPLETED ||
                input.previousChamberStatus == ChamberStatus.MASTERED
            if (!prevOk) return ChamberStatus.LOCKED
        }
        if (input.totalChallenges <= 0) return ChamberStatus.AVAILABLE
        return when {
            input.completedChallenges <= 0 -> ChamberStatus.AVAILABLE
            input.completedChallenges >= input.totalChallenges && input.perfectChallenges >= input.totalChallenges ->
                ChamberStatus.MASTERED
            input.completedChallenges >= input.totalChallenges -> ChamberStatus.COMPLETED
            else -> ChamberStatus.STARTED
        }
    }

    /**
     * La Cámara Maestra requiere que TODAS las demás cámaras (salvo la
     * Entrada y ella misma) estén completadas o dominadas.
     */
    fun isMasterChamberUnlocked(otherChamberStatuses: Map<ChamberId, ChamberStatus>): Boolean {
        val required = ChamberId.entries.filter { it != ChamberId.ENTRANCE && it != ChamberId.MASTER }
        return required.all { id ->
            val s = otherChamberStatuses[id]
            s == ChamberStatus.COMPLETED || s == ChamberStatus.MASTERED
        }
    }

    /** Porcentaje global del templo (0-100), usado en el mapa y el perfil. */
    fun overallProgressPercent(allInputs: List<ChamberProgressInput>): Int {
        val totalAll = allInputs.sumOf { it.totalChallenges }
        if (totalAll == 0) return 0
        val completedAll = allInputs.sumOf { it.completedChallenges.coerceAtMost(it.totalChallenges) }
        return ((completedAll.toDouble() / totalAll) * 100).toInt()
    }
}
