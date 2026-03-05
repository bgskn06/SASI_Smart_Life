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

    private val tag = "SmartHomeService_SASI"

    private val deviceRepo = FBDeviceRepository()
    private val sceneRepo = FBSceneRepository()
    private val homeRepo = FBHomeRepository()
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
            .setContentTitle("Realtime Automation")
            .setContentText("Monitoring automation running...")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                            logic = m["logic"] as? String ?: "AND",
                            ifData = (m["ifData"] as? List<Map<String, Any>>)?.map {
                                SceneCondition(
                                    devId = it["devId"] as String,
                                    operator = it["operator"] as String,
                                    status = (it["status"] as Number).toInt()
                                )
                            } ?: emptyList(),
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
                            thenAction = (m["thenAction"] as? List<Map<String, Any>>)?.map {
                                SceneAction(it["devId"] as String, (it["status"] as Number).toInt())
                            } ?: emptyList()
                        )
                    } catch (e: Exception) { null }
                }
                globalScenesCache.removeAll { it.homeId == home.homeId }
                globalScenesCache.addAll(scenes)
            }

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

                devices.forEach { newDeviceData ->
                    val oldDeviceData = globalDevicesCache.find { it.devId == newDeviceData.devId }
                    if (oldDeviceData?.status != newDeviceData.status) {
                        checkAndExecuteScene(newDeviceData)
                    }
                }

                globalDevicesCache.removeAll { it.homeId == home.homeId }
                globalDevicesCache.addAll(devices)
            }
        }
        Log.d(tag, "✅ Global Logic Loaded")
    }

    private fun checkAndExecuteScene(triggerDevice: Device) {
        try {
            val relatedScenes = globalScenesCache.filter { scene ->
                scene.isActive && scene.ifData.any { it.devId == triggerDevice.devId }
            }

            relatedScenes.forEach { scene ->
                val conditionResults = scene.ifData.map { condition ->
                    val deviceInCache = globalDevicesCache.find { it.devId == condition.devId }

                    val currentStatus = if (condition.devId == triggerDevice.devId) {
                        if (triggerDevice.status) 1 else 0
                    } else {
                        if (deviceInCache?.status == true) 1 else 0
                    }

                    when (condition.operator) {
                        "==" -> currentStatus == condition.status
                        "!=" -> currentStatus != condition.status
                        ">"  -> currentStatus > condition.status
                        "<"  -> currentStatus < condition.status
                        else -> currentStatus == condition.status
                    }
                }

                val isTriggered = if (scene.logic == "OR") {
                    conditionResults.any { it }
                } else {
                    conditionResults.all { it }
                }

                if (isTriggered && isTimeValid(scene)) {
                    Log.d(tag, "🚀 Executing Scene: ${scene.name}")
                    executeScene(scene)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Gagal memproses scene: ${e.message}")
        }
    }

    private fun isTimeValid(scene: SmartScene): Boolean {
        val schedule = scene.schedule

        if (scene.schedule.enabled != true) return true

        val calendar = Calendar.getInstance()

        val dayFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val currentDay = dayFormat.format(calendar.time).lowercase()

        if (scene.schedule.days[currentDay] != true) return false
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
            return true
        }
    }
    private fun executeScene(scene: SmartScene) {
        scene.thenAction.forEach { action ->
            val targetDevice = globalDevicesCache.find { it.devId == action.devId }

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