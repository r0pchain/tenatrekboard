package com.example.tenatrekboard

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var bluetoothSocket: BluetoothSocket? = null
    private var deviceList = mutableListOf<BluetoothDevice>()

    // UI references
    private lateinit var deviceSpinner: Spinner
    private lateinit var connectButton: Button
    private lateinit var buttonSaucer: Button
    private lateinit var buttonSec: Button
    private lateinit var buttonNeck: Button
    private lateinit var buttonChiller: Button
    private lateinit var buttonNav: Button
    private lateinit var buttonStrobe: Button
    private lateinit var buttonImp: Button
    private lateinit var buttonDflct: Button
    private lateinit var buttonWarp: Button


    private lateinit var statusTextView: TextView

    // Buttons for audio clips
    private lateinit var buttonPlay1: Button
    private lateinit var buttonPlay2: Button
    private lateinit var buttonPlay3: Button
    private lateinit var buttonPlay4: Button
    private lateinit var buttonPlay5: Button
    private lateinit var buttonPlay6: Button
    private lateinit var buttonPlay7: Button
    private lateinit var buttonPlay8: Button
    private lateinit var buttonPlay9: Button
    private lateinit var buttonPlay10: Button

    //Buttons for weapons
    private lateinit var buttonPhoton: Button
    private lateinit var buttonPhaser: Button


    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // Required permissions for Android 12+ or older
    private val requiredPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(android.Manifest.permission.BLUETOOTH_SCAN)
            add(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(android.Manifest.permission.BLUETOOTH)
            add(android.Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    // Launcher for runtime permissions
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val allGranted = perms.all { it.value == true }
            if (!allGranted) {
                updateStatus("Some permissions denied. Cannot fully use Bluetooth.")
            } else {
                loadPairedDevices() // Reload devices after permissions are granted
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Link layout IDs
        deviceSpinner = findViewById(R.id.deviceSpinner)
        connectButton = findViewById(R.id.connectButton)
        buttonSaucer = findViewById(R.id.buttonSaucer)
        buttonSec = findViewById(R.id.buttonSec)
        buttonNeck = findViewById(R.id.buttonNeck)
        buttonChiller = findViewById(R.id.buttonChiller)
        buttonNav = findViewById(R.id.buttonNav)
        buttonStrobe = findViewById(R.id.buttonStrobe)
        buttonImp = findViewById(R.id.buttonImp)
        buttonDflct = findViewById(R.id.buttonDflct)
        buttonWarp = findViewById(R.id.buttonWarp)
        statusTextView = findViewById(R.id.statusTextView)

        // Initialize audio clip buttons
        buttonPlay1 = findViewById(R.id.buttonPlay1)
        buttonPlay2 = findViewById(R.id.buttonPlay2)
        buttonPlay3 = findViewById(R.id.buttonPlay3)
        buttonPlay4 = findViewById(R.id.buttonPlay4)
        buttonPlay5 = findViewById(R.id.buttonPlay5)
        buttonPlay6 = findViewById(R.id.buttonPlay6)
        buttonPlay7 = findViewById(R.id.buttonPlay7)
        buttonPlay8 = findViewById(R.id.buttonPlay8)
        buttonPlay9 = findViewById(R.id.buttonPlay9)
        buttonPlay10 = findViewById(R.id.buttonPlay10)

        // Initialize weapon buttons
        buttonPhaser = findViewById(R.id.buttonPhaser)
        buttonPhoton = findViewById(R.id.buttonPhoton)

        // Request permissions
        checkAndRequestPermissions()

        // Click listeners
        connectButton.setOnClickListener { connectToSelectedDevice() }
        buttonSaucer.setOnClickListener { sendCommand("*SAUCER#") }
        buttonSec.setOnClickListener { sendCommand("*SEC#") }
        buttonNeck.setOnClickListener { sendCommand("*NECK#") }
        buttonChiller.setOnClickListener { sendCommand("*CHILLER#") }
        buttonNav.setOnClickListener { sendCommand("*NAV#") }
        buttonStrobe.setOnClickListener { sendCommand("*STROBE*") }
        buttonImp.setOnClickListener { sendCommand("*IMP#") }
        buttonDflct.setOnClickListener { sendCommand("*DFLCT#") }
        buttonWarp.setOnClickListener { sendCommand("*WARP#") }



        // Click listeners for audio clips - you may or may not have these depending on your board
        buttonPlay1.setOnClickListener { sendCommand("*PLAY1$") }
        buttonPlay2.setOnClickListener { sendCommand("*PLAY2$") }
        buttonPlay3.setOnClickListener { sendCommand("*PLAY3$") }
        buttonPlay4.setOnClickListener { sendCommand("*PLAY4$") }
        buttonPlay5.setOnClickListener { sendCommand("*PLAY5$") }
        buttonPlay6.setOnClickListener { sendCommand("*PLAY6$") }
        buttonPlay7.setOnClickListener { sendCommand("*PLAY7$") }
        buttonPlay8.setOnClickListener { sendCommand("*PLAY8$") }
        buttonPlay9.setOnClickListener { sendCommand("*PLAY9$") }
        buttonPlay10.setOnClickListener { sendCommand("*PLAY10$") }

        // CLick listeners for weapons
        buttonPhoton.setOnClickListener { sendCommand("*PHOTON#") }
        buttonPhaser.setOnClickListener { sendCommand("*PHASER#") }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeConnection()
        job.cancel()
    }

    private fun checkAndRequestPermissions() {
        val needed = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            requestPermissionLauncher.launch(needed.toTypedArray())
        } else {
            loadPairedDevices() // Load devices immediately if permissions are granted
        }
    }

    private fun loadPairedDevices() {
        if (bluetoothAdapter == null) {
            updateStatus("Bluetooth not supported")
            return
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
        deviceList.clear()
        var bt12Device: BluetoothDevice? = null

        for (device in pairedDevices) {
            if (device.name == "BT12") {
                bt12Device = device
            } else {
                deviceList.add(device)
            }
        }

        // Ensure BT12 is prioritized at the top if found
        if (bt12Device != null) {
            deviceList.add(0, bt12Device)
        }

        // Convert to a displayable format for the spinner
        val deviceNames = deviceList.map { "${it.name} (${it.address})" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, deviceNames)
        deviceSpinner.adapter = adapter
    }

    private fun connectToSelectedDevice() {
        val selectedIndex = deviceSpinner.selectedItemPosition
        if (selectedIndex < 0 || selectedIndex >= deviceList.size) {
            updateStatus("No device selected")
            return
        }

        val device = deviceList[selectedIndex]
        updateStatus("Connecting to ${device.name}...")

        scope.launch {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()

                withContext(Dispatchers.Main) {
                    updateStatus("Connected to ${device.name}")
                }
            } catch (e: IOException) {
                Log.e("BT_CONNECT", "Connection failed", e)
                withContext(Dispatchers.Main) {
                    updateStatus("Connection failed: ${e.message}")
                    closeConnection()
                }
            }
        }
    }

    private fun closeConnection() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("BT_CLOSE", "Failed to close socket", e)
        } finally {
            bluetoothSocket = null
        }
    }

    private fun sendCommand(command: String) {
        val socket = bluetoothSocket
        if (socket == null || !socket.isConnected) {
            updateStatus("Error: Not connected.")
            return
        }
        scope.launch {
            try {
                socket.outputStream.write(command.toByteArray())
                socket.outputStream.flush()
                withContext(Dispatchers.Main) {
                    updateStatus("Sent: $command")
                }
            } catch (e: IOException) {
                Log.e("BT_SEND", "Failed to send command", e)
                withContext(Dispatchers.Main) {
                    updateStatus("Failed to send command: ${e.message}")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateStatus(msg: String) {
        runOnUiThread {
            statusTextView.text = "Status: $msg"
        }
    }
}
