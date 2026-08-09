package com.example.data

import kotlinx.coroutines.flow.Flow

class MaskBarRepository(private val maskBarDao: MaskBarDao) {
    val allMasks: Flow<List<MaskBarEntity>> = maskBarDao.getAllMaskBars()
    val enabledMasks: Flow<List<MaskBarEntity>> = maskBarDao.getEnabledMaskBars()

    suspend fun getEnabledMasksList(): List<MaskBarEntity> = maskBarDao.getEnabledMaskBarsList()

    suspend fun getMaskById(id: Long): MaskBarEntity? = maskBarDao.getMaskBarById(id)

    suspend fun insert(mask: MaskBarEntity): Long = maskBarDao.insertMaskBar(mask)

    suspend fun update(mask: MaskBarEntity) = maskBarDao.updateMaskBar(mask)

    suspend fun delete(mask: MaskBarEntity) = maskBarDao.deleteMaskBar(mask)

    suspend fun setEnabled(id: Long, isEnabled: Boolean) = maskBarDao.setMaskEnabled(id, isEnabled)
}
