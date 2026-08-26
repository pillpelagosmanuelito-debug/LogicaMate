package com.educalab.logicamate.data.local

import com.educalab.logicamate.data.local.entity.BadgeEntity
import com.educalab.logicamate.data.local.entity.CollectibleItemEntity
import com.educalab.logicamate.data.local.entity.KeyFragmentEntity
import com.educalab.logicamate.data.local.entity.LogicCategoryEntity
import com.educalab.logicamate.data.local.entity.LogicChamberEntity
import com.educalab.logicamate.data.local.entity.ProgressEntity
import com.educalab.logicamate.data.local.entity.UserProfileEntity
import com.educalab.logicamate.data.local.entity.UserStatsEntity
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.ChamberStatus
import com.educalab.logicamate.domain.model.LogicCategory
import com.educalab.logicamate.seed.SeedContent

/**
 * Puebla la base de datos vacía con el contenido semilla (sección 24/40).
 * Idempotente: usa REPLACE en las inserciones y comprueba `challengeDao().count()`
 * antes de volver a insertar el banco completo de desafíos, así que puede
 * llamarse de forma segura en cada arranque de la app.
 */
class DatabaseSeeder(private val db: LogicaMateDatabase) {

    suspend fun seedIfNeeded() {
        seedChambers()
        seedCategories()
        seedBadges()
        seedKeyFragments()
        seedCollectibles()
        ensureDefaultProfile()
        if (db.challengeDao().count() == 0) {
            seedChallenges()
        }
        ensureProgressRows()
    }

    private fun chamberDisplayName(id: ChamberId) = when (id) {
        ChamberId.ENTRANCE -> "Entrada al Templo"
        ChamberId.PATTERNS -> "Galería de Patrones"
        ChamberId.SEQUENCES -> "Pasadizo de Secuencias"
        ChamberId.ANALOGIES -> "Sala de Analogías"
        ChamberId.CLASSIFICATION -> "Sala de Clasificación"
        ChamberId.MATRICES -> "Sala de Matrices"
        ChamberId.RELATIONS -> "Sala de Relaciones"
        ChamberId.DEDUCTION -> "Sala de Deducción"
        ChamberId.CONSTRUCTOR -> "Taller Constructor"
        ChamberId.MASTER -> "Cámara Maestra"
    }

    private fun chamberIcon(id: ChamberId) = "ic_chamber_${id.name.lowercase()}"

    private fun chamberFlavor(id: ChamberId) = when (id) {
        ChamberId.ENTRANCE -> "Tres símbolos, una puerta. Observa antes de tocar nada."
        ChamberId.PATTERNS -> "Los mosaicos del templo esconden ritmos que se repiten."
        ChamberId.SEQUENCES -> "Un pasadizo de casillas que avanzan siguiendo una regla."
        ChamberId.ANALOGIES -> "Un mecanismo transforma objetos siempre de la misma forma."
        ChamberId.CLASSIFICATION -> "Portales que solo aceptan un tipo de símbolo cada uno."
        ChamberId.MATRICES -> "Un panel mural con una pieza que falta."
        ChamberId.RELATIONS -> "Un mapa de posiciones y caminos por descubrir."
        ChamberId.DEDUCTION -> "Pistas dispersas por una sala de investigación."
        ChamberId.CONSTRUCTOR -> "Aquí no resuelves: construyes tus propias reglas."
        ChamberId.MASTER -> "Todo lo aprendido, a la vez."
    }

    private suspend fun seedChambers() {
        val chambers = ChamberId.entries.map { id ->
            LogicChamberEntity(
                id = id.name,
                displayName = chamberDisplayName(id),
                orderIndex = id.order,
                iconRes = chamberIcon(id),
                flavorText = chamberFlavor(id),
            )
        }
        db.chamberDao().insertAll(chambers)
    }

    private suspend fun seedCategories() {
        val names = mapOf(
            LogicCategory.PATTERN to "Patrones",
            LogicCategory.SEQUENCE to "Secuencias",
            LogicCategory.ANALOGY to "Analogías",
            LogicCategory.CLASSIFICATION to "Clasificación",
            LogicCategory.MATRIX to "Matrices",
            LogicCategory.RELATION to "Relaciones",
            LogicCategory.DEDUCTION to "Deducción",
            LogicCategory.CONSTRUCTION to "Construcción",
        )
        val entities = names.map { (cat, label) ->
            LogicCategoryEntity(id = cat.name, displayName = label, iconRes = "ic_chamber_${cat.name.lowercase()}")
        }
        db.categoryDao().insertAll(entities)
    }

    private suspend fun seedBadges() {
        val badges = listOf(
            Triple("PRIMER_MECANISMO", "Primer Mecanismo", "Resolviste tu primer desafío del templo."),
            Triple("CAZADOR_DE_PATRONES", "Cazador de Patrones", "Descubriste 12 patrones ocultos."),
            Triple("MAESTRO_DE_SECUENCIAS", "Maestro de Secuencias", "Completaste 12 secuencias del pasadizo."),
            Triple("DETECTIVE_DE_RELACIONES", "Detective de Relaciones", "Resolviste 8 mapas de posiciones."),
            Triple("CONSTRUCTOR_LOGICO", "Constructor Lógico", "Construiste 6 patrones propios válidos."),
            Triple("EXPLORADOR_DE_MATRICES", "Explorador de Matrices", "Completaste 10 paneles murales."),
            Triple("GRAN_DEDUCIDOR", "Gran Deducidor", "Resolviste 8 investigaciones completas."),
            Triple("MAESTRO_DEL_TEMPLO", "Maestro del Templo", "Reuniste todos los fragmentos de la Llave Lógica."),
        )
        val entities = badges.map { (id, name, desc) ->
            BadgeEntity(id = id, name = name, description = desc, iconRes = "ic_badge_${id.lowercase()}", criteriaDescription = desc)
        }
        db.gamificationDao().insertBadges(entities)
    }

    private suspend fun seedKeyFragments() {
        // Un fragmento por cada cámara "de contenido" (se excluyen Entrada y Maestra).
        val grantingChambers = listOf(
            ChamberId.PATTERNS, ChamberId.SEQUENCES, ChamberId.ANALOGIES, ChamberId.CLASSIFICATION,
            ChamberId.MATRICES, ChamberId.RELATIONS, ChamberId.DEDUCTION, ChamberId.CONSTRUCTOR,
        )
        val fragments = grantingChambers.mapIndexed { idx, chamber ->
            KeyFragmentEntity(
                id = "FRAG_${chamber.name}",
                name = "Fragmento de ${chamberDisplayName(chamber)}",
                chamberId = chamber.name,
                orderIndex = idx,
                shapeDescriptor = "ic_key_fragment",
            )
        }
        db.keyFragmentDao().insertAll(fragments)
    }

    private suspend fun seedCollectibles() {
        // Colección "Tesoros de la Lógica" (sección 29): 2-3 tesoros por cámara de contenido.
        data class Tpl(val suffix: String, val label: String, val icon: String)
        val templates = listOf(
            Tpl("CRISTAL", "Cristal de", "ic_collectible_cristal"),
            Tpl("ENGRANAJE", "Engranaje de", "ic_collectible_engranaje"),
            Tpl("PLACA", "Placa de", "ic_collectible_placa"),
        )
        val chambers = listOf(
            ChamberId.PATTERNS, ChamberId.SEQUENCES, ChamberId.ANALOGIES, ChamberId.CLASSIFICATION,
            ChamberId.MATRICES, ChamberId.RELATIONS, ChamberId.DEDUCTION, ChamberId.CONSTRUCTOR,
        )
        val items = chambers.flatMap { chamber ->
            templates.map { tpl ->
                CollectibleItemEntity(
                    id = "TESORO_${chamber.name}_${tpl.suffix}",
                    name = "${tpl.label} ${chamberDisplayName(chamber)}",
                    description = "Un tesoro del explorador lógico, hallado en ${chamberDisplayName(chamber)}.",
                    chamberId = chamber.name,
                    iconRes = tpl.icon,
                )
            }
        }
        db.collectibleDao().insertAll(items)
    }

    private suspend fun ensureDefaultProfile() {
        if (db.userProfileDao().get() == null) {
            db.userProfileDao().insert(
                UserProfileEntity(id = 1L, alias = "Explorador", avatarId = 0, createdAtMillis = System.currentTimeMillis()),
            )
            db.gamificationDao().upsertStats(UserStatsEntity(userProfileId = 1L, totalXp = 0, currentStreak = 0, lastActiveDay = null))
        }
    }

    private suspend fun seedChallenges() {
        SeedContent.all.forEachIndexed { globalIdx, challenge ->
            val rows = challenge.toRows(orderIndex = globalIdx)
            db.challengeDao().insertChallenges(listOf(rows.challenge))
            db.challengeDao().insertItems(rows.items)
            db.challengeDao().insertRules(rows.rules)
            db.challengeDao().insertHints(rows.hints)
        }
    }

    private suspend fun ensureProgressRows() {
        for (chamber in ChamberId.entries) {
            val existing = db.progressDao().get(chamber.name)
            if (existing == null) {
                val total = db.challengeDao().countByChamber(chamber.name)
                val status = if (chamber == ChamberId.ENTRANCE) ChamberStatus.AVAILABLE else ChamberStatus.LOCKED
                db.progressDao().upsert(
                    ProgressEntity(
                        userProfileId = 1L,
                        chamberId = chamber.name,
                        status = status.name,
                        challengesCompleted = 0,
                        perfectChallenges = 0,
                        totalChallenges = total,
                        xpEarnedInChamber = 0,
                    ),
                )
            }
        }
    }
}
