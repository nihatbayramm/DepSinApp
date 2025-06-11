package com.example.depsinn

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import kotlinx.coroutines.Job
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WifiCommunicationService private constructor(private val context: Context) {
    private var currentUser: String = "Android"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val handler = Handler(Looper.getMainLooper())
    private var isPolling = false
    private var lastMessageId = 0
    private var appContext: Context? = null
    private var isInitialized = false

    private val yasakliKelimeler = setOf(
        "küfür1", "küfür2", "küfür3", // Buraya yasaklı kelimeleri ekleyin
        "argo1", "argo2", "argo3",
        "cinsel1", "cinsel2", "cinsel3"
    )

    private val contentFilter = ContentFilter.getInstance()

    private val executor = Executors.newFixedThreadPool(2)
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var messageListener: ((String) -> Unit)? = null

    fun initialize(context: Context) {
        try {
            if (isInitialized) {
                Log.w(TAG, "WifiCommunicationService zaten başlatılmış")
                return
            }

            appContext = context.applicationContext
            if (isNetworkAvailable()) {
                connectToServer({
                    isInitialized = true
                    startMessagePolling()
                }, { error ->
                    handleConnectionError(error)
                })
            } else {
                handleConnectionError("İnternet bağlantısı yok")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Başlatma hatası: ${e.message}")
            handleConnectionError("Başlatma hatası: ${e.message}")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = appContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Log.e(TAG, "Ağ kontrolü hatası: ${e.message}")
            false
        }
    }

    private fun showUsernameDialog() {
        try {
            handler.post {
                val context = appContext ?: return@post
                val input = EditText(context)
                input.hint = "Kullanıcı adınızı girin"

                AlertDialog.Builder(context)
                    .setTitle("Kullanıcı Adı")
                    .setView(input)
                    .setMessage("Lütfen kullanıcı adınızı girin")
                    .setPositiveButton("Tamam") { _, _ ->
                        val username = input.text.toString().trim()
                        if (username.isNotEmpty()) {
                            setCurrentUser(username)
                            startMessagePolling()
                            Toast.makeText(context, "Hoş geldiniz, $username!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Lütfen geçerli bir kullanıcı adı girin", Toast.LENGTH_SHORT).show()
                            showUsernameDialog()
                        }
                    }
                    .setCancelable(false)
                    .show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Dialog gösterme hatası: ${e.message}")
        }
    }

    fun connectToServer(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isNetworkAvailable()) {
            onError("İnternet bağlantısı yok")
            return
        }

        try {
            val request = Request.Builder()
                .url("$SERVER_URL/connect")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    lastError = e.message
                    handleConnectionError("Bağlantı hatası: ${e.message}")
                    onError(e.message ?: "Bilinmeyen hata")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            isConnected.set(true)
                            onConnectionStatusChanged?.invoke(true)
                            onSuccess()
                            startHeartbeat()
                        } else {
                            val error = "Sunucu hatası: ${response.code}"
                            lastError = error
                            handleConnectionError(error)
                            onError(error)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Yanıt işleme hatası: ${e.message}")
                        onError("Yanıt işleme hatası: ${e.message}")
                    } finally {
                        response.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Bağlantı hatası: ${e.message}")
            onError("Bağlantı hatası: ${e.message}")
        }
    }

    fun sendMessage(alici: String, mesaj: String) {
        if (!isConnected.get()) {
            handleConnectionError("Sunucuya bağlı değil")
            return
        }

        if (!contentFilter.mesajiKontrolEt(mesaj, appContext ?: context)) {
            return
        }

        val filteredMessage = contentFilter.mesajiFiltrele(mesaj)

        try {
            val formBody = FormBody.Builder()
                .add("gonderen", currentUser)
                .add("mesaj", filteredMessage)
                .build()

            val request = Request.Builder()
                .url("$SERVER_URL/gonder")
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Mesaj gönderme hatası: ${e.message}")
                    handleConnectionError("Mesaj gönderilemedi: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) {
                            Log.e(TAG, "Mesaj gönderme hatası: ${response.code}")
                            handleConnectionError("Mesaj gönderilemedi: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Yanıt işleme hatası: ${e.message}")
                    } finally {
                        response.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Mesaj gönderme hatası: ${e.message}")
            handleConnectionError("Mesaj gönderme hatası: ${e.message}")
        }
    }

    private fun startMessagePolling() {
        if (isPolling) return
        isPolling = true

        pollingRunnable = object : Runnable {
            override fun run() {
                if (!isPolling || !isConnected.get()) return

                try {
                    val request = Request.Builder()
                        .url("$SERVER_URL/mesajlar?last_id=$lastMessageId")
                        .get()
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e(TAG, "Mesaj alma hatası: ${e.message}")
                            handleConnectionError("Mesajlar alınamadı: ${e.message}")
                            pollingRunnable?.let { handler.postDelayed(it, POLLING_INTERVAL) }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                if (response.isSuccessful) {
                                    val bodyString = response.body?.string()
                                    if (!bodyString.isNullOrEmpty()) {
                                        val jsonArray = JSONArray(bodyString)
                                        for (i in 0 until jsonArray.length()) {
                                            try {
                                                val json = jsonArray.getJSONObject(i)
                                                val id = json.getInt("id")
                                                val gonderen = json.getString("gonderen")
                                                val mesaj = json.getString("mesaj")
                                                val zaman = json.getString("zaman")

                                                if (id > lastMessageId) {
                                                    lastMessageId = id
                                                    handler.post {
                                                        onMessageReceived?.invoke(gonderen, mesaj, id, false)
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Mesaj ayrıştırma hatası: ${e.message}")
                                            }
                                        }
                                    }
                                } else {
                                    handleConnectionError("Mesajlar alınamadı: ${response.code}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "JSON ayrıştırma hatası: ${e.message}")
                                handleConnectionError("Mesaj işleme hatası: ${e.message}")
                            } finally {
                                response.close()
                                pollingRunnable?.let { handler.postDelayed(it, POLLING_INTERVAL) }
                            }
                        }
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Polling hatası: ${e.message}")
                    handleConnectionError("Polling hatası: ${e.message}")
                    pollingRunnable?.let { handler.postDelayed(it, POLLING_INTERVAL) }
                }
            }
        }
        handler.post(pollingRunnable!!)
    }

    fun stopMessagePolling() {
        isPolling = false
        pollingRunnable?.let { handler.removeCallbacks(it) }
        pollingRunnable = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun handleConnectionError(error: String) {
        lastError = error
        handler.post {
            try {
                onConnectionError?.invoke(error)
                appContext?.let { 
                    Toast.makeText(it, "❌ $error", Toast.LENGTH_LONG).show() 
                }
            } catch (e: Exception) {
                Log.e(TAG, "Hata işleme hatası: ${e.message}")
            }
        }
        handleConnectionLoss()
    }

    private fun handleConnectionLoss() {
        if (isConnected.get()) {
            Log.d(TAG, "Sunucu bağlantısı kesildi, temizleniyor...")
            isConnected.set(false)
            isPolling = false
            handler.post {
                try {
                    onConnectionStatusChanged?.invoke(false)
                    appContext?.let { 
                        Toast.makeText(it, "⚠️ Sunucu bağlantısı kesildi\nYeniden bağlanmaya çalışılıyor...", Toast.LENGTH_LONG).show() 
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Bağlantı kaybı işleme hatası: ${e.message}")
                }
            }
            if (retryCount.incrementAndGet() <= MAX_RETRY_COUNT) {
                Log.d(TAG, "Yeniden bağlanmaya çalışılıyor... (${retryCount.get()}/$MAX_RETRY_COUNT)")
                handler.postDelayed({ 
                    if (!isConnected.get() && isNetworkAvailable()) {
                        connectToServer({
                            startMessagePolling()
                        }, { error ->
                            handleConnectionError(error)
                        })
                    }
                }, RETRY_DELAY)
            } else {
                Log.e(TAG, "Maksimum yeniden deneme sayısına ulaşıldı")
                retryCount.set(0)
                handler.post {
                    try {
                        appContext?.let { 
                            Toast.makeText(it, "❌ Bağlantı başarısız\nLütfen internet bağlantınızı kontrol edin", Toast.LENGTH_LONG).show() 
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Hata mesajı gösterme hatası: ${e.message}")
                    }
                }
            }
        }
    }

    fun cleanup() {
        try {
            Log.d(TAG, "WifiCommunicationService temizleniyor...")
            isConnected.set(false)
            isPolling = false
            isInitialized = false
            handler.removeCallbacksAndMessages(null)
            executor.shutdownNow()
            retryCount.set(0)
            lastError = null
            lastMessageId = 0
            currentUser = "Android"
            stopHeartbeat()
        } catch (e: Exception) {
            Log.e(TAG, "Temizleme hatası: ${e.message}")
        }
    }

    private fun startHeartbeat() {
        if (heartbeatRunnable != null) return
        heartbeatRunnable = object : Runnable {
            override fun run() {
                if (!isConnected.get()) return
                sendHeartbeat()
                handler.postDelayed(this, HEARTBEAT_INTERVAL)
            }
        }
        handler.post(heartbeatRunnable!!)
    }

    private fun stopHeartbeat() {
        heartbeatRunnable?.let { handler.removeCallbacks(it) }
        heartbeatRunnable = null
    }

    private fun sendHeartbeat() {
        if (!isConnected.get()) return

        try {
            val request = Request.Builder()
                .url("$SERVER_URL/heartbeat")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Heartbeat failed: ${e.message}")
                    handleConnectionError("Bağlantı hatası: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) {
                            Log.e(TAG, "Heartbeat failed with code: ${response.code}")
                            handleConnectionError("Sunucu hatası: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Heartbeat yanıt işleme hatası: ${e.message}")
                    } finally {
                        response.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Heartbeat gönderme hatası: ${e.message}")
        }
    }

    fun startServer(port: Int) {
        executor.execute {
            try {
                serverSocket = ServerSocket(port)
                Log.d(TAG, "Sunucu başlatıldı, port: $port")
                while (true) {
                    val client = serverSocket?.accept()
                    client?.let {
                        handleClientConnection(it)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Sunucu hatası: ${e.message}")
            }
        }
    }

    fun connectToPeer(device: WifiP2pDevice, port: Int) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Bağlantı başarılı")
                wifiP2pManager.requestConnectionInfo(channel) { info: WifiP2pInfo ->
                    if (info.groupFormed) {
                        if (info.isGroupOwner) {
                            startServer(port)
                        } else {
                            connectToServer(info.groupOwnerAddress.hostAddress, port)
                        }
                    }
                }
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Bağlantı hatası: $reason")
            }
        })
    }

    private fun connectToServer(hostAddress: String, port: Int) {
        executor.execute {
            try {
                clientSocket = Socket()
                clientSocket?.connect(InetSocketAddress(hostAddress, port), 5000)
                Log.d(TAG, "Sunucuya bağlanıldı")
                handleClientConnection(clientSocket!!)
            } catch (e: IOException) {
                Log.e(TAG, "Sunucuya bağlanma hatası: ${e.message}")
            }
        }
    }

    private fun handleClientConnection(socket: Socket) {
        executor.execute {
            try {
                val buffer = ByteArray(1024)
                val input = socket.getInputStream()
                while (true) {
                    val bytes = input.read(buffer)
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        val filtrelenmisMesaj = contentFilter.mesajiFiltrele(message)
                        messageListener?.invoke(filtrelenmisMesaj)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "İstemci bağlantı hatası: ${e.message}")
            }
        }
    }

    fun sendMessage(message: String) {
        if (!contentFilter.mesajiKontrolEt(message, context)) {
            Log.w(TAG, "Mesaj gönderimi engellendi: Uygunsuz içerik")
            return
        }

        executor.execute {
            try {
                clientSocket?.getOutputStream()?.write(message.toByteArray())
            } catch (e: IOException) {
                Log.e(TAG, "Mesaj gönderme hatası: ${e.message}")
            }
        }
    }

    fun setMessageListener(listener: (String) -> Unit) {
        messageListener = listener
    }

    fun stop() {
        try {
            serverSocket?.close()
            clientSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Kapatma hatası: ${e.message}")
        }
        executor.shutdown()
    }

    companion object {
        private const val TAG = "WifiCommunicationService"
        private const val MAX_RETRY_COUNT = 3
        private const val RETRY_DELAY = 2000L
        private const val POLLING_INTERVAL = 3000L
        private const val HEARTBEAT_INTERVAL = 5000L
        private var instance: WifiCommunicationService? = null
        var onMessageReceived: ((gonderen: String, mesaj: String, id: Int, okundu: Boolean) -> Unit)? = null
        var onConnectionStatusChanged: ((Boolean) -> Unit)? = null
        var onConnectionError: ((String) -> Unit)? = null
        private var SERVER_URL = "http://10.42.0.1:5000"

        fun getInstance(context: Context): WifiCommunicationService {
            if (instance == null) {
                instance = WifiCommunicationService(context)
            }
            return instance!!
        }

        fun setCurrentUser(username: String) {
            instance?.currentUser = username
        }

        fun getCurrentUser(): String {
            return instance?.currentUser ?: "Android"
        }

        fun sendMessage(alici: String, mesaj: String) {
            if (instance?.isInitialized != true) {
                Log.e(TAG, "WifiCommunicationService henüz başlatılmadı")
                return
            }
            instance?.sendMessage(alici, mesaj)
        }

        fun setServerUrl(url: String) {
            SERVER_URL = "http://$url"
        }
    }

    private val isConnected = AtomicBoolean(false)
    private val retryCount = AtomicInteger(0)
    private var lastError: String? = null
    private var heartbeatRunnable: Runnable? = null
    private var pollingRunnable: Runnable? = null
} 