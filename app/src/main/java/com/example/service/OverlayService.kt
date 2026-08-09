package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.MaskBarEntity
import com.example.util.OrientationUtil
import com.example.util.PointF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverlayService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var windowManager: WindowManager
    private val overlayViews = mutableMapOf<Long, View>()
    private var orientationLockView: View? = null

    private var isOrientationLocked = false
    private var currentLockedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private var displayListener: DisplayManager.DisplayListener? = null

    companion object {
        const val CHANNEL_ID = "line_mask_overlay_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.ACTION_START_OVERLAY"
        const val ACTION_STOP = "com.example.ACTION_STOP_OVERLAY"
        const val ACTION_TOGGLE_ORIENTATION_LOCK = "com.example.ACTION_TOGGLE_ORIENTATION_LOCK"

        var isRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()

        setupDisplayListener()
        observeDatabaseMasks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        } else if (action == ACTION_TOGGLE_ORIENTATION_LOCK) {
            isOrientationLocked = !isOrientationLocked
            if (isOrientationLocked) {
                val rot = OrientationUtil.getDisplayRotation(this)
                currentLockedOrientation = when (rot) {
                    android.view.Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    android.view.Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    android.view.Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                applyOrientationLock(true, currentLockedOrientation)
            } else {
                applyOrientationLock(false)
            }
        }

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun observeDatabaseMasks() {
        serviceScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(applicationContext).maskBarDao()
            dao.getEnabledMaskBars().collectLatest { masks ->
                withContext(Dispatchers.Main) {
                    updateOverlayViews(masks)
                }
            }
        }
    }

    private fun updateOverlayViews(masks: List<MaskBarEntity>) {
        val currentIds = masks.map { it.id }.toSet()

        val iterator = overlayViews.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!currentIds.contains(entry.key)) {
                try {
                    windowManager.removeView(entry.value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                iterator.remove()
            }
        }

        masks.forEach { mask ->
            val existingView = overlayViews[mask.id]
            if (existingView != null) {
                updateSingleOverlayView(existingView, mask)
            } else {
                createOverlayViewForMask(mask)?.let { newView ->
                    overlayViews[mask.id] = newView
                }
            }
        }
    }

    private fun createOverlayViewForMask(mask: MaskBarEntity): View? {
        if (!Settings.canDrawOverlays(this)) {
            Log.e("OverlayService", "Overlay permission not granted. Cannot add window.")
            return null
        }
        val view = View(this)
        applyMaskToView(view, mask)

        val params = createLayoutParamsForMask(mask)
        return try {
            windowManager.addView(view, params)
            view
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to add overlay view for mask ${mask.id}", e)
            null
        }
    }

    private fun updateSingleOverlayView(view: View, mask: MaskBarEntity) {
        if (!Settings.canDrawOverlays(this)) return
        applyMaskToView(view, mask)
        val params = createLayoutParamsForMask(mask)
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to update overlay view layout for mask ${mask.id}", e)
        }
    }

    private fun applyMaskToView(view: View, mask: MaskBarEntity) {
        val colorInt = try {
            val parsed = Color.parseColor(mask.colorHex)
            val alpha = (mask.opacity * 255).toInt().coerceIn(0, 255)
            Color.argb(alpha, Color.red(parsed), Color.green(parsed), Color.blue(parsed))
        } catch (e: Exception) {
            Color.BLACK
        }
        view.setBackgroundColor(colorInt)
        view.rotation = mask.angleDegrees
    }

    private fun createLayoutParamsForMask(mask: MaskBarEntity): WindowManager.LayoutParams {
        val rotation = OrientationUtil.getDisplayRotation(this)
        val screen = OrientationUtil.getScreenDimensions(this)

        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

        if (mask.touchPassThrough) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        val density = resources.displayMetrics.density
        val thicknessPx = (mask.thicknessDp * density).toInt().coerceAtLeast(1)

        val barWidthPx: Int
        val barHeightPx: Int

        if (mask.isVertical) {
            barWidthPx = thicknessPx
            barHeightPx = (screen.y * mask.lengthRatio).toInt().coerceAtLeast(1)
        } else {
            barWidthPx = (screen.x * mask.lengthRatio).toInt().coerceAtLeast(1)
            barHeightPx = thicknessPx
        }

        val mappedPos = if (mask.hardwareLockOrientation) {
            OrientationUtil.mapPhysicalToSoftware(
                mask.xPosRatio,
                mask.yPosRatio,
                rotation,
                screen.x,
                screen.y
            )
        } else {
            PointF(
                x = mask.xPosRatio * screen.x,
                y = mask.yPosRatio * screen.y
            )
        }

        val params = WindowManager.LayoutParams(
            barWidthPx,
            barHeightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = (mappedPos.x - barWidthPx / 2f).toInt()
        params.y = (mappedPos.y - barHeightPx / 2f).toInt()

        return params
    }

    private fun setupDisplayListener() {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {}
            override fun onDisplayRemoved(displayId: Int) {}
            override fun onDisplayChanged(displayId: Int) {
                if (displayId == Display.DEFAULT_DISPLAY) {
                    refreshAllOverlays()
                }
            }
        }
        displayManager.registerDisplayListener(displayListener, null)
    }

    private fun refreshAllOverlays() {
        serviceScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(applicationContext).maskBarDao()
            val masks = dao.getEnabledMaskBarsList()
            withContext(Dispatchers.Main) {
                masks.forEach { mask ->
                    overlayViews[mask.id]?.let { view ->
                        updateSingleOverlayView(view, mask)
                    }
                }
            }
        }
    }

    private fun applyOrientationLock(enable: Boolean, orientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        if (enable) {
            if (!Settings.canDrawOverlays(this)) {
                Log.e("OverlayService", "Overlay permission missing, skipping orientation lock view.")
                return
            }
            if (orientationLockView == null) {
                val dummy = View(this)
                val params = WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT
                )
                params.screenOrientation = orientation
                try {
                    windowManager.addView(dummy, params)
                    orientationLockView = dummy
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val params = orientationLockView?.layoutParams as? WindowManager.LayoutParams
                if (params != null) {
                    params.screenOrientation = orientation
                    try {
                        windowManager.updateViewLayout(orientationLockView, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            orientationLockView?.let { view ->
                try {
                    windowManager.removeView(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            orientationLockView = null
        }
        val notification = buildNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Line Mask Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keep black bar masks active over screen lines"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleLockIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_TOGGLE_ORIENTATION_LOCK
        }
        val pendingToggleLock = PendingIntent.getService(
            this, 1, toggleLockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val lockStateText = if (isOrientationLocked) "Orientation Locked" else "Orientation Free"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Line Mask Overlay Active")
            .setContentText("Masks active. Status: $lockStateText")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingOpen)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, if (isOrientationLocked) "Unlock Orientation" else "Lock Orientation", pendingToggleLock)
            .addAction(0, "Stop Service", pendingStop)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false

        displayListener?.let {
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            displayManager.unregisterDisplayListener(it)
        }

        overlayViews.values.forEach { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayViews.clear()

        applyOrientationLock(false)

        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
