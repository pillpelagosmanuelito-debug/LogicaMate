package com.educalab.logicamate.ui.screens.collection

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.educalab.logicamate.ServiceLocator
import com.educalab.logicamate.data.local.entity.CollectibleItemEntity
import com.educalab.logicamate.ui.common.ChamberTopBar
import com.educalab.logicamate.ui.theme.RuneGold
import com.educalab.logicamate.ui.theme.StoneDeep
import com.educalab.logicamate.ui.theme.StoneMuted
import com.educalab.logicamate.ui.theme.SurfaceCard
import com.educalab.logicamate.ui.theme.TextMuted
import com.educalab.logicamate.ui.theme.TextPrimary
import kotlinx.coroutines.flow.first

@Composable
fun CollectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var items by remember0<List<CollectibleItemEntity>>(emptyList())
    var unlockedIds by remember0<Set<String>>(emptySet())

    LaunchedEffect(Unit) {
        val db = ServiceLocator.database(context.applicationContext)
        items = db.collectibleDao().observeAll().first()
        unlockedIds = db.collectibleDao().getUnlockedIds().toSet()
    }

    Column(Modifier.fillMaxSize().background(StoneDeep)) {
        ChamberTopBar(title = "Tesoros de la Lógica", onBack = onBack)
        Text(
            "Cada tesoro se desbloquea completando cámaras del templo de verdad — nada aquí es decorativo.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(16.dp),
        )
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            items(items) { item ->
                val unlocked = item.id in unlockedIds
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (unlocked) SurfaceCard else StoneMuted.copy(alpha = 0.08f))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(if (unlocked) RuneGold.copy(alpha = 0.2f) else StoneMuted.copy(alpha = 0.1f)),
                    )
                    Text(
                        text = if (unlocked) item.name else "???",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (unlocked) TextPrimary else TextMuted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> remember0(initial: T): androidx.compose.runtime.MutableState<T> {
    return androidx.compose.runtime.remember { mutableStateOf(initial) }
}
