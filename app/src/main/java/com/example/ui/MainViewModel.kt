package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MaskBarEntity
import com.example.data.MaskBarRepository
import com.example.service.OverlayService
import com.example.util.RootUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MaskBarRepository

    val allMasks: StateFlow<List<MaskBarEntity>>

    private val _isOverlayPermissionGranted = MutableStateFlow(false)
    val isOverlayPermissionGranted: StateFlow<Boolean> = _isOverlayPermissionGranted.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _isRootAvailable = MutableStateFlow(false)
    val isRootAvailable: StateFlow<Boolean> = _isRootAvailable.asStateFlow()

    private val _editingMask = MutableStateFlow<MaskBarEntity?>(null)
    val editingMask: StateFlow<MaskBarEntity?> = _editingMask.asStateFlow()

    private val _rootMessage = MutableStateFlow<String?>(null)
    val rootMessage: StateFlow<String?> = _rootMessage.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).maskBarDao()
        repository = MaskBarRepository(dao)

        allMasks = repository.allMasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        checkPermissionsAndStatus()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val initialList = repository.allMasks.first()
                if (initialList.isEmpty()) {
                    repository.insert(
                        MaskBarEntity(
                            name = "Primary Green Line Mask",
                            isVertical = true,
                            xPosRatio = 0.72f,
                            yPosRatio = 0.50f,
                            thicknessDp = 5,
                            lengthRatio = 1.0f,
                            colorHex = "#000000",
                            isEnabled = true,
                            hardwareLockOrientation = true,
                            touchPassThrough = true
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkPermissionsAndStatus() {
        val context = getApplication<Application>()
        _isOverlayPermissionGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
        _isServiceRunning.value = OverlayService.isRunning
        _isRootAvailable.value = RootUtil.isRootAvailable()
    }

    fun toggleService(context: Context) {
        val intent = Intent(context, OverlayService::class.java)
        if (OverlayService.isRunning) {
            intent.action = OverlayService.ACTION_STOP
            context.startService(intent)
            _isServiceRunning.value = false
        } else {
            intent.action = OverlayService.ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _isServiceRunning.value = true
        }
    }

    fun toggleOrientationLock(context: Context) {
        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_TOGGLE_ORIENTATION_LOCK
        }
        context.startService(intent)
    }

    fun requestAddQuickTile(context: Context) {
        com.example.service.OverlayTileService.requestAddTile(context)
    }

    fun setEditingMask(mask: MaskBarEntity?) {
        _editingMask.value = mask
    }

    fun createNewMask() {
        _editingMask.value = MaskBarEntity(
            name = "Line Mask #${(allMasks.value.size + 1)}",
            isVertical = true,
            xPosRatio = 0.5f,
            yPosRatio = 0.5f,
            thicknessDp = 6,
            lengthRatio = 1.0f,
            colorHex = "#000000",
            isEnabled = true,
            hardwareLockOrientation = true,
            touchPassThrough = true
        )
    }

    fun saveMask(mask: MaskBarEntity) {
        viewModelScope.launch {
            if (mask.id == 0L) {
                repository.insert(mask)
            } else {
                repository.update(mask)
            }
            _editingMask.value = null
        }
    }

    fun deleteMask(mask: MaskBarEntity) {
        viewModelScope.launch {
            repository.delete(mask)
            if (_editingMask.value?.id == mask.id) {
                _editingMask.value = null
            }
        }
    }

    fun toggleMaskEnabled(mask: MaskBarEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setEnabled(mask.id, isEnabled)
        }
    }

    fun addPreset(presetName: String) {
        viewModelScope.launch {
            val newMask = when (presetName) {
                "Vertical Right Green Line" -> MaskBarEntity(
                    name = "Vertical Right Green Line",
                    isVertical = true,
                    xPosRatio = 0.85f,
                    yPosRatio = 0.5f,
                    thicknessDp = 4,
                    lengthRatio = 1.0f
                )
                "Vertical Center Line" -> MaskBarEntity(
                    name = "Vertical Center Green Line",
                    isVertical = true,
                    xPosRatio = 0.50f,
                    yPosRatio = 0.5f,
                    thicknessDp = 4,
                    lengthRatio = 1.0f
                )
                "Horizontal Top Line" -> MaskBarEntity(
                    name = "Horizontal Top Defect",
                    isVertical = false,
                    xPosRatio = 0.5f,
                    yPosRatio = 0.15f,
                    thicknessDp = 6,
                    lengthRatio = 1.0f
                )
                "Camera Punchhole Mask" -> MaskBarEntity(
                    name = "Camera Notch Mask",
                    isVertical = false,
                    xPosRatio = 0.5f,
                    yPosRatio = 0.03f,
                    thicknessDp = 24,
                    lengthRatio = 0.35f
                )
                else -> MaskBarEntity(
                    name = "Custom Green Line Mask",
                    isVertical = true,
                    xPosRatio = 0.3f,
                    yPosRatio = 0.5f,
                    thicknessDp = 4
                )
            }
            repository.insert(newMask)
        }
    }

    fun grantOverlayViaRoot() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val res = RootUtil.grantOverlayPermissionViaRoot(context.packageName)
            _rootMessage.value = if (res.isSuccess) {
                "Root command executed! SYSTEM_ALERT_WINDOW permission granted."
            } else {
                "Root command output: ${res.output}"
            }
            checkPermissionsAndStatus()
        }
    }

    fun clearRootMessage() {
        _rootMessage.value = null
    }

    fun requestOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }
}
