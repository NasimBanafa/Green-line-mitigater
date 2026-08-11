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
     * Maps coordinate ratios directly to screen dimensions without rotational transformation.
     */
    fun mapPhysicalToSoftware(
        xPhysRatio: Float,
        yPhysRatio: Float,
        rotation: Int,
        screenWidth: Int,
        screenHeight: Int
    ): PointF {
        return PointF(
            x = xPhysRatio * screenWidth,
            y = yPhysRatio * screenHeight
        )
    }
}
