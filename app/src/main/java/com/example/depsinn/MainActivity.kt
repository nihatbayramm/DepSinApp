package com.example.depsinn

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var startChatButton: Button
    private lateinit var wifiCommunicationService: WifiCommunicationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        startChatButton = findViewById(R.id.startChatButton)

        wifiCommunicationService = WifiCommunicationService.getInstance(this)
        wifiCommunicationService.initialize(this)

        wifiCommunicationService.onConnectionStatusChanged = { isConnected ->
            runOnUiThread {
                if (isConnected) {
                    statusText.text = "Sunucuya bağlandı"
                    startChatButton.isEnabled = true
                } else {
                    statusText.text = "Sunucuya bağlanılıyor..."
                    startChatButton.isEnabled = false
                }
            }
        }

        wifiCommunicationService.onConnectionError = { error ->
            runOnUiThread {
                statusText.text = "Bağlantı hatası: $error"
                startChatButton.isEnabled = false
            }
        }

        startChatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiCommunicationService.cleanup()
    }
}

