package com.example.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val doctorName: String,
    val specialty: String,
    val clinicName: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM AM/PM
    val reason: String,
    val status: String = "upcoming", // "upcoming", "completed", "cancelled"
    val cancelReason: String = "",
    val recommendations: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits ORDER BY date ASC, time ASC")
    fun getAllVisitsFlow(): Flow<List<VisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity)

    @Query("DELETE FROM visits WHERE id = :id")
    suspend fun deleteVisitById(id: Int)

    @Query("SELECT COUNT(*) FROM visits")
    suspend fun getCount(): Int

    @Query("UPDATE visits SET status = :status, cancelReason = :cancelReason WHERE id = :id")
    suspend fun updateVisitStatus(id: Int, status: String, cancelReason: String)
}
