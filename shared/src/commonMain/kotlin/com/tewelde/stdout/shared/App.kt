package com.tewelde.stdout.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slack.circuit.foundation.Circuit
import com.tewelde.stdout.common.di.UiScope
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


interface AppUi {
    @Composable
    fun Content(
        onRootPop: () -> Unit,
        modifier: Modifier = Modifier,
    )
}

@Inject
@SingleIn(UiScope::class)
@ContributesBinding(UiScope::class)
class StdoutApp(
    private val circuit: Circuit,
) : AppUi {

    @Composable
    override fun Content(onRootPop: () -> Unit, modifier: Modifier) {
        App()
    }
}

@Composable
fun App() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("APP")
    }
}