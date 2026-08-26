package com.educalab.logicamate

import android.content.Context
import com.educalab.logicamate.data.local.DatabaseSeeder
import com.educalab.logicamate.data.local.LogicaMateDatabase
import com.educalab.logicamate.data.repository.ProgressRepository

/**
 * Inyección de dependencias manual (sección 21: "DI manual (sin Hilt)").
 * Objeto singleton simple respaldado por [LogicaMateDatabase.getInstance],
 * que ya gestiona su propio ciclo de vida singleton internamente.
 */
object ServiceLocator {
    private var database: LogicaMateDatabase? = null
    private var progressRepository: ProgressRepository? = null

    fun database(context: Context): LogicaMateDatabase =
        database ?: LogicaMateDatabase.getInstance(context).also { database = it }

    fun progressRepository(context: Context): ProgressRepository =
        progressRepository ?: ProgressRepository(database(context)).also { progressRepository = it }

    fun seeder(context: Context): DatabaseSeeder = DatabaseSeeder(database(context))
}
