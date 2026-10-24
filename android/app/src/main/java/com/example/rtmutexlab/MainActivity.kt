package com.example.rtmutexlab

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Paper = Color(0xFFF4F2EB)
private val Ink = Color(0xFF202923)
private val Muted = Color(0xFF68736C)
private val Green = Color(0xFF176B50)
private val GreenPale = Color(0xFFDCECE2)
private val GoldPale = Color(0xFFFFF3D5)
private val Panel = Color(0xFFFFFEFA)

private data class GuideStep(
    val title: String,
    val description: String,
)

private val guideSteps = listOf(
    GuideStep(
        "Confirm your exact model",
        "Open Settings > About phone and note the full model number (for example, SM-A037…). A03s variants and carrier builds are not interchangeable. This guide does not certify any variant as unlockable.",
    ),
    GuideStep(
        "Check unlock eligibility first",
        "Look for OEM unlocking in Developer options. If it is missing, unavailable, or the bootloader is carrier/enterprise locked, stop here and confirm eligibility for your exact model and region. Do not try exploits or cross-flash firmware.",
    ),
    GuideStep(
        "Back up and understand the trade-offs",
        "Unlocking the bootloader erases user data. Rooting trips Samsung Knox permanently and can affect warranty, Secure Folder, payments, banking apps, and updates. Back up anything you need before proceeding.",
    ),
    GuideStep(
        "Get matching stock firmware",
        "Use firmware for your exact model, region/CSC, and current build. Keep an untouched copy. Never use another A03s variant's files; mismatched firmware can make the phone unbootable.",
    ),
    GuideStep(
        "Patch with the official Magisk app",
        "Follow Magisk's Samsung-specific installation instructions to patch the AP file from your matching firmware. Keep the resulting patched file private and follow the official guide for the required installation and first-boot steps.",
    ),
    GuideStep(
        "Verify and keep a recovery plan",
        "Only proceed with the official, model-matched instructions. Keep stock firmware available so you can restore the device if needed. This companion does not unlock, patch, flash, or grant root access.",
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
                A03sGuideScreen()
            }
        }
    }
}

@Composable
private fun A03sGuideScreen() {
    var modelNumber by rememberSaveable { mutableStateOf("") }
    var completedSteps by remember { mutableStateOf(setOf<Int>()) }
    val context = LocalContext.current
    val completedCount = completedSteps.size

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
            MonoLabel("DEVICE NOTES / 01", Ink)
            MonoLabel("SAMSUNG A03S", Muted)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MonoLabel("BOOTLOADER + MAGISK GUIDE", Green)
            Text(
                text = "A careful route to root.",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 38.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif,
                color = Ink,
            )
            Text(
                text = "A preparation checklist for Samsung Galaxy A03s. Compatibility depends on your exact model, region, and bootloader status.",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                lineHeight = 24.sp,
            )
        }

        Surface(color = GoldPale, shape = MaterialTheme.shapes.extraSmall) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                MonoLabel("READ BEFORE YOU START", Ink)
                Text(
                    "This is a guide, not a one-tap root or exploit. Bootloader unlocking wipes your data; rooting trips Knox permanently. Some carrier or regional variants may not be unlockable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                    lineHeight = 19.sp,
                )
            }
        }

        Surface(color = Panel, shape = MaterialTheme.shapes.extraSmall) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Your device", style = MaterialTheme.typography.titleLarge, color = Ink)
                OutlinedTextField(
                    value = modelNumber,
                    onValueChange = { modelNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Exact model number") },
                    placeholder = { Text("e.g. SM-A037…") },
                    singleLine = true,
                )
                Text(
                    "Find it in Settings > About phone. Entering a model does not verify that it can be unlocked.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    lineHeight = 18.sp,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Preparation checklist", style = MaterialTheme.typography.titleLarge, color = Ink)
            MonoLabel("$completedCount / ${guideSteps.size}", Green)
        }

        guideSteps.forEachIndexed { index, step ->
            val isCompleted = index in completedSteps
            Surface(
                color = if (isCompleted) GreenPale else Panel,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MonoLabel("STEP ${index + 1}", Green)
                    Text(step.title, style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(
                        step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                        lineHeight = 21.sp,
                    )
                    OutlinedButton(
                        onClick = {
                            completedSteps = if (isCompleted) {
                                completedSteps - index
                            } else {
                                completedSteps + index
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(if (isCompleted) "Marked ready" else "Mark reviewed")
                    }
                }
            }
        }

        Button(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://topjohnwu.github.io/Magisk/install.html")),
                )
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green),
        ) {
            Text("Open official Magisk instructions")
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "No device access · No exploit payloads · No firmware included",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

@Composable
private fun MonoLabel(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 13.sp,
    )
}
