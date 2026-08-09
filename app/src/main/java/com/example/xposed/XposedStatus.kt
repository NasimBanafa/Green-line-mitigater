package com.example.xposed

import android.util.Log

object XposedStatus {
    private const val TAG = "XposedStatus"

    /**
     * Returns true if the Xposed/LSPosed module is loaded and active.
     * When Xposed hooks this package, it overrides this method to return true.
     */
    @JvmStatic
    fun isModuleActive(): Boolean {
        return false
    }

    /**
     * Check if system hook features (like FLAG_SECURE bypass & anti-kill) are active.
     */
    @JvmStatic
    fun isAntiKillActive(): Boolean {
        return isModuleActive()
    }
}
