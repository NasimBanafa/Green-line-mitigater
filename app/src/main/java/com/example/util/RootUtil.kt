package com.example.util

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader

object RootUtil {

    fun isRootAvailable(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    suspend fun executeRootCommand(command: String): RootResult {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val output = reader.readText()
            val error = errorReader.readText()
            val exitCode = process.waitFor()

            RootResult(
                isSuccess = exitCode == 0,
                output = output.ifEmpty { error },
                exitCode = exitCode
            )
        } catch (e: Exception) {
            RootResult(
                isSuccess = false,
                output = e.localizedMessage ?: "Root execution failed",
                exitCode = -1
            )
        }
    }

    suspend fun grantOverlayPermissionViaRoot(packageName: String): RootResult {
        return executeRootCommand("pm grant $packageName android.permission.SYSTEM_ALERT_WINDOW")
    }

    suspend fun setSystemOrientationLockViaRoot(lock: Boolean, orientationValue: Int = 0): RootResult {
        return if (lock) {
            executeRootCommand("settings put system accelerometer_rotation 0 && settings put system user_rotation $orientationValue")
        } else {
            executeRootCommand("settings put system accelerometer_rotation 1")
        }
    }
}

data class RootResult(
    val isSuccess: Boolean,
    val output: String,
    val exitCode: Int
)
