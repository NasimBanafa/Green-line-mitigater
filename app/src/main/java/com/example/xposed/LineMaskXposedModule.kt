package com.example.xposed

import android.app.Activity
import android.content.Context
import android.view.Window
import android.view.WindowManager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class LineMaskXposedModule : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        private const val TAG = "LineMaskXposedModule"
        private const val TARGET_PACKAGE_NAME = "com.aistudio.linemask.xqvz"
        private const val TARGET_ALT_PACKAGE = "com.example"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
        XposedBridge.log("$TAG: Zygote initialized")
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // 1. Hook into our own app to report Xposed status as ACTIVE
        if (lpparam.packageName == TARGET_PACKAGE_NAME || lpparam.packageName == TARGET_ALT_PACKAGE) {
            XposedBridge.log("$TAG: Hooking into self package ${lpparam.packageName}")
            try {
                XposedHelpers.findAndHookMethod(
                    "com.example.xposed.XposedStatus",
                    lpparam.classLoader,
                    "isModuleActive",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            param.result = true
                        }
                    }
                )
            } catch (t: Throwable) {
                XposedBridge.log("$TAG: Failed to hook XposedStatus: ${t.message}")
            }
        }

        // 2. Hook System Framework ("android") to prevent app process termination & memory trim
        if (lpparam.packageName == "android") {
            hookSystemServerProcessProtection(lpparam)
            hookWindowManagerFlagSecure(lpparam)
        }

        // 3. Hook System UI to ensure overlay windows stay on top without restriction
        if (lpparam.packageName == "com.android.systemui") {
            hookSystemUIOverlays(lpparam)
        }
    }

    private fun hookSystemServerProcessProtection(lpparam: LoadPackageParam) {
        try {
            // Hook ActivityManagerService to prevent force-stopping or killing the overlay service
            val amsClass = XposedHelpers.findClassIfExists(
                "com.android.server.am.ActivityManagerService",
                lpparam.classLoader
            )
            if (amsClass != null) {
                XposedBridge.log("$TAG: Hooking ActivityManagerService for anti-kill protection")
                XposedHelpers.findAndHookMethod(
                    amsClass,
                    "killAppAtUsersRequest",
                    String::class.java,
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val pkg = param.args[0] as? String
                            if (pkg == TARGET_PACKAGE_NAME || pkg?.startsWith(TARGET_ALT_PACKAGE) == true) {
                                XposedBridge.log("$TAG: Blocked user kill request for $pkg")
                                param.result = null
                            }
                        }
                    }
                )
            }
        } catch (t: Throwable) {
            XposedBridge.log("$TAG: Anti-kill hook info: ${t.message}")
        }
    }

    private fun hookWindowManagerFlagSecure(lpparam: LoadPackageParam) {
        try {
            // Disable FLAG_SECURE on windows so overlay black mask remains visible on top of all screens
            val windowClass = XposedHelpers.findClassIfExists("android.view.Window", lpparam.classLoader)
            if (windowClass != null) {
                XposedHelpers.findAndHookMethod(
                    windowClass,
                    "setFlags",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            var flags = param.args[0] as Int
                            val mask = param.args[1] as Int
                            if ((mask and WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                                flags = flags and WindowManager.LayoutParams.FLAG_SECURE.inv()
                                param.args[0] = flags
                            }
                        }
                    }
                )
            }
        } catch (t: Throwable) {
            XposedBridge.log("$TAG: FLAG_SECURE hook info: ${t.message}")
        }
    }

    private fun hookSystemUIOverlays(lpparam: LoadPackageParam) {
        XposedBridge.log("$TAG: SystemUI hooked for unrestricted system overlays")
    }
}
