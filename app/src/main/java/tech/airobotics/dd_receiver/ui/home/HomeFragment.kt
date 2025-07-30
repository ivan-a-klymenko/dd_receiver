package tech.airobotics.dd_receiver.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.airobotics.dd_receiver.databinding.FragmentHomeBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var serverThread: Thread? = null
    private val bluetoothServerPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startBluetoothServer()
            } else {
                Toast.makeText(requireContext(), "Bluetooth permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    private var running = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val permission = Manifest.permission.BLUETOOTH_CONNECT
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothServerPermissionLauncher.launch(permission)
        } else {
            startBluetoothServer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopBluetoothServer()
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothServer() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        serverThread = Thread {
            var serverSocket: BluetoothServerSocket? = null
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BT_APP", uuid)
                Log.d("BTServer", "Waiting for connection...")

                while (running) {
                    val socket = serverSocket.accept()
                    Log.d("BTServer", "Client connected: ${socket.remoteDevice.name}")
                    handleClient(socket)
                }
            } catch (e: IOException) {
                Log.e("BTServer", "Server error: ${e.message}")
            } finally {
                try {
                    serverSocket?.close()
                } catch (_: IOException) {
                }
            }
        }
        serverThread?.start()
    }

    private fun stopBluetoothServer() {
        running = false
        serverThread?.interrupt()
    }

    private fun handleClient(socket: BluetoothSocket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                var line: String?
                while (running && socket.isConnected) {
                    line = reader.readLine()
                    if (line == null) break
                    Log.d("BTServer", "Received: $line")
                    onCommandReceived(line)
                }
            } catch (e: IOException) {
                Log.e("BTServer", "Read error: ${e.message}")
            } finally {
                try {
                    socket.close()
                } catch (_: IOException) {
                }
            }
        }
    }

    private fun onCommandReceived(command: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(
                requireContext(),
                "BTServer received command: $command",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}