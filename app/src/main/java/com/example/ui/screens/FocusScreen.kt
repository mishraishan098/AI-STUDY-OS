package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.StudyNavDestination
import com.example.ui.theme.StudyAmber
import com.example.ui.theme.StudyCyan
import com.example.ui.theme.StudyEmerald
import com.example.ui.theme.StudyIndigo

@Composable
fun FocusScreen(
    viewModel: MainViewModel,
    onNavigate: (StudyNavDestination) -> Unit
) {
    val totalSeconds by viewModel.focusTotalSeconds.collectAsState()
    val remainingSeconds by viewModel.focusRemainingSeconds.collectAsState()
    val isRunning by viewModel.isFocusRunning.collectAsState()
    val taskTitle by viewModel.focusTaskTitle.collectAsState()
    val ambientSound by viewModel.ambientSoundEnabled.collectAsState()

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("focus_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Task Badge
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONNECTED STUDY TASK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = taskTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Circular Timer Display
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("focus_timer_circle"),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 14.dp,
                color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRunning) "Deep Focus Active" else "Ready to Focus",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isRunning) StudyEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Preset Duration Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple(25, "25m Pomodoro", "25m Focus: Physics"),
                Triple(50, "50m Block", "50m Deep Block: Math/Chem"),
                Triple(10, "10m Break", "Mindfulness Refreshment Break")
            ).forEach { (mins, label, task) ->
                val isSelected = totalSeconds == mins * 60
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setFocusPreset(mins, task) },
                    label = { Text(label) }
                )
            }
        }

        // Controls: Play/Pause, Reset, Ambient sound
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = { viewModel.resetFocusTimer() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Main Play/Pause Button
                Button(
                    onClick = { viewModel.toggleFocusTimer() },
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("focus_toggle_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) StudyAmber else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Ambient sound toggle
                IconButton(
                    onClick = { viewModel.toggleAmbientSound() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (ambientSound) StudyCyan.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        imageVector = if (ambientSound) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Ambient sound",
                        tint = if (ambientSound) StudyCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Note on study time tracking
            Text(
                text = "Completed study sessions are logged automatically to your daily stats.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
