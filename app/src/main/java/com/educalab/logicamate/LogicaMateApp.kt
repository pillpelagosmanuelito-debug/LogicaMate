package com.educalab.logicamate

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LogicaMateApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Sembrado inicial en background: la primera pantalla (Entrada) espera
        // a que termine mediante el estado de carga expuesto en MainActivity.
        applicationScope.launch {
            ServiceLocator.seeder(this@LogicaMateApp).seedIfNeeded()
        }
    }
}
