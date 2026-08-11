package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mask_bars")
data class MaskBarEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isVertical: Boolean = true,
    val xPosRatio: Float = 0.5f,
    val yPosRatio: Float = 0.5f,
    val thicknessDp: Int = 4,
    val lengthRatio: Float = 1.0f,
    val angleDegrees: Float = 0f,
    val colorHex: String = "#000000",
    val opacity: Float = 1.0f,
    val isEnabled: Boolean = true,
    val touchPassThrough: Boolean = true,
    val hardwareLockOrientation: Boolean = true,
    val orientationCondition: String = "ALL", // "ALL", "PORTRAIT_ONLY", "LANDSCAPE_ONLY"
    val createdAt: Long = System.currentTimeMillis()
)
