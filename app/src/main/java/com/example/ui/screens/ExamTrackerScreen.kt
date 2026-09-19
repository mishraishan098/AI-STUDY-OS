package com.example.ui.screens

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
import com.example.data.local.ExamEntity
import com.example.ui.MainViewModel
import com.example.ui.StudyNavDestination
import com.example.ui.theme.StudyAmber
import com.example.ui.theme.StudyCyan
import com.example.ui.theme.StudyEmerald
import com.example.ui.theme.StudyRose

@Composable
fun ExamTrackerScreen(
    viewModel: MainViewModel,
    onNavigate: (StudyNavDestination) -> Unit
) {
    val exams by viewModel.exams.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var examNameInput by remember { mutableStateOf("") }
    var examDateInput by remember { mutableStateOf("") }
    var daysRemainingInput by remember { mutableStateOf("45") }
    var priorityInput by remember { mutableStateOf("High") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_exam_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("exam_tracker_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Exam Countdown & Readiness",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI Study OS recalculates study schedule priority weighting based on days remaining and your weakest areas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(exams, key = { it.id }) { exam ->
                ExamCard(
                    exam = exam,
                    onDelete = { viewModel.removeExam(exam) },
                    onSetGoal = {
                        viewModel.setTargetExam(exam.name)
                        onNavigate(StudyNavDestination.DASHBOARD)
                    }
                )
            }
        }

        // Add Exam Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Upcoming Exam") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = examNameInput,
                            onValueChange = { examNameInput = it },
                            label = { Text("Exam Name (e.g. CBSE 12 Chemistry)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = examDateInput,
                            onValueChange = { examDateInput = it },
                            label = { Text("Exam Date (e.g. 28 Feb 2026)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = daysRemainingInput,
                            onValueChange = { daysRemainingInput = it },
                            label = { Text("Days Remaining") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (examNameInput.isNotBlank()) {
                                viewModel.addExam(
                                    examNameInput,
                                    examDateInput.ifBlank { "TBD" },
                                    daysRemainingInput.toIntOrNull() ?: 30,
                                    priorityInput
                                )
                                examNameInput = ""
                                examDateInput = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Add Exam")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ExamCard(
    exam: ExamEntity,
    onDelete: () -> Unit,
    onSetGoal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (exam.daysRemaining < 50) StudyRose.copy(alpha = 0.15f) else StudyCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${exam.daysRemaining} DAYS REMAINING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (exam.daysRemaining < 50) StudyRose else StudyCyan
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Remove Exam",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exam.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Target Date: ${exam.examDateStr} • Priority: ${exam.priorityLevel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = onSetGoal,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Adjust, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Align AI Timetable with this Exam")
            }
        }
    }
}
