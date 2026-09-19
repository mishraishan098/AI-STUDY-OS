package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.QuizQuestion
import com.example.ui.MainViewModel
import com.example.ui.StudyNavDestination
import com.example.ui.theme.StudyAmber
import com.example.ui.theme.StudyCyan
import com.example.ui.theme.StudyEmerald
import com.example.ui.theme.StudyIndigo
import com.example.ui.theme.StudyRose

@Composable
fun PracticeScreen(
    viewModel: MainViewModel,
    onNavigate: (StudyNavDestination) -> Unit
) {
    val quizQuestions by viewModel.quizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuizIndex.collectAsState()
    val selectedOption by viewModel.selectedQuizOption.collectAsState()
    val isSubmitted by viewModel.isOptionSubmitted.collectAsState()
    val quizFinished by viewModel.quizFinished.collectAsState()
    val lastResult by viewModel.lastQuizResult.collectAsState()

    var selectedSubject by remember { mutableStateOf("Science 1") }
    var selectedDifficulty by remember { mutableStateOf("Medium") }

    val currentQ = quizQuestions.getOrNull(currentIndex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("practice_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SSC Board AI Practice Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chapter-wise adaptive testing for Maharashtra SSC Class 10 Board blueprint.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Subject Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Science 1", "Science 2", "Maths 1 (Algebra)", "Maths 2 (Geometry)", "Social Science").forEach { sub ->
                            FilterChip(
                                selected = selectedSubject == sub,
                                onClick = {
                                    selectedSubject = sub
                                    viewModel.startQuiz(sub, "Chapter Drill", selectedDifficulty)
                                },
                                label = { Text(sub, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Difficulty Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Easy", "Medium", "Hard").forEach { diff ->
                            FilterChip(
                                selected = selectedDifficulty == diff,
                                onClick = {
                                    selectedDifficulty = diff
                                    viewModel.startQuiz(selectedSubject, "Chapter 1", diff)
                                },
                                label = { Text(diff, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Active Quiz Question View
        if (!quizFinished && currentQ != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${quizQuestions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = currentQ.conceptTag,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Question Text Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = currentQ.questionText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp
                    )
                }
            }

            // 4 Options
            items(currentQ.options.indices.toList()) { optIdx ->
                val optionText = currentQ.options[optIdx]
                val isSelected = selectedOption == optIdx
                val isCorrect = optIdx == currentQ.correctIndex

                val containerColor = when {
                    isSubmitted && isCorrect -> StudyEmerald.copy(alpha = 0.2f)
                    isSubmitted && isSelected && !isCorrect -> StudyRose.copy(alpha = 0.2f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderColor = when {
                    isSubmitted && isCorrect -> StudyEmerald
                    isSubmitted && isSelected && !isCorrect -> StudyRose
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSubmitted) {
                            viewModel.selectQuizOption(optIdx)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val optionLetter = ('A' + optIdx).toString()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = optionLetter,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSubmitted && isCorrect) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudyEmerald)
                        } else if (isSubmitted && isSelected && !isCorrect) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = StudyRose)
                        }
                    }
                }
            }

            // Explanation Card (Visible after submit)
            if (isSubmitted) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = StudyAmber)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Explanation & Trap Analysis:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentQ.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Action Button: Submit or Next Question
            item {
                if (!isSubmitted) {
                    Button(
                        onClick = { viewModel.submitQuizAnswer() },
                        enabled = selectedOption != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_quiz_answer_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Answer", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextQuizQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("next_quiz_question_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next Question", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        // Post-Quiz Summary Review
        if (quizFinished && lastResult != null) {
            val res = lastResult!!

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (res.accuracyPercent >= 75) StudyEmerald.copy(alpha = 0.2f) else StudyAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (res.accuracyPercent >= 75) "EXCELLENT ACCURACY" else "NEEDS REINFORCEMENT",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (res.accuracyPercent >= 75) StudyEmerald else StudyAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${res.accuracyPercent}% Accuracy",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${res.correctCount} of ${res.totalQuestions} questions correct",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (res.mistakes.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StudyRose.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = StudyRose)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${res.mistakes.size} incorrect questions automatically added to Mistake Book for spaced retry!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startQuiz(selectedSubject, "Chapter 1", selectedDifficulty) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Retake Quiz")
                            }

                            OutlinedButton(
                                onClick = { onNavigate(StudyNavDestination.MISTAKES) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("View Mistakes")
                            }
                        }
                    }
                }
            }
        }
    }
}
