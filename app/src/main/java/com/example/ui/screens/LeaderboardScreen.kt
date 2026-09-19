package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardUser
import com.example.ui.MainViewModel
import com.example.ui.StudyNavDestination
import com.example.ui.theme.StudyAmber
import com.example.ui.theme.StudyCyan
import com.example.ui.theme.StudyEmerald
import com.example.ui.theme.StudyIndigo
import com.example.ui.theme.StudyRose

@Composable
fun LeaderboardScreen(
    viewModel: MainViewModel,
    onNavigate: (StudyNavDestination) -> Unit
) {
    val leaderboardUsers by viewModel.leaderboardFlow.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    var selectedFilter by remember { mutableStateOf("All Maharashtra") }
    var selectedUserForDetail by remember { mutableStateOf<LeaderboardUser?>(null) }
    var cheerFeedback by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(selectedFilter, leaderboardUsers, studentProfile.district) {
        when (selectedFilter) {
            "My District" -> leaderboardUsers.filter { it.district.equals(studentProfile.district, ignoreCase = true) || it.isCurrentUser }
            "Top Accuracy" -> leaderboardUsers.sortedByDescending { it.accuracyPercent }
            "Weekly XP" -> leaderboardUsers.sortedByDescending { it.xpPoints }
            else -> leaderboardUsers
        }
    }

    val topThree = remember(filteredList) {
        filteredList.take(3)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("leaderboard_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(StudyIndigo, Color(0xFF4F46E5), StudyCyan)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🏆", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "MAHARASHTRA SSC 10TH BOARD",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = StudyAmber.copy(alpha = 0.9f)
                                ) {
                                    Text(
                                        text = "LIVE PEER XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "State-Wide Peer Leaderboard",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Compare your syllabus coverage, weekly study hours, and mock accuracy with Class 10 toppers across Pune, Mumbai, Nagpur & all Maharashtra districts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All Maharashtra", "My District", "Weekly XP", "Top Accuracy").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = if (filter == "My District") "${studentProfile.district}" else filter,
                                    fontSize = 12.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Top 3 Podium
            if (topThree.size >= 3) {
                item {
                    PodiumSection(
                        first = topThree[0],
                        second = topThree[1],
                        third = topThree[2],
                        onUserClick = { selectedUserForDetail = it }
                    )
                }
            }

            // User's Position Sticky Card
            item {
                val currentUserRank = leaderboardUsers.find { it.isCurrentUser }
                if (currentUserRank != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${currentUserRank.rank}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${currentUserRank.name} (You)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = StudyEmerald.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = currentUserRank.district,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StudyEmerald,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${currentUserRank.weeklyHours}h this week • ${currentUserRank.accuracyPercent}% Accuracy • ${currentUserRank.xpPoints} XP",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigate(StudyNavDestination.PRACTICE) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("+50 XP Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // List Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Student Rankings (${filteredList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap student for full stats",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Student Cards
            items(filteredList) { user ->
                LeaderboardStudentCard(
                    user = user,
                    onClick = { selectedUserForDetail = user },
                    onCheer = {
                        cheerFeedback = "🌟 Shabash! You cheered for ${user.name}!"
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Cheer Feedback Toast
        AnimatedVisibility(
            visible = cheerFeedback != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cheerFeedback ?: "",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { cheerFeedback = null },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Detail Dialog
        selectedUserForDetail?.let { user ->
            StudentDetailDialog(
                user = user,
                onDismiss = { selectedUserForDetail = null },
                onCheer = {
                    cheerFeedback = "🌟 Sent a Maharashtra Board study cheer to ${user.name}!"
                    selectedUserForDetail = null
                }
            )
        }
    }
}

@Composable
fun PodiumSection(
    first: LeaderboardUser,
    second: LeaderboardUser,
    third: LeaderboardUser,
    onUserClick: (LeaderboardUser) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "State Board Class 10 Toppers",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // #2 Second place
                PodiumColumn(
                    user = second,
                    rank = 2,
                    pedestalHeight = 90,
                    medal = "🥈",
                    color = Color(0xFF94A3B8),
                    onClick = { onUserClick(second) }
                )

                // #1 First place
                PodiumColumn(
                    user = first,
                    rank = 1,
                    pedestalHeight = 120,
                    medal = "🥇",
                    color = StudyAmber,
                    onClick = { onUserClick(first) }
                )

                // #3 Third place
                PodiumColumn(
                    user = third,
                    rank = 3,
                    pedestalHeight = 75,
                    medal = "🥉",
                    color = Color(0xFFB45309),
                    onClick = { onUserClick(third) }
                )
            }
        }
    }
}

@Composable
fun PodiumColumn(
    user: LeaderboardUser,
    rank: Int,
    pedestalHeight: Int,
    medal: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Text(text = medal, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.avatarInitials,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.name.split(" ").firstOrNull() ?: user.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = user.district,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = "${user.xpPoints} XP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal block
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight.dp),
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            color = color.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = color
                )
            }
        }
    }
}

@Composable
fun LeaderboardStudentCard(
    user: LeaderboardUser,
    onClick: () -> Unit,
    onCheer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isCurrentUser)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Surface(
                shape = CircleShape,
                color = when (user.rank) {
                    1 -> StudyAmber.copy(alpha = 0.2f)
                    2 -> Color(0xFF94A3B8).copy(alpha = 0.2f)
                    3 -> Color(0xFFB45309).copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${user.rank}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = when (user.rank) {
                            1 -> StudyAmber
                            2 -> Color(0xFF64748B)
                            3 -> Color(0xFFB45309)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name + if (user.isCurrentUser) " (You)" else "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = user.district,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = user.school,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Stats Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ ${user.weeklyHours}h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🎯 ${user.accuracyPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudyEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🔥 ${user.streakDays}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudyAmber
                    )
                    Text(
                        text = "🌟 ${user.xpPoints} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudyIndigo,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cheer Button
            IconButton(
                onClick = onCheer,
                modifier = Modifier
                    .size(36.dp)
                    .background(StudyAmber.copy(alpha = 0.15f), CircleShape)
            ) {
                Text(text = "👏", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StudentDetailDialog(
    user: LeaderboardUser,
    onDismiss: () -> Unit,
    onCheer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.avatarInitials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rank #${user.rank} in Maharashtra SSC",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🏫 School & Region Details",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.school,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "District: ${user.district} • Medium: ${user.medium}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Core Subject Coverage in Maharashtra Class 10
                Text(
                    text = "Maharashtra SSC Class 10 Syllabus Coverage",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                SubjectCoverageRow("Science 1 (Physics & Chem)", user.science1Coverage, StudyIndigo)
                SubjectCoverageRow("Science 2 (Biology & EV)", user.science2Coverage, StudyEmerald)
                SubjectCoverageRow("Maths 1 (Algebra)", user.algebraCoverage, StudyCyan)
                SubjectCoverageRow("Maths 2 (Geometry)", user.geometryCoverage, StudyAmber)

                // Badges
                if (user.badges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Earned SSC Badges",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        user.badges.forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StudyAmber.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "🎖️ $badge",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudyAmber,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onCheer) {
                Text("Send Shabash! 🌟")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SubjectCoverageRow(subject: String, percent: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = subject, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
