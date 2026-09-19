package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.StudyNavDestination
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.FeatureModuleBar
import com.example.ui.screens.*
import com.example.ui.theme.AIStudyOSTheme
import com.example.ui.theme.StudyEmerald
import com.example.ui.theme.StudyIndigo

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val currentDest by viewModel.currentDestination.collectAsState()
            val studentProfile by viewModel.studentProfile.collectAsState()
            val streakDays by viewModel.streakDays.collectAsState()

            var showBoardInfoDialog by remember { mutableStateOf(false) }

            AIStudyOSTheme(darkTheme = isDarkTheme) {
                if (!isLoggedIn) {
                    // Mandatory Login / Registration Screen for Class 10 Maharashtra Board
                    LoginScreen(viewModel = viewModel)
                } else {
                    // Authenticated Maharashtra SSC Student Portal
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            AppTopBar(
                                currentDestination = currentDest,
                                studentProfile = studentProfile,
                                streakDays = streakDays,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDarkTheme() },
                                onLogout = { viewModel.logout() },
                                onBoardInfoClick = { showBoardInfoDialog = true }
                            )
                        },
                        bottomBar = {
                            AppBottomBar(
                                currentDestination = currentDest,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Quick Modules Filter Bar
                            FeatureModuleBar(
                                currentDestination = currentDest,
                                onNavigate = { viewModel.navigateTo(it) }
                            )

                            // Screen Content with smooth animation
                            Crossfade(
                                targetState = currentDest,
                                modifier = Modifier.weight(1f),
                                label = "ScreenTransition"
                            ) { dest ->
                                when (dest) {
                                    StudyNavDestination.DASHBOARD -> DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    StudyNavDestination.LEADERBOARD -> LeaderboardScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    StudyNavDestination.PLANNER -> PlannerScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    StudyNavDestination.PRACTICE -> PracticeScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    StudyNavDestination.AI_TUTOR -> TutorScreen(
                                        viewModel = viewModel
                                    )
                                    StudyNavDestination.AI_NOTES -> NotesScreen(
                                        viewModel = viewModel
                                    )
                                    StudyNavDestination.MISTAKES -> MistakesScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    StudyNavDestination.REVISION -> RevisionScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    StudyNavDestination.FOCUS -> FocusScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    StudyNavDestination.EXAMS -> ExamTrackerScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    StudyNavDestination.WEEKLY_REVIEW -> WeeklyReviewScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                            }
                        }

                        // Maharashtra SSC Board Info Dialog
                        if (showBoardInfoDialog) {
                            AlertDialog(
                                onDismissRequest = { showBoardInfoDialog = false },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Maharashtra SSC Class 10 Portal")
                                    }
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = "👤 Active Student Session",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = studentProfile.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${studentProfile.school} • ${studentProfile.district}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Seat No: ${studentProfile.seatNumber} • Medium: ${studentProfile.medium}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Target Goal: ${studentProfile.targetPercentage}% in Board Exams",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = StudyEmerald
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Aligned strictly to Maharashtra State Board Secondary Education (MSBSHSE) Pune curriculum:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("• Science 1 & 2: 40 + 40 marks (Theory) + 20 (Practical)", fontSize = 12.sp)
                                            Text("• Maths 1 (Algebra) & Maths 2 (Geometry): 40 + 40 marks", fontSize = 12.sp)
                                            Text("• Social Sciences: History/Pol.Sci (40) & Geography (40)", fontSize = 12.sp)
                                            Text("• State-wide peer ranking & district level competition", fontSize = 12.sp)
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(onClick = { showBoardInfoDialog = false }) {
                                        Text("Continue Studying")
                                    }
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = {
                                            showBoardInfoDialog = false
                                            viewModel.logout()
                                        }
                                    ) {
                                        Text("Logout / Switch Student")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
