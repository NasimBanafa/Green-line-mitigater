package com.example.service

import android.app.PendingIntent
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.R

class OverlayTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val context = applicationContext
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        if (!hasPermission) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val serviceIntent = Intent(context, OverlayService::class.java)
        if (OverlayService.isRunning) {
            serviceIntent.action = OverlayService.ACTION_STOP
            context.startService(serviceIntent)
        } else {
            serviceIntent.action = OverlayService.ACTION_START
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = OverlayService.isRunning

        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Line Mask"
        tile.subtitle = if (isRunning) "Overlay ON" else "Overlay OFF"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.contentDescription = if (isRunning) "Line Mask Overlay is ON" else "Line Mask Overlay is OFF"
        }
        tile.updateTile()
    }

    companion object {
        fun requestAddTile(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    val statusBarManager = context.getSystemService(Context.STATUS_BAR_SERVICE) as? StatusBarManager
                    val executor = ContextCompat.getMainExecutor(context)
                    statusBarManager?.requestAddTileService(
                        ComponentName(context, OverlayTileService::class.java),
                        "Line Mask",
                        Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
                        executor
                    ) { _ -> }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}
