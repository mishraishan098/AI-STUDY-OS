package com.example.data.model

data class StudentProfile(
    val name: String = "Ishan Mishra",
    val school: String = "Balmohan Vidyamandir, Dadar",
    val district: String = "Mumbai Suburban",
    val divisionRoll: String = "10th-A / Roll #24",
    val seatNumber: String = "M092144",
    val medium: String = "English Medium", // "English Medium", "Semi-English", "Marathi Medium"
    val targetExam: String = "Maharashtra SSC Class 10 (MSBSHSE)",
    val targetPercentage: Int = 95,
    val xpPoints: Int = 4890
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val avatarInitials: String,
    val district: String,
    val school: String,
    val weeklyHours: Float,
    val accuracyPercent: Int = 90,
    val questionsSolved: Int = 500,
    val streakDays: Int,
    val xpPoints: Int,
    val topSubject: String = "Science 1",
    val medium: String = "English Medium",
    val isCurrentUser: Boolean = false,
    val badges: List<String> = emptyList(),
    val science1Coverage: Int = 92,
    val science2Coverage: Int = 88,
    val algebraCoverage: Int = 95,
    val geometryCoverage: Int = 90
)

data class QuizQuestion(
    val id: Int,
    val subject: String,
    val chapter: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val conceptTag: String
)

data class QuizResult(
    val subject: String,
    val chapter: String,
    val difficulty: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val accuracyPercent: Int,
    val timeTakenSeconds: Int,
    val mistakes: List<MistakeRecord>
)

data class MistakeRecord(
    val questionText: String,
    val chosenAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val conceptTag: String,
    val errorType: String
)

data class SmartNotesResult(
    val topic: String,
    val subject: String,
    val summaryPoints: List<String>,
    val definitions: List<Pair<String, String>>, // term to definition
    val formulas: List<Pair<String, String>>, // formula name to formula
    val flashcards: List<Flashcard>,
    val mcqs: List<QuizQuestion>,
    val revisionQuestions: List<String>
)

data class Flashcard(
    val question: String,
    val answer: String,
    val category: String
)

data class TutorMessage(
    val id: String,
    val isUser: Boolean,
    val messageText: String,
    val stepByStepPoints: List<String> = emptyList(),
    val practicalAnalogy: String = "",
    val formulaShortcut: String = "",
    val practiceQuestion: String = "",
    val practiceAnswer: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class RevisionStage(val label: String, val colorHex: Long) {
    LEARN("Learn", 0xFF0284C7),       // Sky blue
    PRACTICE("Practice", 0xFFEAB308), // Amber
    REVIEW("Review", 0xFFF97316),     // Orange
    MASTER("Master", 0xFF10B981)      // Emerald
}

