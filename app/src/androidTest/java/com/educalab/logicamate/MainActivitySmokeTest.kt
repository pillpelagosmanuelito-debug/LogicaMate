package com.educalab.logicamate

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba de humo: arranca MainActivity de extremo a extremo (Application ->
 * seeding de Room -> primera composición) y comprueba que el mapa del
 * templo aparece sin crashear. No sustituye una ejecución real en un
 * dispositivo/emulador (ver docs/BUILD_REPORT.md: este entorno no tiene
 * Android SDK), pero es la prueba que correría en CI vía
 * `./gradlew connectedDebugAndroidTest` o el workflow de GitHub Actions incluido.
 */
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndShowsTempleMap() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("Templo", substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
