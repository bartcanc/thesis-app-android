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
        // Zawsze zwracaj true, że wszystkie uprawnienia są przyznane
        return true
    }

    fun simulatePermissionResult(granted: Boolean) {
        // Opcjonalnie możesz dodać symulację wyniku żądania uprawnień
        if (granted) {
            Log.d("MockPermissionManager", "Simulated: Permissions granted.")
        } else {
            Log.d("MockPermissionManager", "Simulated: Permissions denied.")
        }
    }


}
