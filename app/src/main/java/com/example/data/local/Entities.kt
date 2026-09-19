package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val chapter: String,
    val timeSlot: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val isBreak: Boolean = false,
    val priority: String = "Medium", // High, Medium, Low
    val dateStr: String
)

@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val subject: String,
    val chapter: String,
    val chosenAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val errorType: String, // Conceptual, Calculation, Formula Confusion
    val dateAdded: String,
    val isResolved: Boolean = false
)

@Entity(tableName = "revision_topics")
data class RevisionTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val chapter: String,
    val stage: String, // LEARN, PRACTICE, REVIEW, MASTER
    val nextReviewDate: String,
    val masteryPercent: Int = 30
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val examDateStr: String,
    val daysRemaining: Int,
    val priorityLevel: String, // Critical, High, Medium
    val targetPercent: Int = 95
)

@Entity(tableName = "study_logs")
data class StudyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskOrSubject: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
