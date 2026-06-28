package com.example.data

import kotlinx.coroutines.flow.Flow

class FoodSafetyRepository(private val dao: FoodSafetyDao) {
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val scanHistory: Flow<List<ScanHistory>> = dao.getScanHistory()

    suspend fun saveUserProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    suspend fun addScanRecord(scan: ScanHistory) {
        dao.insertScan(scan)
    }

    suspend fun deleteScanRecord(id: Long) {
        dao.deleteScanById(id)
    }

    suspend fun clearHistory() {
        dao.clearScanHistory()
    }
}
