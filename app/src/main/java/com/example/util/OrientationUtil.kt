package com.example.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.Surface
import android.view.WindowManager

data class PointF(val x: Float, val y: Float)

object OrientationUtil {

    fun getDisplayRotation(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                @Suppress("DEPRECATION")
                wm.defaultDisplay?.rotation ?: Surface.ROTATION_0
            } else {
                @Suppress("DEPRECATION")
                wm.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }
        } catch (e: Exception) {
            Surface.ROTATION_0
        }
    }

    fun getScreenDimensions(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            point.x = bounds.width()
            point.y = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealSize(point)
        }
        return point
    }

    /**
     * Converts a physical hardware display coordinate (relative to portrait ROTATION_0)
     * into current software window coordinates based on active display rotation angle.
     * This ensures physical green screen line defect remains perfectly masked.
     */
    fun mapPhysicalToSoftware(
        xPhysRatio: Float,
        yPhysRatio: Float,
        rotation: Int,
        screenWidth: Int,
        screenHeight: Int
    ): PointF {
        return when (rotation) {
            Surface.ROTATION_90 -> {
                // Rotated 90 degrees counter-clockwise (natural landscape left)
                // Physical Y maps to Software X; Physical X (measured from left) maps to distance from software bottom
                PointF(
                    x = yPhysRatio * screenWidth,
                    y = (1f - xPhysRatio) * screenHeight
                )
            }
            Surface.ROTATION_180 -> {
                // Reverse portrait (upside down)
                PointF(
                    x = (1f - xPhysRatio) * screenWidth,
                    y = (1f - yPhysRatio) * screenHeight
                )
            }
            Surface.ROTATION_270 -> {
                // Rotated 270 degrees counter-clockwise / 90 degrees clockwise (reverse landscape right)
                // Physical Y maps to inverted Software X; Physical X maps to Software Y from top
                PointF(
                    x = (1f - yPhysRatio) * screenWidth,
                    y = xPhysRatio * screenHeight
                )
            }
            else -> {
                // ROTATION_0 (Standard portrait)
                PointF(
                    x = xPhysRatio * screenWidth,
                    y = yPhysRatio * screenHeight
                )
            }
        }
    }
}
