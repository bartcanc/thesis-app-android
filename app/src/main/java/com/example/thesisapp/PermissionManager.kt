package com.example.thesisapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open class PermissionManager(private val activity: Activity) {
    protected val permissionsQueue = mutableListOf<String>()
    private var permissionsRequested = false
    private var currentPermissionRequestCode = 0

    companion object {
        const val REQUEST_CODE_BASE = 100
    }

    open fun requestAllPermissions() {
        if (!permissionsRequested) {
            permissionsRequested = true

            checkBluetoothPermissions()
            checkWifiPermissions()
            checkLocationPermissions()
            checkAndRequestMtuPermissions()

            if (permissionsQueue.isNotEmpty()) {
                showNextPermissionDialog()
            } else {
                Log.d("PermissionManager", "All permissions already granted.")
            }
        }
    }

    private fun checkBluetoothPermissions() {
        addPermissionToQueue(
            Manifest.permission.BLUETOOTH_SCAN
        )
    }

    private fun checkWifiPermissions() {
        addPermissionToQueue(
            Manifest.permission.ACCESS_WIFI_STATE
        )
    }

    private fun checkLocationPermissions() {
        addPermissionToQueue(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun checkAndRequestMtuPermissions() {
        addPermissionToQueue(
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }

    private fun addPermissionToQueue(permission: String) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsQueue.add(permission)
            Log.d("PermissionManager", "Added permission to queue: $permission")
        }
    }

    private fun showNextPermissionDialog() {
        if (permissionsQueue.isNotEmpty()) {
            val nextPermission = permissionsQueue.removeAt(0)
            Log.d("PermissionManager", "Requesting permission: $nextPermission")
            ActivityCompat.requestPermissions(activity, arrayOf(nextPermission), REQUEST_CODE_BASE + currentPermissionRequestCode++)
        }
    }

    fun handlePermissionsResult(grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("PermissionManager", "Permission granted")
            if (permissionsQueue.isNotEmpty()) {
                showNextPermissionDialog()
            }
        } else {
            AlertDialog.Builder(activity)
                .setTitle("Wymagane uprawnienie")
                .setMessage("To uprawnienie jest wymagane do poprawnego działania aplikacji. Czy chcesz spróbować ponownie?")
                .setPositiveButton("Spróbuj ponownie") { _, _ ->
                    showNextPermissionDialog()
                }
                .setNegativeButton("Anuluj") { _, _ ->
                    if (permissionsQueue.isNotEmpty()) {
                        showNextPermissionDialog()
                    }
                }
                .show()
        }
    }

    open fun allPermissionsGranted(): Boolean {
        val permissions = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
