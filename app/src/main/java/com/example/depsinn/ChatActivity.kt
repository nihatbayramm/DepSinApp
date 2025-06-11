package com.example.depsinn

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var wifiCommunicationService: WifiCommunicationService
    private val handler = Handler(Looper.getMainLooper())
    private val messages = mutableListOf<Message>()
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initializeViews()
        setupWifiP2p()
        setupRecyclerView()
        setupMessageHandlers()
        loadMessages()
    }

    private fun initializeViews() {
        messageRecyclerView = findViewById(R.id.messageRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        sendButton.isEnabled = false
        messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendButton.isEnabled = !s.isNullOrEmpty()
            }
        })

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            }
        }
    }

    private fun setupWifiP2p() {
        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(this, mainLooper, null)
        wifiCommunicationService = WifiCommunicationService.getInstance(this, wifiP2pManager, channel)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages)
        messageRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun setupMessageHandlers() {
        WifiCommunicationService.onMessageReceived = { gonderen, mesaj, id, okundu ->
            handler.post {
                val message = Message(
                    id = id,
                    sender = gonderen,
                    content = mesaj,
                    timestamp = System.currentTimeMillis(),
                    isRead = okundu
                )
                addMessage(message)
            }
        }

        WifiCommunicationService.onConnectionStatusChanged = { isConnected ->
            handler.post {
                updateConnectionStatus(isConnected)
            }
        }

        WifiCommunicationService.onConnectionError = { error ->
            handler.post {
                showError(error)
            }
        }
    }

    private fun sendMessage(messageText: String) {
        try {
            wifiCommunicationService.sendMessage("all", messageText)
        } catch (e: Exception) {
            Log.e(TAG, "Mesaj gönderme hatası: ${e.message}")
            showError("Mesaj gönderilemedi: ${e.message}")
        }
    }

    private fun addMessage(message: Message) {
        messages.add(message)
        messageAdapter.notifyItemInserted(messages.size - 1)
        messageRecyclerView.scrollToPosition(messages.size - 1)
        saveMessages()
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        if (isConnected) {
            Toast.makeText(this, "✅ Bağlantı kuruldu", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "⚠️ Bağlantı kesildi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(error: String) {
        Toast.makeText(this, "❌ $error", Toast.LENGTH_LONG).show()
    }

    private fun saveMessages() {
        try {
            val json = gson.toJson(messages)
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_MESSAGES, json)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Mesajları kaydetme hatası: ${e.message}")
        }
    }

    private fun loadMessages() {
        try {
            val json = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_MESSAGES, null)
            if (json != null) {
                val type = object : TypeToken<List<Message>>() {}.type
                val loadedMessages = gson.fromJson<List<Message>>(json, type)
                messages.clear()
                messages.addAll(loadedMessages)
                messageAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Mesajları yükleme hatası: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val TAG = "ChatActivity"
        private const val PREFS_NAME = "ChatPrefs"
        private const val KEY_MESSAGES = "messages"
    }
}

data class Message(
    val id: Int,
    val sender: String,
    val content: String,
    val timestamp: Long,
    var isRead: Boolean = false
)

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    class MessageViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val senderText: TextView = view.findViewById(R.id.senderText)
        val messageText: TextView = view.findViewById(R.id.messageText)
        val timeText: TextView = view.findViewById(R.id.timeText)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MessageViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.senderText.text = message.sender
        holder.messageText.text = message.content
        holder.timeText.text = dateFormat.format(Date(message.timestamp))
    }

    override fun getItemCount() = messages.size
} 