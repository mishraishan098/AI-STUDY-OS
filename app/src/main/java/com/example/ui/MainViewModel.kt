package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiStudyRepository
import com.example.data.local.AppDatabase
import com.example.data.local.ExamEntity
import com.example.data.local.MistakeEntity
import com.example.data.local.RevisionTopicEntity
import com.example.data.local.StudyLogEntity
import com.example.data.local.StudyTaskEntity
import com.example.data.model.Flashcard
import com.example.data.model.LeaderboardUser
import com.example.data.model.MistakeRecord
import com.example.data.model.QuizQuestion
import com.example.data.model.QuizResult
import com.example.data.model.RevisionStage
import com.example.data.model.SmartNotesResult
import com.example.data.model.StudentProfile
import com.example.data.model.TutorMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StudyNavDestination(val label: String) {
    DASHBOARD("Home"),
    LEADERBOARD("Leaderboard"),
    PLANNER("Planner"),
    PRACTICE("Practice"),
    AI_TUTOR("AI Tutor"),
    AI_NOTES("Notes"),
    MISTAKES("Mistakes"),
    REVISION("Revision"),
    FOCUS("Focus"),
    EXAMS("Exams"),
    WEEKLY_REVIEW("Review")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.studyDao()
    private val aiRepo = AiStudyRepository()

    // Navigation
    private val _currentDestination = MutableStateFlow(StudyNavDestination.DASHBOARD)
    val currentDestination: StateFlow<StudyNavDestination> = _currentDestination.asStateFlow()

    fun navigateTo(dest: StudyNavDestination) {
        _currentDestination.value = dest
    }

    // Dark Theme Toggle
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Authentication / Login State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _studentProfile = MutableStateFlow(
        StudentProfile(
            name = "Ishan Mishra",
            school = "Balmohan Vidyamandir, Dadar",
            district = "Mumbai Suburban",
            divisionRoll = "10th-B / Roll #24",
            seatNumber = "M092144",
            medium = "English Medium",
            targetExam = "Maharashtra SSC Class 10 (MSBSHSE)",
            targetPercentage = 95,
            xpPoints = 4890
        )
    )
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    fun login(
        name: String,
        school: String,
        district: String,
        divisionRoll: String,
        seatNo: String,
        medium: String,
        pin: String,
        targetPercent: Int = 95
    ) {
        val resolvedName = name.ifBlank { "Ishan Mishra" }
        val resolvedSchool = school.ifBlank { "Balmohan Vidyamandir, Dadar" }
        val resolvedDistrict = district.ifBlank { "Mumbai Suburban" }
        val resolvedRoll = divisionRoll.ifBlank { "10th-A / Roll #1" }
        val resolvedSeat = seatNo.ifBlank { "M084291" }
        val resolvedMedium = medium.ifBlank { "English Medium" }

        _studentProfile.value = StudentProfile(
            name = resolvedName,
            school = resolvedSchool,
            district = resolvedDistrict,
            divisionRoll = resolvedRoll,
            seatNumber = resolvedSeat,
            medium = resolvedMedium,
            targetExam = "Maharashtra SSC Class 10 (MSBSHSE)",
            targetPercentage = targetPercent,
            xpPoints = 4890
        )
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    val studentName: StateFlow<String>
        get() = MutableStateFlow(_studentProfile.value.name).asStateFlow()

    val targetExam: StateFlow<String> = MutableStateFlow("Maharashtra SSC Class 10 (MSBSHSE)").asStateFlow()

    fun setTargetExam(exam: String) {
        // Kept for UI compatibility, locked to Maharashtra SSC Class 10
    }

    private val _streakDays = MutableStateFlow(21)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _studyHoursToday = MutableStateFlow(4.2f)
    val studyHoursToday: StateFlow<Float> = _studyHoursToday.asStateFlow()

    private val _syllabusProgressPercent = MutableStateFlow(78)
    val syllabusProgressPercent: StateFlow<Int> = _syllabusProgressPercent.asStateFlow()

    private val _weakTopics = MutableStateFlow(
        listOf(
            "Science 1: Effects of Electric Current (Motor vs Generator)",
            "Maths 1: Quadratic Equations (Discriminant & Word Problems)",
            "Maths 2: Similarity (BPT Theorem & Areas Ratio)",
            "Science 2: Heredity (Transcription, Translation & Translocation)"
        )
    )
    val weakTopics: StateFlow<List<String>> = _weakTopics.asStateFlow()

    // Database Reactive Flows
    val tasks: StateFlow<List<StudyTaskEntity>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakes: StateFlow<List<MistakeEntity>> = dao.getAllMistakes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revisionTopics: StateFlow<List<RevisionTopicEntity>> = dao.getAllRevisionTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamEntity>> = dao.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Maharashtra Class 10 State-Wide Peer Leaderboard
    private val _leaderboardList = MutableStateFlow(
        listOf(
            LeaderboardUser(
                rank = 1,
                name = "Aarav Deshmukh",
                avatarInitials = "AD",
                district = "Pune",
                school = "Fergusson Junior High, Pune",
                weeklyHours = 48.5f,
                accuracyPercent = 98,
                questionsSolved = 1240,
                streakDays = 28,
                xpPoints = 5420,
                topSubject = "Science 1",
                medium = "Semi-English",
                badges = listOf("State Rank #1", "Kepler's Master", "Centum Aim"),
                science1Coverage = 98,
                science2Coverage = 95,
                algebraCoverage = 99,
                geometryCoverage = 97
            ),
            LeaderboardUser(
                rank = 2,
                name = "Swara Kulkarni",
                avatarInitials = "SK",
                district = "Thane",
                school = "Saraswati Vidyalaya, Thane",
                weeklyHours = 46.0f,
                accuracyPercent = 97,
                questionsSolved = 1180,
                streakDays = 25,
                xpPoints = 5180,
                topSubject = "Maths 1 (Algebra)",
                medium = "English Medium",
                badges = listOf("Algebra Whiz", "Cramer's Rule Ace", "Fast Solver"),
                science1Coverage = 95,
                science2Coverage = 94,
                algebraCoverage = 98,
                geometryCoverage = 94
            ),
            LeaderboardUser(
                rank = 3,
                name = "Ishan Mishra",
                avatarInitials = "IM",
                district = "Mumbai Suburban",
                school = "Balmohan Vidyamandir, Dadar",
                weeklyHours = 42.5f,
                accuracyPercent = 94,
                questionsSolved = 1050,
                streakDays = 21,
                xpPoints = 4890,
                topSubject = "Maths 2 (Geometry)",
                medium = "English Medium",
                isCurrentUser = true,
                badges = listOf("Top 3 Maharashtra", "BPT Scholar", "Consistent Daily"),
                science1Coverage = 92,
                science2Coverage = 88,
                algebraCoverage = 96,
                geometryCoverage = 92
            ),
            LeaderboardUser(
                rank = 4,
                name = "Tanvi Patil",
                avatarInitials = "TP",
                district = "Nashik",
                school = "KTHM High School, Nashik",
                weeklyHours = 39.0f,
                accuracyPercent = 93,
                questionsSolved = 980,
                streakDays = 19,
                xpPoints = 4520,
                topSubject = "Science 2",
                medium = "Marathi Medium",
                badges = listOf("Life Process Ace", "Darwin Topper"),
                science1Coverage = 90,
                science2Coverage = 96,
                algebraCoverage = 91,
                geometryCoverage = 89
            ),
            LeaderboardUser(
                rank = 5,
                name = "Parth Shinde",
                avatarInitials = "PS",
                district = "Nagpur",
                school = "Somani Vidyalaya, Nagpur",
                weeklyHours = 37.5f,
                accuracyPercent = 91,
                questionsSolved = 910,
                streakDays = 16,
                xpPoints = 4310,
                topSubject = "Science 1",
                medium = "Semi-English",
                badges = listOf("Motor Specialist", "Electric Circuit Pro"),
                science1Coverage = 94,
                science2Coverage = 87,
                algebraCoverage = 89,
                geometryCoverage = 88
            ),
            LeaderboardUser(
                rank = 6,
                name = "Snehal Jadhav",
                avatarInitials = "SJ",
                district = "Chhatrapati Sambhajinagar",
                school = "SB High School, CSN",
                weeklyHours = 35.0f,
                accuracyPercent = 92,
                questionsSolved = 860,
                streakDays = 14,
                xpPoints = 4140,
                topSubject = "Maths 1 (Algebra)",
                medium = "English Medium",
                badges = listOf("Quadratic Formula Ace"),
                science1Coverage = 89,
                science2Coverage = 88,
                algebraCoverage = 94,
                geometryCoverage = 86
            ),
            LeaderboardUser(
                rank = 7,
                name = "Aditya Sawant",
                avatarInitials = "AS",
                district = "Kolhapur",
                school = "Rajaram Vidyalaya, Kolhapur",
                weeklyHours = 34.0f,
                accuracyPercent = 89,
                questionsSolved = 820,
                streakDays = 12,
                xpPoints = 3980,
                topSubject = "Maths 2 (Geometry)",
                medium = "Marathi Medium",
                badges = listOf("Apollonius Ace"),
                science1Coverage = 87,
                science2Coverage = 85,
                algebraCoverage = 88,
                geometryCoverage = 93
            ),
            LeaderboardUser(
                rank = 8,
                name = "Riddhi More",
                avatarInitials = "RM",
                district = "Solapur",
                school = "Haribhai Deokaran High School, Solapur",
                weeklyHours = 32.5f,
                accuracyPercent = 88,
                questionsSolved = 790,
                streakDays = 11,
                xpPoints = 3820,
                topSubject = "Social Science",
                medium = "Semi-English",
                badges = listOf("Historiography Topper", "Brazil Expert"),
                science1Coverage = 85,
                science2Coverage = 84,
                algebraCoverage = 86,
                geometryCoverage = 85
            ),
            LeaderboardUser(
                rank = 9,
                name = "Omkar Joshi",
                avatarInitials = "OJ",
                district = "Amravati",
                school = "Manibai Gujarati High School, Amravati",
                weeklyHours = 31.0f,
                accuracyPercent = 87,
                questionsSolved = 740,
                streakDays = 9,
                xpPoints = 3690,
                topSubject = "Science 2",
                medium = "English Medium",
                badges = listOf("Cell Biology Star"),
                science1Coverage = 84,
                science2Coverage = 90,
                algebraCoverage = 85,
                geometryCoverage = 83
            ),
            LeaderboardUser(
                rank = 10,
                name = "Pranav Gaikwad",
                avatarInitials = "PG",
                district = "Satara",
                school = "Dravid High School, Wai, Satara",
                weeklyHours = 29.5f,
                accuracyPercent = 86,
                questionsSolved = 690,
                streakDays = 8,
                xpPoints = 3510,
                topSubject = "Science 1",
                medium = "Marathi Medium",
                badges = listOf("Newtonian Thinker"),
                science1Coverage = 88,
                science2Coverage = 82,
                algebraCoverage = 84,
                geometryCoverage = 82
            )
        )
    )
    val leaderboard: List<LeaderboardUser>
        get() = _leaderboardList.value

    val leaderboardFlow: StateFlow<List<LeaderboardUser>> = _leaderboardList.asStateFlow()

    fun cheerStudent(name: String) {
        // Updates local cheer interaction
    }

    // AI Tutor State
    private val _tutorMessages = MutableStateFlow<List<TutorMessage>>(emptyList())
    val tutorMessages: StateFlow<List<TutorMessage>> = _tutorMessages.asStateFlow()

    private val _isTutorThinking = MutableStateFlow(false)
    val isTutorThinking: StateFlow<Boolean> = _isTutorThinking.asStateFlow()

    fun askTutor(query: String) {
        if (query.isBlank()) return
        val userMsg = TutorMessage(
            id = System.currentTimeMillis().toString(),
            isUser = true,
            messageText = query
        )
        _tutorMessages.value = _tutorMessages.value + userMsg

        viewModelScope.launch {
            _isTutorThinking.value = true
            val aiReply = aiRepo.askTutor(query, "Class 10 Maharashtra State Board SSC")
            _isTutorThinking.value = false
            _tutorMessages.value = _tutorMessages.value + aiReply
        }
    }

    // AI Notes State
    private val _notesResult = MutableStateFlow<SmartNotesResult?>(null)
    val notesResult: StateFlow<SmartNotesResult?> = _notesResult.asStateFlow()

    private val _isGeneratingNotes = MutableStateFlow(false)
    val isGeneratingNotes: StateFlow<Boolean> = _isGeneratingNotes.asStateFlow()

    private val _activeNotesTab = MutableStateFlow(0)
    val activeNotesTab: StateFlow<Int> = _activeNotesTab.asStateFlow()

    fun setNotesTab(index: Int) {
        _activeNotesTab.value = index
    }

    private val _flashcardIndex = MutableStateFlow(0)
    val flashcardIndex: StateFlow<Int> = _flashcardIndex.asStateFlow()

    private val _isFlashcardFlipped = MutableStateFlow(false)
    val isFlashcardFlipped: StateFlow<Boolean> = _isFlashcardFlipped.asStateFlow()

    fun toggleFlashcardFlip() {
        _isFlashcardFlipped.value = !_isFlashcardFlipped.value
    }

    fun nextFlashcard(total: Int) {
        if (total <= 0) return
        _isFlashcardFlipped.value = false
        _flashcardIndex.value = (_flashcardIndex.value + 1) % total
    }

    fun prevFlashcard(total: Int) {
        if (total <= 0) return
        _isFlashcardFlipped.value = false
        _flashcardIndex.value = if (_flashcardIndex.value > 0) _flashcardIndex.value - 1 else total - 1
    }

    fun generateNotes(topic: String, subject: String) {
        viewModelScope.launch {
            _isGeneratingNotes.value = true
            val result = aiRepo.generateSmartNotes(topic, subject)
            _notesResult.value = result
            _activeNotesTab.value = 0
            _flashcardIndex.value = 0
            _isFlashcardFlipped.value = false
            _isGeneratingNotes.value = false
        }
    }

    // AI Practice / Quiz State
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _selectedQuizOption = MutableStateFlow<Int?>(null)
    val selectedQuizOption: StateFlow<Int?> = _selectedQuizOption.asStateFlow()

    private val _isOptionSubmitted = MutableStateFlow(false)
    val isOptionSubmitted: StateFlow<Boolean> = _isOptionSubmitted.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished.asStateFlow()

    private val _lastQuizResult = MutableStateFlow<QuizResult?>(null)
    val lastQuizResult: StateFlow<QuizResult?> = _lastQuizResult.asStateFlow()

    private val currentMistakesBuffer = mutableListOf<MistakeRecord>()

    fun startQuiz(subject: String, chapter: String, difficulty: String) {
        viewModelScope.launch {
            val questions = aiRepo.getChapterQuiz(subject, chapter, difficulty)
            _quizQuestions.value = questions
            _currentQuizIndex.value = 0
            _selectedQuizOption.value = null
            _isOptionSubmitted.value = false
            _quizScore.value = 0
            _quizFinished.value = false
            _lastQuizResult.value = null
            currentMistakesBuffer.clear()
        }
    }

    fun selectQuizOption(index: Int) {
        if (!_isOptionSubmitted.value) {
            _selectedQuizOption.value = index
        }
    }

    fun submitQuizAnswer() {
        val selected = _selectedQuizOption.value ?: return
        val currentQ = _quizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return

        _isOptionSubmitted.value = true
        val isCorrect = selected == currentQ.correctIndex

        if (isCorrect) {
            _quizScore.value = _quizScore.value + 1
        } else {
            val chosen = currentQ.options.getOrElse(selected) { "" }
            val correct = currentQ.options.getOrElse(currentQ.correctIndex) { "" }
            val mistake = MistakeRecord(
                questionText = currentQ.questionText,
                chosenAnswer = chosen,
                correctAnswer = correct,
                explanation = currentQ.explanation,
                conceptTag = currentQ.conceptTag,
                errorType = if (currentQ.questionText.contains("calculate", ignoreCase = true) || currentQ.questionText.contains("value", ignoreCase = true)) "Calculation slip" else "Conceptual trap"
            )
            currentMistakesBuffer.add(mistake)

            // Auto-save to Mistake Book in Room
            viewModelScope.launch {
                dao.insertMistake(
                    MistakeEntity(
                        question = currentQ.questionText,
                        subject = currentQ.subject,
                        chapter = currentQ.chapter,
                        chosenAnswer = chosen,
                        correctAnswer = correct,
                        explanation = currentQ.explanation,
                        errorType = mistake.errorType,
                        dateAdded = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
                    )
                )
            }
        }
    }

    fun nextQuizQuestion() {
        val nextIdx = _currentQuizIndex.value + 1
        if (nextIdx < _quizQuestions.value.size) {
            _currentQuizIndex.value = nextIdx
            _selectedQuizOption.value = null
            _isOptionSubmitted.value = false
        } else {
            val total = _quizQuestions.value.size
            val correct = _quizScore.value
            val accuracy = if (total > 0) (correct * 100) / total else 0
            val result = QuizResult(
                subject = _quizQuestions.value.firstOrNull()?.subject ?: "Science 1",
                chapter = _quizQuestions.value.firstOrNull()?.chapter ?: "Mixed SSC Test",
                difficulty = "Medium",
                totalQuestions = total,
                correctCount = correct,
                accuracyPercent = accuracy,
                timeTakenSeconds = 110,
                mistakes = currentMistakesBuffer.toList()
            )
            _lastQuizResult.value = result
            _quizFinished.value = true
        }
    }

    // Mistake Book actions
    fun resolveMistake(mistake: MistakeEntity) {
        viewModelScope.launch {
            dao.updateMistake(mistake.copy(isResolved = true))
        }
    }

    fun deleteMistake(id: Long) {
        viewModelScope.launch {
            dao.deleteMistake(id)
        }
    }

    // Spaced Revision actions
    fun advanceRevisionStage(topic: RevisionTopicEntity) {
        val nextStage = when (topic.stage) {
            RevisionStage.LEARN.name -> RevisionStage.PRACTICE.name
            RevisionStage.PRACTICE.name -> RevisionStage.REVIEW.name
            RevisionStage.REVIEW.name -> RevisionStage.MASTER.name
            else -> RevisionStage.MASTER.name
        }
        val nextPercent = when (nextStage) {
            RevisionStage.PRACTICE.name -> 60
            RevisionStage.REVIEW.name -> 85
            RevisionStage.MASTER.name -> 100
            else -> 100
        }
        viewModelScope.launch {
            dao.updateRevisionTopic(topic.copy(stage = nextStage, masteryPercent = nextPercent))
        }
    }

    fun resetRevisionStage(topic: RevisionTopicEntity) {
        viewModelScope.launch {
            dao.updateRevisionTopic(topic.copy(stage = RevisionStage.LEARN.name, masteryPercent = 30))
        }
    }

    // Focus Mode (Pomodoro Timer)
    private val _focusTotalSeconds = MutableStateFlow(25 * 60)
    val focusTotalSeconds: StateFlow<Int> = _focusTotalSeconds.asStateFlow()

    private val _focusRemainingSeconds = MutableStateFlow(25 * 60)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _isFocusRunning = MutableStateFlow(false)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    private val _focusTaskTitle = MutableStateFlow("Deep Study: Science 1 Gravitation & Kepler's Laws")
    val focusTaskTitle: StateFlow<String> = _focusTaskTitle.asStateFlow()

    private val _ambientSoundEnabled = MutableStateFlow(true)
    val ambientSoundEnabled: StateFlow<Boolean> = _ambientSoundEnabled.asStateFlow()

    private var timerJob: Job? = null

    fun setFocusPreset(minutes: Int, label: String) {
        timerJob?.cancel()
        _isFocusRunning.value = false
        _focusTotalSeconds.value = minutes * 60
        _focusRemainingSeconds.value = minutes * 60
        _focusTaskTitle.value = label
    }

    fun toggleFocusTimer() {
        if (_isFocusRunning.value) {
            pauseFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    private fun startFocusTimer() {
        _isFocusRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_focusRemainingSeconds.value > 0 && _isFocusRunning.value) {
                delay(1000)
                _focusRemainingSeconds.value = _focusRemainingSeconds.value - 1
            }
            if (_focusRemainingSeconds.value == 0) {
                _isFocusRunning.value = false
                val minutesDone = _focusTotalSeconds.value / 60
                _studyHoursToday.value = _studyHoursToday.value + (minutesDone / 60f)
                dao.insertStudyLog(
                    StudyLogEntity(
                        taskOrSubject = _focusTaskTitle.value,
                        durationMinutes = minutesDone
                    )
                )
            }
        }
    }

    private fun pauseFocusTimer() {
        _isFocusRunning.value = false
        timerJob?.cancel()
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        _focusRemainingSeconds.value = _focusTotalSeconds.value
    }

    fun toggleAmbientSound() {
        _ambientSoundEnabled.value = !_ambientSoundEnabled.value
    }

    // Task Planner actions
    fun toggleTaskComplete(task: StudyTaskEntity) {
        viewModelScope.launch {
            dao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: StudyTaskEntity) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }

    fun regeneratePlan(hours: Int, weakArea: String) {
        viewModelScope.launch {
            dao.clearTasks()
            val newTasks = listOf(
                StudyTaskEntity(
                    title = "Maharashtra SSC Weak Area: $weakArea Core Deep Dive",
                    subject = "High Priority",
                    chapter = weakArea,
                    timeSlot = "06:00 AM - 07:30 AM",
                    durationMinutes = 90,
                    isCompleted = false,
                    isBreak = false,
                    priority = "High",
                    dateStr = "Today"
                ),
                StudyTaskEntity(
                    title = "Active Refreshment & Morning Break",
                    subject = "Break",
                    chapter = "Mindset",
                    timeSlot = "07:30 AM - 07:45 AM",
                    durationMinutes = 15,
                    isCompleted = false,
                    isBreak = true,
                    priority = "Low",
                    dateStr = "Today"
                ),
                StudyTaskEntity(
                    title = "Maharashtra SSC Previous Years Questions (PYQ) Solving",
                    subject = "Practice",
                    chapter = "Speed & Accuracy Drill",
                    timeSlot = "08:00 AM - 09:30 AM",
                    durationMinutes = 90,
                    isCompleted = false,
                    isBreak = false,
                    priority = "High",
                    dateStr = "Today"
                ),
                StudyTaskEntity(
                    title = "SSC Flashcard Recall & Mistake Book Review",
                    subject = "Revision",
                    chapter = "Spaced Repetition",
                    timeSlot = "10:00 AM - 11:00 AM",
                    durationMinutes = 60,
                    isCompleted = false,
                    isBreak = false,
                    priority = "Medium",
                    dateStr = "Today"
                )
            )
            dao.insertTasks(newTasks)
        }
    }

    // Exam Tracker actions
    fun addExam(name: String, dateStr: String, days: Int, priority: String) {
        viewModelScope.launch {
            dao.insertExam(
                ExamEntity(
                    name = name,
                    examDateStr = dateStr,
                    daysRemaining = days,
                    priorityLevel = priority
                )
            )
        }
    }

    fun removeExam(exam: ExamEntity) {
        viewModelScope.launch {
            dao.deleteExam(exam)
        }
    }

    init {
        // Pre-seed sample student data if database is fresh
        seedInitialData()
        // Initialize default notes
        generateNotes("Gravitation & Kepler's Laws", "Science 1")
        // Initialize default quiz
        startQuiz("Science 1", "Gravitation", "Medium")
        // Initialize welcome message in tutor
        _tutorMessages.value = listOf(
            TutorMessage(
                id = "welcome",
                isUser = false,
                messageText = "Namaskar Ishan! I am your AI Study OS Tutor dedicated exclusively to Maharashtra State Board (MSBSHSE) Class 10 (SSC).",
                stepByStepPoints = listOf(
                    "Science 1 & 2: Theorems, Kepler's laws, reaction balancing, electric motor/generator derivations, and biology flowcharts.",
                    "Maths 1 (Algebra): Cramer's rule, Quadratic equations formula method, AP, Financial planning (GST), and probability.",
                    "Maths 2 (Geometry): Similarity proofs (BPT), Pythagoras, Inscribed angle theorem, and constructions.",
                    "Social Science & Languages: India-Brazil comparative geography and board paper answering patterns."
                ),
                practicalAnalogy = "Select any of the quick suggestions below or type any doubt from your Balbharati Class 10 textbook!"
            )
        )
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            launch {
                dao.getAllTasks().collect { currentList ->
                    if (currentList.isEmpty()) {
                        dao.insertTasks(aiRepo.generateDefaultTasks())
                    }
                }
            }
            launch {
                dao.getAllExams().collect { examList ->
                    if (examList.isEmpty()) {
                        dao.insertExams(
                            listOf(
                                ExamEntity(name = "SSC Board: English (Kumarbharati)", examDateStr = "06 Mar", daysRemaining = 42, priorityLevel = "High", targetPercent = 95),
                                ExamEntity(name = "SSC Board: Mathematics Part 1 (Algebra)", examDateStr = "11 Mar", daysRemaining = 47, priorityLevel = "Critical", targetPercent = 100),
                                ExamEntity(name = "SSC Board: Mathematics Part 2 (Geometry)", examDateStr = "14 Mar", daysRemaining = 50, priorityLevel = "Critical", targetPercent = 100),
                                ExamEntity(name = "SSC Board: Science & Tech Part 1", examDateStr = "17 Mar", daysRemaining = 53, priorityLevel = "Critical", targetPercent = 98),
                                ExamEntity(name = "SSC Board: Science & Tech Part 2", examDateStr = "20 Mar", daysRemaining = 56, priorityLevel = "High", targetPercent = 98),
                                ExamEntity(name = "SSC Board: Social Sciences (History & Civics)", examDateStr = "23 Mar", daysRemaining = 59, priorityLevel = "Medium", targetPercent = 95),
                                ExamEntity(name = "SSC Board: Geography (India & Brazil)", examDateStr = "26 Mar", daysRemaining = 62, priorityLevel = "Medium", targetPercent = 96)
                            )
                        )
                    }
                }
            }
            launch {
                dao.getAllRevisionTopics().collect { topicList ->
                    if (topicList.isEmpty()) {
                        dao.insertRevisionTopics(
                            listOf(
                                RevisionTopicEntity(title = "Gravitation & Kepler's Laws", subject = "Science 1", chapter = "Gravitation", stage = RevisionStage.REVIEW.name, nextReviewDate = "Today", masteryPercent = 85),
                                RevisionTopicEntity(title = "Quadratic Equations Formula Method", subject = "Maths 1", chapter = "Algebra", stage = RevisionStage.PRACTICE.name, nextReviewDate = "Tomorrow", masteryPercent = 65),
                                RevisionTopicEntity(title = "Basic Proportionality Theorem (BPT)", subject = "Maths 2", chapter = "Geometry", stage = RevisionStage.LEARN.name, nextReviewDate = "In 2 days", masteryPercent = 40),
                                RevisionTopicEntity(title = "Chemical Reactions & Balancing", subject = "Science 1", chapter = "Chemistry", stage = RevisionStage.MASTER.name, nextReviewDate = "Next week", masteryPercent = 95),
                                RevisionTopicEntity(title = "Heredity & Transcription Flow", subject = "Science 2", chapter = "Biology", stage = RevisionStage.REVIEW.name, nextReviewDate = "In 3 days", masteryPercent = 78),
                                RevisionTopicEntity(title = "Cramer's Rule Determinants", subject = "Maths 1", chapter = "Algebra", stage = RevisionStage.MASTER.name, nextReviewDate = "Next week", masteryPercent = 96)
                            )
                        )
                    }
                }
            }
            launch {
                dao.getAllMistakes().collect { mistakeList ->
                    if (mistakeList.isEmpty()) {
                        dao.insertMistake(
                            MistakeEntity(
                                question = "What is the nature of the roots for the quadratic equation 2x² - 4x + 3 = 0?",
                                subject = "Maths 1 (Algebra)",
                                chapter = "Quadratic Equations",
                                chosenAnswer = "Real and equal",
                                correctAnswer = "Not real numbers (Δ < 0)",
                                explanation = "Discriminant Δ = b² - 4ac = (-4)² - 4(2)(3) = 16 - 24 = -8. Since Δ < 0, the roots are not real numbers.",
                                errorType = "Sign calculation slip in Δ",
                                dateAdded = "Yesterday"
                            )
                        )
                        dao.insertMistake(
                            MistakeEntity(
                                question = "In Fleming's Left Hand Rule, which finger represents the direction of the magnetic field?",
                                subject = "Science 1",
                                chapter = "Effects of Electric Current",
                                chosenAnswer = "Middle finger",
                                correctAnswer = "Forefinger / Index finger",
                                explanation = "Remember: Thumb = Force/Motion, Forefinger = Magnetic Field (Mother/Field), Middle finger = Current (Child/Current).",
                                errorType = "Conceptual rule confusion",
                                dateAdded = "2 days ago"
                            )
                        )
                        dao.insertMistake(
                            MistakeEntity(
                                question = "In an arithmetic progression, if a = 2 and d = 2.5, find t₁₀.",
                                subject = "Maths 1 (Algebra)",
                                chapter = "Arithmetic Progression",
                                chosenAnswer = "27",
                                correctAnswer = "24.5",
                                explanation = "t₁₀ = a + (10 - 1)·d = 2 + 9 × 2.5 = 2 + 22.5 = 24.5.",
                                errorType = "Calculation slip",
                                dateAdded = "3 days ago"
                            )
                        )
                    }
                }
            }
        }
    }
}
