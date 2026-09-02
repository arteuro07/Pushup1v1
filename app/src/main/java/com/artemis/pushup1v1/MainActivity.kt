package com.artemis.pushup1v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.artemis.pushup1v1.ui.screens.DuelScreen
import com.artemis.pushup1v1.ui.screens.HomeScreen
import com.artemis.pushup1v1.ui.screens.ResultScreen
import com.artemis.pushup1v1.ui.theme.PushUp1v1Theme

sealed class Screen {
    data object Home : Screen()
    data object Duel : Screen()
    data object Result : Screen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: PushupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PushUp1v1Theme {
                Surface(Modifier.fillMaxSize()) {
                    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
                    when (screen) {
                        Screen.Home -> HomeScreen {
                            viewModel.reset()
                            screen = Screen.Duel
                        }
                        Screen.Duel -> DuelScreen(viewModel) { screen = Screen.Result }
                        Screen.Result -> ResultScreen(
                            viewModel,
                            onRematch = { viewModel.reset(); screen = Screen.Duel },
                            onHome = { viewModel.reset(); screen = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}
