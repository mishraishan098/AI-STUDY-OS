package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RevisionTopicEntity
import com.example.data.model.RevisionStage
import com.example.ui.MainViewModel
import com.example.ui.StudyNavDestination
import com.example.ui.theme.StudyAmber
import com.example.ui.theme.StudyCyan
import com.example.ui.theme.StudyEmerald
import com.example.ui.theme.StudyIndigo

@Composable
fun RevisionScreen(
    viewModel: MainViewModel,
    onNavigate: (StudyNavDestination) -> Unit
) {
    val revisionTopics by viewModel.revisionTopics.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val stages = listOf("All", "LEARN", "PRACTICE", "REVIEW", "MASTER")

    val filteredList = if (selectedFilter == "All") {
        revisionTopics
    } else {
        revisionTopics.filter { it.stage.equals(selectedFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("revision_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Spaced Repetition Pipeline Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Smart Spaced Repetition (SRS)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scientifically combats the Ebbinghaus forgetting curve. Topics advance across 4 retention stages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Stage Visual Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StageBadge("1. Learn", StudyCyan)
                        Text("➔", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        StageBadge("2. Practice", StudyAmber)
                        Text("➔", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        StageBadge("3. Review", Color(0xFFF97316))
                        Text("➔", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        StageBadge("4. Master", StudyEmerald)
                    }
                }
            }
        }

        // Stage Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stages.forEach { st ->
                    FilterChip(
                        selected = selectedFilter == st,
                        onClick = { selectedFilter = st },
                        label = { Text(st.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Topics List
        items(filteredList, key = { it.id }) { topic ->
            RevisionTopicCard(
                topic = topic,
                onAdvance = { viewModel.advanceRevisionStage(topic) },
                onReset = { viewModel.resetRevisionStage(topic) },
                onQuiz = {
                    viewModel.startQuiz(topic.subject, topic.title, "Medium")
                    onNavigate(StudyNavDestination.PRACTICE)
                }
            )
        }
    }
}

@Composable
fun StageBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun RevisionTopicCard(
    topic: RevisionTopicEntity,
    onAdvance: () -> Unit,
    onReset: () -> Unit,
    onQuiz: () -> Unit
) {
    val stageColor = when (topic.stage.uppercase()) {
        "LEARN" -> StudyCyan
        "PRACTICE" -> StudyAmber
        "REVIEW" -> Color(0xFFF97316)
        "MASTER" -> StudyEmerald
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = stageColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = topic.stage,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = stageColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = topic.subject,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Next: ${topic.nextReviewDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Chapter: ${topic.chapter}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mastery progress bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { topic.masteryPercent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                    color = stageColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${topic.masteryPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = stageColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onQuiz,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Topic", style = MaterialTheme.typography.labelSmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (topic.stage.uppercase() != "MASTER") {
                        FilledTonalButton(
                            onClick = onAdvance,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Advance Stage ➔", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
