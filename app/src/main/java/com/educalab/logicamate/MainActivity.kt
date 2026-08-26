package com.educalab.logicamate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.educalab.logicamate.ui.navigation.LogicaMateNavHost
import com.educalab.logicamate.ui.theme.LogicaMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LogicaMateTheme {
                LogicaMateNavHost()
            }
        }
    }
}
