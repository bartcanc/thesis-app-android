package com.example.thesisapp

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log

class MockPermissionManager(activity: Activity) : PermissionManager(activity) {

    override fun requestAllPermissions() {
        Log.d("MockPermissionManager", "Symulacja przyznania uprawnień.")
        handlePermissionsResult(IntArray(permissionsQueue.size) { PackageManager.PERMISSION_GRANTED })
    }


    override fun allPermissionsGranted(): Boolean {
        return true
    }

    fun simulatePermissionResult(granted: Boolean) {
        if (granted) {
            Log.d("MockPermissionManager", "Simulated: Permissions granted.")
        } else {
            Log.d("MockPermissionManager", "Simulated: Permissions denied.")
        }
    }


}
