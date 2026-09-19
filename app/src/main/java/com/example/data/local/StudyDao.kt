package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    // Tasks
    @Query("SELECT * FROM study_tasks ORDER BY isCompleted ASC, id ASC")
    fun getAllTasks(): Flow<List<StudyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTaskEntity): Long

    @Update
    suspend fun updateTask(task: StudyTaskEntity)

    @Delete
    suspend fun deleteTask(task: StudyTaskEntity)

    @Query("DELETE FROM study_tasks")
    suspend fun clearTasks()

    // Mistakes
    @Query("SELECT * FROM mistakes ORDER BY isResolved ASC, id DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity): Long

    @Update
    suspend fun updateMistake(mistake: MistakeEntity)

    @Query("DELETE FROM mistakes WHERE id = :id")
    suspend fun deleteMistake(id: Long)

    // Revision
    @Query("SELECT * FROM revision_topics ORDER BY id ASC")
    fun getAllRevisionTopics(): Flow<List<RevisionTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisionTopics(topics: List<RevisionTopicEntity>)

    @Update
    suspend fun updateRevisionTopic(topic: RevisionTopicEntity)

    // Exams
    @Query("SELECT * FROM exams ORDER BY daysRemaining ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    // Study Logs
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC")
    fun getAllStudyLogs(): Flow<List<StudyLogEntity>>

    @Insert
    suspend fun insertStudyLog(log: StudyLogEntity)

    @Query("SELECT SUM(durationMinutes) FROM study_logs")
    fun getTotalStudyMinutes(): Flow<Int?>
}
