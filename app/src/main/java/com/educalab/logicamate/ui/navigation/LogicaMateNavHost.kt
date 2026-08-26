package com.educalab.logicamate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.ui.screens.analogies.AnalogiesScreen
import com.educalab.logicamate.ui.screens.classification.ClassificationScreen
import com.educalab.logicamate.ui.screens.collection.CollectionScreen
import com.educalab.logicamate.ui.screens.constructor.ConstructorScreen
import com.educalab.logicamate.ui.screens.daily.DailyChallengeScreen
import com.educalab.logicamate.ui.screens.deduction.DeductionScreen
import com.educalab.logicamate.ui.screens.entrance.EntranceScreen
import com.educalab.logicamate.ui.screens.home.HomeMapScreen
import com.educalab.logicamate.ui.screens.master.MasterScreen
import com.educalab.logicamate.ui.screens.matrices.MatricesScreen
import com.educalab.logicamate.ui.screens.patterns.PatternsScreen
import com.educalab.logicamate.ui.screens.profile.ProfileScreen
import com.educalab.logicamate.ui.screens.relations.RelationsScreen
import com.educalab.logicamate.ui.screens.sequences.SequencesScreen

object Routes {
    const val MAP = "map"
    const val PROFILE = "profile"
    const val COLLECTION = "collection"
    const val DAILY = "daily"
    fun forChamber(id: ChamberId) = "chamber/${id.name}"
}

@Composable
fun LogicaMateNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.MAP) {
        composable(Routes.MAP) {
            HomeMapScreen(
                onOpenChamber = { navController.navigate(Routes.forChamber(it)) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenCollection = { navController.navigate(Routes.COLLECTION) },
                onOpenDaily = { navController.navigate(Routes.DAILY) },
            )
        }
        composable(Routes.PROFILE) { ProfileScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.COLLECTION) { CollectionScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.DAILY) { DailyChallengeScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.forChamber(ChamberId.ENTRANCE)) { EntranceScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.PATTERNS)) { PatternsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.SEQUENCES)) { SequencesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.ANALOGIES)) { AnalogiesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.CLASSIFICATION)) { ClassificationScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.MATRICES)) { MatricesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.RELATIONS)) { RelationsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.DEDUCTION)) { DeductionScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.CONSTRUCTOR)) { ConstructorScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.forChamber(ChamberId.MASTER)) { MasterScreen(onBack = { navController.popBackStack() }) }
    }
}
