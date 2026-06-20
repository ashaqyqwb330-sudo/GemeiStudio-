package com.example.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vital_logs")
data class VitalLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val systolic: Int,
    val diastolic: Int,
    val bloodSugar: Int, // mg/dL
    val isPostPrandial: Boolean = false,
    val pulseRate: Int,
    val mood: String, // 😊, 😐, 😣, 🤒, 🤢, 😢
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface VitalLogDao {
    @Query("SELECT * FROM vital_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<VitalLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VitalLogEntity)

    @Query("DELETE FROM vital_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("SELECT COUNT(*) FROM vital_logs")
    suspend fun getCount(): Int
}
