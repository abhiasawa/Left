package com.timeleft.data.repository

import com.timeleft.data.db.CustomDateDao
import com.timeleft.data.db.CustomDateEntity
import com.timeleft.domain.models.CustomDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TimeRepository(private val customDateDao: CustomDateDao) {

    val customDates: Flow<List<CustomDate>> = customDateDao.getAllCustomDates()
        .map { entities -> entities.map { it.toDomain() } }

    suspend fun addCustomDate(customDate: CustomDate): Long {
        return customDateDao.insertCustomDate(CustomDateEntity.fromDomain(customDate))
    }

    suspend fun updateCustomDate(customDate: CustomDate) {
        customDateDao.updateCustomDate(CustomDateEntity.fromDomain(customDate))
    }

    suspend fun deleteCustomDate(id: Int) {
        customDateDao.deleteById(id)
    }

    suspend fun getCustomDate(id: Int): CustomDate? {
        return customDateDao.getCustomDateById(id)?.toDomain()
    }

    suspend fun getCustomDateCount(): Int {
        return customDateDao.getCount()
    }
}
