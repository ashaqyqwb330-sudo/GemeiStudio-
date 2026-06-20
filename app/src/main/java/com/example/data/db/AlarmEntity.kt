package com.example.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true,
    val repeatDays: String, // Comma-separated or "All Days", "Once", "Weekdays"
    val challengeType: String = "Math", // "Math", "Shake", "Pattern"
    val difficulty: String = "Medium", // "Easy", "Medium", "Hard"
    val isRingingNow: Boolean = false
)

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarmsFlow(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Int)

    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun getCount(): Int

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun updateEnabledStatus(id: Int, enabled: Boolean)
}
