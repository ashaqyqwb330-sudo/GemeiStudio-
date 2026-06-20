package com.example.data.db

import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val visitDao: VisitDao,
    private val vitalLogDao: VitalLogDao
) {
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarmsFlow()
    val allVisits: Flow<List<VisitEntity>> = visitDao.getAllVisitsFlow()
    val allVitals: Flow<List<VitalLogEntity>> = vitalLogDao.getAllLogsFlow()

    suspend fun insert(alarm: AlarmEntity) {
        alarmDao.insertAlarm(alarm)
    }

    suspend fun delete(id: Int) {
        alarmDao.deleteAlarmById(id)
    }

    suspend fun updateEnabled(id: Int, enabled: Boolean) {
        alarmDao.updateEnabledStatus(id, enabled)
    }

    suspend fun getCount(): Int {
        return alarmDao.getCount()
    }

    // Visits
    suspend fun insertVisit(visit: VisitEntity) {
        visitDao.insertVisit(visit)
    }

    suspend fun deleteVisit(id: Int) {
        visitDao.deleteVisitById(id)
    }

    suspend fun updateVisitStatus(id: Int, status: String, cancelReason: String) {
        visitDao.updateVisitStatus(id, status, cancelReason)
    }

    suspend fun getVisitCount(): Int {
        return visitDao.getCount()
    }

    // Vitals
    suspend fun insertVitalLog(log: VitalLogEntity) {
        vitalLogDao.insertLog(log)
    }

    suspend fun deleteVitalLog(id: Int) {
        vitalLogDao.deleteLogById(id)
    }

    suspend fun getVitalsCount(): Int {
        return vitalLogDao.getCount()
    }
}
