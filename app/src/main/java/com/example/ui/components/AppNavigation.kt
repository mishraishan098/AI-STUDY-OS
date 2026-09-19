package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile
import com.example.ui.StudyNavDestination
import com.example.ui.theme.StudyAmber
import com.example.ui.theme.StudyCyan
import com.example.ui.theme.StudyEmerald
import com.example.ui.theme.StudyIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentDestination: StudyNavDestination,
    studentProfile: StudentProfile,
    streakDays: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onBoardInfoClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Maharashtra SSC Pill
            Column(modifier = Modifier.clickable { onBoardInfoClick() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "AI Study OS",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Study OS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StudyIndigo.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "MSBSHSE",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudyIndigo,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Class 10 SSC • ${studentProfile.name.split(" ").firstOrNull() ?: studentProfile.name} (${studentProfile.district})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Board Info",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Right side: Streak pill + Theme toggle + Logout / Switch Profile
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Streak Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudyAmber.copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$streakDays d",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StudyAmber
                        )
                    }
                }

                // Theme Toggle Icon
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Logout / Switch Student Button
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout / Switch Student",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AppBottomBar(
    currentDestination: StudyNavDestination,
    onNavigate: (StudyNavDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentDestination == StudyNavDestination.DASHBOARD,
            onClick = { onNavigate(StudyNavDestination.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == StudyNavDestination.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentDestination == StudyNavDestination.LEADERBOARD,
            onClick = { onNavigate(StudyNavDestination.LEADERBOARD) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == StudyNavDestination.LEADERBOARD) Icons.Filled.Leaderboard else Icons.Outlined.Leaderboard,
                    contentDescription = "Leaderboard"
                )
            },
            label = { Text("Board") }
        )

        NavigationBarItem(
            selected = currentDestination == StudyNavDestination.PLANNER,
            onClick = { onNavigate(StudyNavDestination.PLANNER) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == StudyNavDestination.PLANNER) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Plan"
                )
            },
            label = { Text("Plan") }
        )

        NavigationBarItem(
            selected = currentDestination == StudyNavDestination.PRACTICE,
            onClick = { onNavigate(StudyNavDestination.PRACTICE) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == StudyNavDestination.PRACTICE) Icons.Filled.Quiz else Icons.Outlined.Quiz,
                    contentDescription = "Practice"
                )
            },
            label = { Text("Practice") }
        )

        NavigationBarItem(
            selected = currentDestination == StudyNavDestination.AI_TUTOR,
            onClick = { onNavigate(StudyNavDestination.AI_TUTOR) },
            icon = {
                Icon(
                    imageVector = if (currentDestination == StudyNavDestination.AI_TUTOR) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                    contentDescription = "AI Tutor"
                )
            },
            label = { Text("Tutor") }
        )
    }
}
