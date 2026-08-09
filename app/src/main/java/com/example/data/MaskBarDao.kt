package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MaskBarDao {
    @Query("SELECT * FROM mask_bars ORDER BY createdAt DESC")
    fun getAllMaskBars(): Flow<List<MaskBarEntity>>

    @Query("SELECT * FROM mask_bars WHERE isEnabled = 1")
    fun getEnabledMaskBars(): Flow<List<MaskBarEntity>>

    @Query("SELECT * FROM mask_bars WHERE isEnabled = 1")
    suspend fun getEnabledMaskBarsList(): List<MaskBarEntity>

    @Query("SELECT * FROM mask_bars WHERE id = :id")
    suspend fun getMaskBarById(id: Long): MaskBarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaskBar(maskBar: MaskBarEntity): Long

    @Update
    suspend fun updateMaskBar(maskBar: MaskBarEntity)

    @Delete
    suspend fun deleteMaskBar(maskBar: MaskBarEntity)

    @Query("DELETE FROM mask_bars WHERE id = :id")
    suspend fun deleteMaskBarById(id: Long)

    @Query("UPDATE mask_bars SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setMaskEnabled(id: Long, isEnabled: Boolean)
}
