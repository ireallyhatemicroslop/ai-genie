package com.example.rtmutexlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Paper = Color(0xFFF4F2EB)
private val Ink = Color(0xFF202923)
private val Muted = Color(0xFF68736C)
private val Green = Color(0xFF176B50)
private val GreenPale = Color(0xFFDCECE2)
private val Red = Color(0xFFBB4F3B)
private val RedPale = Color(0xFFF4E1D9)
private val Panel = Color(0xFFFFFEFA)

private enum class CleanupPath { Buggy, Fixed }

private data class SimulationStage(
    val title: String,
    val commonCopy: String? = null,
    val buggyCopy: String? = null,
    val fixedCopy: String? = null,
)

private val stages = listOf(
    SimulationStage(
        title = "Waiting",
        commonCopy = "Task B is queued for the proxy lock. Its pi_blocked_on field points to that lock.",
    ),
    SimulationStage(
        title = "Rollback begins",
        commonCopy = "Task A called futex_requeue and starts rolling back the proxy-lock attempt. Task A is current; Task B is the waiter.",
    ),
    SimulationStage(
        title = "Cleanup",
        buggyCopy = "Buggy cleanup uses current (Task A) for dequeue-related work, even though Task B owns the waiter state.",
        fixedCopy = "Fixed cleanup uses waiter->task (Task B) for dequeue, PI-lock handling, and priority-chain adjustment.",
    ),
    SimulationStage(
        title = "Result",
        buggyCopy = "Task B can retain a stale pi_blocked_on pointer, and the wrong priority waiter may be adjusted.",
        fixedCopy = "Task B is dequeued under its PI lock and its blocked-on state is cleared. The correct priority waiter is adjusted.",
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = Paper,
                    surface = Panel,
                    primary = Green,
                    onPrimary = Color.White,
                    onBackground = Ink,
                    onSurface = Ink,
                ),
            ) {
                RtmutexLabScreen()
            }
        }
    }
}

@Composable
private fun RtmutexLabScreen() {
    var path by remember { mutableStateOf(CleanupPath.Buggy) }
    var stageIndex by remember { mutableIntStateOf(0) }
    val stage = stages[stageIndex]
    val isBuggy = path == CleanupPath.Buggy
    val finalBuggy = isBuggy && stageIndex == 3
    val hasCleanupStarted = stageIndex >= 2
    val bodyCopy = when {
        stage.commonCopy != null -> stage.commonCopy
        isBuggy -> stage.buggyCopy.orEmpty()
        else -> stage.fixedCopy.orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonoLabel("KERNEL NOTES / 01", Ink)
            MonoLabel("INTERACTIVE EXPLAINER", Muted)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MonoLabel("CVE-2026-43499 · LINUX RTMUTEX", Green)
            Text(
                text = "Waiter is not current.",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 39.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif,
                color = Ink,
            )
            Text(
                text = "Proxy-lock rollback used the caller's task where it needed the lock waiter's task. Compare the buggy path with the fix.",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                lineHeight = 24.sp,
            )
        }

        Surface(color = Color(0xFFFFFDF5), shape = MaterialTheme.shapes.extraSmall) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text("!", color = Red, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MonoLabel("SIMULATION ONLY", Ink)
                    Text(
                        "This app models the reported state mismatch in memory. It does not access the device kernel, trigger the CVE, or test device vulnerability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink,
                        lineHeight = 19.sp,
                    )
                }
            }
        }

        Surface(color = Panel, shape = MaterialTheme.shapes.extraSmall) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Proxy-lock rollback", style = MaterialTheme.typography.titleLarge, color = Ink)
                    MonoLabel("FUTEX_REQUEUE", Muted)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeButton(
                        text = "Buggy",
                        selected = isBuggy,
                        modifier = Modifier.weight(1f),
                        onClick = { path = CleanupPath.Buggy; stageIndex = 0 },
                    )
                    ModeButton(
                        text = "Fixed",
                        selected = !isBuggy,
                        modifier = Modifier.weight(1f),
                        onClick = { path = CleanupPath.Fixed; stageIndex = 0 },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    stages.forEachIndexed { index, _ ->
                        Surface(
                            modifier = Modifier.weight(1f).height(5.dp),
                            color = if (index <= stageIndex) Green else Color(0xFFD5D9D1),
                            shape = MaterialTheme.shapes.extraSmall,
                        ) {}
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    stages.forEach { MonoLabel(it.title.uppercase(), Muted, size = 8) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TaskPanel(
                        role = "CALLER · CURRENT",
                        name = "Task A",
                        state = when {
                            stageIndex < 2 -> "Starts rollback"
                            isBuggy -> "Used for cleanup (wrong task)"
                            else -> "Caller; not the waiter"
                        },
                        tint = if (hasCleanupStarted && isBuggy) RedPale else Panel,
                        modifier = Modifier.weight(1f),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MonoLabel("QUEUE", Muted, size = 8)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            if (finalBuggy) "Task B\nretained" else if (stageIndex == 3) "Empty" else "Task B",
                            modifier = Modifier.background(
                                if (stageIndex == 3 && !isBuggy) GreenPale else Panel,
                            ).padding(horizontal = 7.dp, vertical = 10.dp),
                            color = if (finalBuggy) Red else Green,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                        )
                    }
                    TaskPanel(
                        role = "LOCK WAITER · WAITER->TASK",
                        name = "Task B",
                        state = if (stageIndex == 3 && !isBuggy) "pi_blocked_on = null"
                        else if (finalBuggy) "pi_blocked_on may be stale"
                        else "pi_blocked_on = lock",
                        tint = when {
                            finalBuggy -> RedPale
                            hasCleanupStarted && !isBuggy -> GreenPale
                            else -> Panel
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                Surface(
                    color = Color(0xFFF5F6F1),
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        MonoLabel("STEP ${stageIndex + 1} OF 4 · ${stage.title.uppercase()}", Muted, size = 9)
                        Text(bodyCopy, style = MaterialTheme.typography.bodyMedium, color = Ink, lineHeight = 21.sp)
                    }
                }

                Button(
                    onClick = { stageIndex = if (stageIndex == stages.lastIndex) 0 else stageIndex + 1 },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                ) {
                    Text(if (stageIndex == stages.lastIndex) "Restart sequence" else "Step through")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("What the fix changes", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                "Cleanup consistently targets waiter->task for dequeue, PI-lock handling, blocked-on state, and priority-chain adjustment.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                lineHeight = 21.sp,
            )
        }

        Text(
            "Linux kernel issue, not an Android application API. Device exposure depends on its kernel and vendor patches.",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun ModeButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(42.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink),
        ) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier.height(42.dp)) { Text(text) }
    }
}

@Composable
private fun TaskPanel(
    role: String,
    name: String,
    state: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(116.dp),
        color = tint,
        shape = MaterialTheme.shapes.extraSmall,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            MonoLabel(role, Muted, size = 7)
            Text(name, style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                state,
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun MonoLabel(text: String, color: Color, size: Int = 9) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = size.sp,
        lineHeight = (size + 4).sp,
    )
}