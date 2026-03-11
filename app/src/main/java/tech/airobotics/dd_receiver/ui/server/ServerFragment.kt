package tech.airobotics.dd_receiver.ui.server

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import tech.airobotics.dd_receiver.databinding.FragmentServerBinding
import tech.airobotics.dd_receiver.model.HttpMessage
import tech.airobotics.dd_receiver.mvi.ServerIntent
import tech.airobotics.dd_receiver.mvi.ServerViewModel
import tech.airobotics.dd_receiver.server.ACTION_START
import tech.airobotics.dd_receiver.server.ACTION_STOP
import tech.airobotics.dd_receiver.server.LocalHttpService

class ServerFragment : Fragment() {

    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ServerViewModel by viewModels()
    private val adapter = MessageAdapter()
    private val TAG = "ServerFragment"

    private val requestNotifLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startServiceSafely()
            } else {
                try {
                    Toast.makeText(
                        requireContext(),
                        "Notification permission required to run server reliably",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (_: Exception) {
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = adapter

        binding.btnStart.setOnClickListener {
            // Check notification permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val has = ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                if (has == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    startServiceSafely()
                } else {
                    requestNotifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                startServiceSafely()
            }
        }

        binding.btnStop.setOnClickListener {
            val intent = Intent(requireContext(), LocalHttpService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                requireContext().stopService(intent)
                viewModel.dispatch(ServerIntent.StopServer)
            } catch (e: Exception) {
                Log.e(TAG, "stopService failed", e)
                try {
                    Toast.makeText(
                        requireContext(),
                        "Failed to stop service: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (_: Exception) {
                }
            }
        }

        binding.btnClearPayload.setOnClickListener {
            try {
                binding.tvPayload.text = "Payload: "
                viewModel.dispatch(ServerIntent.ClearMessages)
            } catch (e: Exception) {
                Log.e(TAG, "clear payload failed", e)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvStatus.text =
                    if (state.isRunning) "Status: running" else "Status: stopped"
                adapter.submitList(state.messages)
                // показать payload последнего сообщения (если есть)
                val last = state.messages.firstOrNull()
                binding.tvPayload.text = last?.payload?.toString() ?: "Payload: "
            }
        }
    }

    private fun startServiceSafely() {
        val intent =
            Intent(requireContext(), LocalHttpService::class.java).apply { action = ACTION_START }
        try {
            ContextCompat.startForegroundService(requireContext(), intent)
            viewModel.dispatch(ServerIntent.StartServer)
        } catch (e: Exception) {
            Log.e(TAG, "startForegroundService failed", e)
            try {
                Toast.makeText(
                    requireContext(),
                    "Failed to start service: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (_: Exception) {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class MessageAdapter :
        androidx.recyclerview.widget.ListAdapter<HttpMessage, MessageViewHolder>(
            object : androidx.recyclerview.widget.DiffUtil.ItemCallback<HttpMessage>() {
                override fun areItemsTheSame(oldItem: HttpMessage, newItem: HttpMessage): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: HttpMessage,
                    newItem: HttpMessage
                ): Boolean = oldItem == newItem
            }
        ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
        private val t2: android.widget.TextView = view.findViewById(android.R.id.text2)

        fun bind(msg: HttpMessage) {
            t1.text = msg.id
            t2.text = "${msg.timestamp} from ${msg.sourceIp ?: "?"}"
        }
    }
}
