package com.example.test_bluetooth_devices
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.test_bluetooth_devices/bluetooth"
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var leScanner: BluetoothLeScanner
    private val PERMISSION_REQUEST_CODE = 1

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        leScanner = bluetoothAdapter.bluetoothLeScanner

        MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger ?: return, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "getBluetoothDevices") {
                if (checkPermissions()) {
                    val devices = getBluetoothDevices()
                    result.success(devices)
                } else {
                    requestPermissions()
                    result.error("PERMISSION_DENIED", "Bluetooth permissions are required", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val bluetoothScanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        val bluetoothConnectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return bluetoothScanPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED &&
                locationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, you can start scanning now
            } else {
                // Handle the case where permissions were not granted
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getBluetoothDevices(): List<String> {
        val devices = mutableListOf<String>()

        // Get bonded devices (Classic Bluetooth)
        val bondedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        bondedDevices?.forEach { device ->
            devices.add("Classic: ${device.name} - ${device.address}")
        }

        // Scan for BLE devices
        val leScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { device ->
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        devices.add("BLE: ${device.name} - ${device.address}")

                        return
                    }
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                results.forEach { result ->
                    result.device?.let { device ->
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            devices.add("BLE: ${device.name} - ${device.address}")

                            return
                        }
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                // Handle scan failure
            }
        }

        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                leScanner.startScan(leScanCallback)
                // Wait some time to collect devices (consider running in a coroutine or a different thread)
                Thread.sleep(5000)
                leScanner.stopScan(leScanCallback)
            }

        }

        return devices
    }
}
