package com.educalab.logicamate.domain.model

/**
 * Las 10 cámaras del Templo de los Patrones (sección 7 del prompt específico).
 * El orden define la posición en el mapa y el orden de desbloqueo por defecto.
 */
enum class ChamberId(val order: Int) {
    ENTRANCE(0),
    PATTERNS(1),
    SEQUENCES(2),
    ANALOGIES(3),
    CLASSIFICATION(4),
    MATRICES(5),
    RELATIONS(6),
    DEDUCTION(7),
    CONSTRUCTOR(8),
    MASTER(9),
}

/** Categoría de razonamiento (sección 34, entidad LogicCategory). */
enum class LogicCategory {
    PATTERN,
    SEQUENCE,
    ANALOGY,
    CLASSIFICATION,
    MATRIX,
    RELATION,
    DEDUCTION,
    CONSTRUCTION,
}

enum class DifficultyLevel { INITIAL, INTERMEDIATE, ADVANCED }

/**
 * Tipos de interacción manipulativa (sección 21). OPTION_SELECT existe pero
 * se usa como mecánica secundaria; el motor de contenido garantiza que no
 * supere el 50% del total (ver ContentBalanceTest).
 */
enum class InteractionType {
    DRAG_PLACE,      // ARRÁSTRALO
    ORDER,           // ORDÉNALO
    BUILD,           // CONSTRÚYELO
    CONNECT,         // CONÉCTALO
    COMPLETE,        // COMPLETA
    DISCOVER,        // DESCÚBRELO
    CLASSIFY,        // CLASIFÍCALO
    CORRECT,         // CORRÍGELO
    ACTIVATE,        // ACTÍVALO
    INVESTIGATE,     // INVESTIGA
    OPTION_SELECT,   // selección múltiple (secundaria)
}

/** Estados visuales de progreso (sección 19 / 28). */
enum class ChamberStatus { LOCKED, AVAILABLE, STARTED, COMPLETED, MASTERED }
