package com.artemis.pushup1v1.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.artemis.pushup1v1.PoseAnalyzer
import com.artemis.pushup1v1.PushupViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DuelScreen(viewModel: PushupViewModel, onDuelFinished: () -> Unit) {
    val permission = rememberPermissionState(Manifest.permission.CAMERA)
    val phase by viewModel.phase

    LaunchedEffect(Unit) {
        if (!permission.status.isGranted) permission.launchPermissionRequest()
    }
    LaunchedEffect(phase) {
        if (phase == PushupViewModel.Phase.RESULT) onDuelFinished()
    }

    if (!permission.status.isGranted) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Autorise la caméra pour compter les pompes.", fontSize = 18.sp)
            Button(onClick = { permission.launchPermissionRequest() }, Modifier.padding(top = 20.dp)) {
                Text("Autoriser la caméra")
            }
        }
        return
    }

    when (phase) {
        PushupViewModel.Phase.IDLE -> PlayerReadyPanel("Joueur 1", "Place-toi de profil et assure-toi que tout le corps est visible.") {
            viewModel.startPlayer1()
        }
        PushupViewModel.Phase.WAITING_FOR_PLAYER2 -> PlayerReadyPanel(
            "Joueur 2", "Joueur 1 : ${viewModel.player1Reps} pompes. À toi !"
        ) { viewModel.startPlayer2() }
        PushupViewModel.Phase.PLAYER1_TURN, PushupViewModel.Phase.PLAYER2_TURN -> CameraDuelPanel(viewModel)
        PushupViewModel.Phase.RESULT -> Unit
    }
}

@Composable
private fun PlayerReadyPanel(title: String, subtitle: String, onReady: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("PRÊT ?", color = MaterialTheme.colorScheme.secondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp))
            Text(subtitle, fontSize = 15.sp, modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onReady, Modifier.padding(top = 30.dp)) { Text("C'est parti — 30 s") }
        }
    }
}

@Composable
private fun CameraDuelPanel(viewModel: PushupViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val phase by viewModel.phase
    val seconds by viewModel.secondsLeft
    val reps = if (phase == PushupViewModel.Phase.PLAYER1_TURN) viewModel.player1Reps else viewModel.player2Reps
    val playerLabel = if (phase == PushupViewModel.Phase.PLAYER1_TURN) "JOUEUR 1" else "JOUEUR 2"

    key(playerLabel) {
        val analyzer = remember {
            PoseAnalyzer(
                onRepDetected = { viewModel.registerRep() },
                onDownStateChanged = viewModel::setDownState
            )
        }
        DisposableEffect(Unit) {
            onDispose { analyzer.close() }
        }

        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
        DisposableEffect(Unit) {
            onDispose { cameraExecutor.shutdown() }
        }

        Box(Modifier.fillMaxSize()) {
            AndroidView(
                Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val future = ProcessCameraProvider.getInstance(ctx)
                    future.addListener({
                        val provider = future.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setImageQueueDepth(1)
                            .build()
                            .also { useCase ->
                                useCase.setAnalyzer(cameraExecutor) { proxy -> analyzer.analyze(proxy) }
                            }
                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
                        } catch (_: Exception) {
                            // Camera can disappear during navigation; the composable will clean up.
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )

            Column(
                Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(playerLabel, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${seconds}s", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    Text("$reps", color = MaterialTheme.colorScheme.secondary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text("POMPES", color = Color.White, fontSize = 12.sp)
            }

            Text(
                "Garde épaule, coude et poignet visibles",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp)
            )
        }
    }
}
