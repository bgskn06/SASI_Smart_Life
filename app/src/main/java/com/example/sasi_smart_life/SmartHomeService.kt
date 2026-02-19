package com.example.sasi_smart_life

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.Home
import com.example.sasi_smart_life.data.models.SceneAction
import com.example.sasi_smart_life.data.models.SceneCondition
import com.example.sasi_smart_life.data.models.SceneSchedule
import com.example.sasi_smart_life.data.models.SmartScene
import com.example.sasi_smart_life.data.repository.FBDeviceRepository
import com.example.sasi_smart_life.data.repository.FBHomeRepository
import com.example.sasi_smart_life.data.repository.FBSceneRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SmartHomeService : Service() {

    private val tag = "SmartHomeService"

    private val deviceRepo = FBDeviceRepository()
    private val sceneRepo = FBSceneRepository()
    private val homeRepo = FBHomeRepository()

    // Global Cache
    private val globalScenesCache = mutableListOf<SmartScene>()
    private val globalDevicesCache = mutableListOf<Device>()

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Service Created")
        startForegroundService()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            initGlobalLogic(uid)
        }
    }

    private fun startForegroundService() {
        val channelId = "SmartHomeChannel"
        val channelName = "Smart Home Automation"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SASI Smart Home")
            .setContentText("Monitoring automation running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // 🔥 PERBAIKAN UNTUK ANDROID 14 (API 34) 🔥
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Tentukan tipe service sesuai Manifest
            val serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE

            startForeground(1, notification, serviceType)
        } else {
            startForeground(1, notification)
        }
    }

    private fun initGlobalLogic(uid: String) {
        homeRepo.getHomesByUser(uid) { rawData ->

            val homes = rawData.map { m ->
                Home(
                    homeId = m["homeId"] as? String ?: "",
                    name = m["name"] as? String ?: "",
                    ownerUid = m["ownerUid"] as? String ?: "",
                    imageUrl = m["imageUrl"] as? String ?: "",
                    tuyaHomeId = (m["tuyaHomeId"] as? Number)?.toLong() ?: 0L
                )
            }

            loadGlobalLogicData(homes)
        }
    }

    private fun loadGlobalLogicData(homes: List<Home>) {
        homes.forEach { home ->
            sceneRepo.getScenes(home.homeId) { list ->
                val scenes = list.mapNotNull { m ->
                    try {
                        SmartScene(
                            sceneId = m["sceneId"].toString(),
                            homeId = home.homeId,
                            isActive = m["isActive"] as? Boolean ?: true,
                            name = m["name"] as? String ?: "Unknown",
                            ifData = SceneCondition(
                                devId = (m["if"] as? Map<*, *>)?.get("devId") as? String ?: "",
                                operator = (m["if"] as? Map<*, *>)?.get("operator") as? String
                                    ?: "==",
                                status = ((m["if"] as? Map<*, *>)?.get("status") as? Number)?.toInt()
                                    ?: 0
                            ),
                            schedule = SceneSchedule(
                                enabled = (m["schedule"] as? Map<*, *>)?.get("enabled") as? Boolean
                                    ?: false,
                                startTime = (m["schedule"] as? Map<*, *>)?.get("startTime") as? String
                                    ?: "00:00",
                                endTime = (m["schedule"] as? Map<*, *>)?.get("endTime") as? String
                                    ?: "23:59",
                                days = (m["schedule"] as? Map<*, *>)?.get("days") as? Map<String, Boolean>
                                    ?: emptyMap()
                            ),
                            thenAction = (m["then"] as? List<Map<String, Any>>)?.map {
                                SceneAction(it["devId"] as String, (it["status"] as Number).toInt())
                            } ?: emptyList()
                        )
                    } catch (e: Exception) { null }
                }
                globalScenesCache.removeAll { it.homeId == home.homeId }
                globalScenesCache.addAll(scenes)
            }

            // Tambahkan Listener dan simpan referensinya
            deviceRepo.getDevicesByHomeListener(home.homeId) { list ->
                val devices = list.map { m ->
                    Device(
                        devId = m["devId"] as? String ?: "",
                        name = m["name"] as? String ?: "",
                        homeId = m["homeId"] as? String ?: "",
                        roomId = m["roomId"] as? String,
                        status = (m["status"] == true || m["status"] == 1L)
                    )
                }

                // Cek perubahan untuk trigger otomatisasi
                devices.forEach { newDeviceData ->
                    val oldDeviceData = globalDevicesCache.find { it.devId == newDeviceData.devId }
                    // Hanya cek scene jika statusnya BERUBAH (mencegah loop)
                    if (oldDeviceData?.status != newDeviceData.status) {
                        checkAndExecuteScene(newDeviceData)
                    }
                }

                globalDevicesCache.removeAll { it.homeId == home.homeId }
                globalDevicesCache.addAll(devices)
            }
        }
    }
    private fun checkAndExecuteScene(triggerDevice: Device) {
        try {
            globalScenesCache.filter { it.isActive && it.ifData.devId == triggerDevice.devId }
                .forEach { scene ->
                    val currentStatus = if (triggerDevice.status) 1 else 0
                    val threshold = scene.ifData.status

                    val isTriggered = when (scene.ifData.operator) {
                        ">" -> currentStatus > threshold
                        "<" -> currentStatus < threshold
                        else -> currentStatus == threshold
                    }

                    if (isTriggered && isTimeValid(scene)) {
                        executeScene(scene)
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Gagal memproses scene untuk device ${triggerDevice.devId}: ${e.message}")
        }
    }
    private fun isTimeValid(scene: SmartScene): Boolean {
        // 1. Pastikan schedule tidak null
        val schedule = scene.schedule ?: return true

        // 2. Jika tidak diaktifkan, abaikan pengecekan waktu
        if (schedule.enabled != true) return true

        val calendar = Calendar.getInstance()

        // 3. Cek Hari (Safe Handling)
        val dayFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val currentDay = dayFormat.format(calendar.time).lowercase()

        if (scene.schedule.days[currentDay] != true) return false

        // 4. Cek Jam (Safe Parsing)
        try {
            val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
            val nowMin = calendar.get(Calendar.MINUTE)
            val nowInMinutes = nowHour * 60 + nowMin

            val startParts = schedule.startTime.split(":")
            val startInMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()

            val endParts = schedule.endTime.split(":")
            val endInMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()

            return if (startInMinutes < endInMinutes) {
                nowInMinutes in startInMinutes..endInMinutes
            } else {
                nowInMinutes >= startInMinutes || nowInMinutes <= endInMinutes
            }
        } catch (e: Exception) {
            Log.e("SmartHomeService", "Error parsing time: ${e.message}")
            return true // Default true agar tidak menghalangi aksi jika format salah
        }
    }
    private fun executeScene(scene: SmartScene) {
        scene.thenAction.forEach { action ->
            val targetDevice = globalDevicesCache.find { it.devId == action.devId }

            // HANYA update jika status saat ini berbeda dengan status target scene
            val currentStatusInt = if (targetDevice?.status == true) 1 else 0
            if (currentStatusInt != action.status) {
                targetDevice?.roomId?.let { roomId ->
                    deviceRepo.updateDeviceStatus(action.devId, roomId, action.status) { success, _ ->
                        if(success) Log.d(tag, "✅ Action Success: ${scene.name}")
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY: Jika sistem membunuh service, service akan direstart otomatis
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(tag, "Service dihentikan. Mematikan semua monitoring...")
        deviceRepo.removeDevicesListener() // Matikan semua listener rumah
        super.onDestroy()
    }
}