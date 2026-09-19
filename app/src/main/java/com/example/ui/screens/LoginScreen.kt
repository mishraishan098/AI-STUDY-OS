package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.StudyAmber
import com.example.ui.theme.StudyCyan
import com.example.ui.theme.StudyEmerald
import com.example.ui.theme.StudyIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    var studentName by remember { mutableStateOf("Ishan Mishra") }
    var schoolName by remember { mutableStateOf("Balmohan Vidyamandir, Dadar") }
    var divisionRoll by remember { mutableStateOf("10th-B / Roll #24") }
    var seatNumber by remember { mutableStateOf("M092144") }
    var pin by remember { mutableStateOf("1234") }
    var targetPercent by remember { mutableFloatStateOf(95f) }

    var selectedDistrict by remember { mutableStateOf("Mumbai Suburban") }
    var selectedMedium by remember { mutableStateOf("English Medium") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val maharashtraDistricts = listOf(
        "Mumbai Suburban", "Pune", "Thane", "Nagpur", "Nashik",
        "Chhatrapati Sambhajinagar", "Kolhapur", "Solapur", "Amravati",
        "Sangli", "Satara", "Nanded", "Jalgaon", "Akola"
    )

    val mediumOptions = listOf("English Medium", "Semi-English", "Marathi Medium")

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Official Board Identity Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(StudyIndigo, Color(0xFF4338CA), StudyCyan)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Board Seal",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "महाराष्ट्र राज्य माध्यमिक व उच्च माध्यमिक शिक्षण मंडळ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "MAHARASHTRA STATE BOARD (SSC)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Class 10 AI Study OS Portal",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudyAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login / Register Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Mode Toggle (Login vs Register)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isRegisterMode = false },
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Text(
                                text = "Student Login",
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (!isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isRegisterMode = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Text(
                                text = "New Registration",
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Student Name
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Student Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input"),
                        singleLine = true
                    )

                    // School Name
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("School / Jr. High Name") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // District Selector Chips
                    Column {
                        Text(
                            text = "Maharashtra District",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            maharashtraDistricts.forEach { district ->
                                FilterChip(
                                    selected = selectedDistrict == district,
                                    onClick = { selectedDistrict = district },
                                    label = { Text(district, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Medium of Study
                    Column {
                        Text(
                            text = "Medium of Instruction",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mediumOptions.forEach { medium ->
                                FilterChip(
                                    selected = selectedMedium == medium,
                                    onClick = { selectedMedium = medium },
                                    label = { Text(medium, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Roll / Seat Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = divisionRoll,
                            onValueChange = { divisionRoll = it },
                            label = { Text("Class & Roll") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = seatNumber,
                            onValueChange = { seatNumber = it },
                            label = { Text("SSC Seat No.") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // 4-Digit Security PIN
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4) pin = it },
                        label = { Text("4-Digit Student Security PIN") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Target SSC Percentage Goal
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target SSC Percentage Goal:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${targetPercent.toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = targetPercent,
                            onValueChange = { targetPercent = it },
                            valueRange = 70f..100f,
                            steps = 5
                        )
                    }

                    // Error notice if any
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Login Action Button
                    Button(
                        onClick = {
                            if (studentName.isBlank()) {
                                errorMessage = "Please enter student name."
                            } else if (pin.isBlank()) {
                                errorMessage = "Please enter 4-digit PIN."
                            } else {
                                errorMessage = null
                                viewModel.login(
                                    name = studentName,
                                    school = schoolName,
                                    district = selectedDistrict,
                                    divisionRoll = divisionRoll,
                                    seatNo = seatNumber,
                                    medium = selectedMedium,
                                    pin = pin,
                                    targetPercent = targetPercent.toInt()
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("enter_study_os_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRegisterMode) "Register & Enter SSC OS" else "Login to SSC Study OS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Demo Profiles for instant verification
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚡ Quick Demo Student Profiles (1-Tap Login)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.login(
                                    name = "Ishan Mishra",
                                    school = "Balmohan Vidyamandir, Dadar",
                                    district = "Mumbai Suburban",
                                    divisionRoll = "10th-B / Roll #24",
                                    seatNo = "M092144",
                                    medium = "English Medium",
                                    pin = "1234",
                                    targetPercent = 95
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ishan (Mumbai)", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.login(
                                    name = "Aarav Deshmukh",
                                    school = "Fergusson Junior High, Pune",
                                    district = "Pune",
                                    divisionRoll = "10th-A / Roll #1",
                                    seatNo = "P014285",
                                    medium = "Semi-English",
                                    pin = "1234",
                                    targetPercent = 98
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Aarav (Pune)", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
