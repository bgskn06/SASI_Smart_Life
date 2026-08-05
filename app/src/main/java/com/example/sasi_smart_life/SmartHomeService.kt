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
import com.example.sasi_smart_life.data.models.*
import com.example.sasi_smart_life.data.repository.FBDeviceRepository
import com.example.sasi_smart_life.data.repository.FBHomeRepository
import com.example.sasi_smart_life.data.repository.FBSceneRepository
import com.google.firebase.auth.FirebaseAuth
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.api.IThingHomeStatusListener
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IDevListener
import com.thingclips.smart.sdk.bean.DeviceBean
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.math.log

class SmartHomeService : Service() {

    private val tag = "SmartHomeService_SASI"

    private val deviceRepo = FBDeviceRepository()
    private val sceneRepo = FBSceneRepository()
    private val homeRepo = FBHomeRepository()
    private val globalScenesCache = mutableListOf<SmartScene>()
    private val globalDevicesCache = mutableListOf<Device>()

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val homeId = intent?.getLongExtra("TUYA_HOME_ID", 0L) ?: 0L

        if (uid != null) {
            initGlobalLogic(uid)
        }
        if (homeId != 0L) {
            startListeningToHome(homeId)
        }
        return START_STICKY
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
                    val tuyaMap = m["tuyaInfo"] as? Map<*, *>
                    Device(
                        devId = m["devId"] as? String ?: "",
                        name = m["name"] as? String ?: "",
                        homeId = m["homeId"] as? String ?: "",
                        roomId = m["roomId"] as? String,
                        status = (m["status"] == true || m["status"] == 1L),
                        tuyaInfo = TuyaInfo(
                            temp = (tuyaMap?.get("temp") as? Number)?.toDouble(),
                            humidity = (tuyaMap?.get("humidity") as? Number)?.toInt()
                        )
                    )
                }

                devices.forEach { newDevice ->
                    val oldDevice = globalDevicesCache.find { it.devId == newDevice.devId }
                    val statusChanged = oldDevice?.status != newDevice.status
                    val tempChanged = oldDevice?.tuyaInfo?.temp != newDevice.tuyaInfo?.temp

                    Log.d(tag, "Device ${newDevice.devId}: oldTemp=${oldDevice?.tuyaInfo?.temp} newTemp=${newDevice.tuyaInfo?.temp} statusChanged=$statusChanged tempChanged=$tempChanged")
                    if (statusChanged || tempChanged) {
                        checkAndExecuteScene(newDevice)
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
                    val deviceToCheck = if (condition.devId == triggerDevice.devId) triggerDevice else deviceInCache

                    if (deviceToCheck == null) return@map false

                    val currentValue: Double = when {
                        deviceToCheck.tuyaInfo?.temp != null -> deviceToCheck.tuyaInfo.temp
                        else -> if (deviceToCheck.status) 1.0 else 0.0
                    }

                    val conditionValue: Double = condition.status.toDouble()

                    when (condition.operator) {
                        "==" -> currentValue == conditionValue
                        "!=" -> currentValue != conditionValue
                        ">"  -> currentValue > conditionValue
                        "<"  -> currentValue < conditionValue
                        else -> currentValue == conditionValue
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

        if (!schedule.enabled != true) return true

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


    private var currentListeningHomeId: Long? = null
    private val activeListeners = mutableMapOf<String, IDevListener>()
    private val localDeviceCache = mutableMapOf<String, MutableMap<String, Any>>()
    private val deviceCategoryMap = mutableMapOf<String, String>()

    private var homeStatusListener: IThingHomeStatusListener? = null

    private val pendingDpQueue = mutableMapOf<String, MutableList<String>>()

    fun startListeningToHome(homeId: Long) {
        if (currentListeningHomeId == homeId) {
            Log.d(tag, "Listener already running $homeId")
            return
        }

        currentListeningHomeId = homeId

        val cachedHomeBean = ThingHomeSdk.getDataInstance().getHomeBean(homeId)
        if (cachedHomeBean != null) {
            Log.d(tag, "🚀 Menggunakan Cache Device List untuk Listener Awal")
            registerAllDevices(cachedHomeBean.deviceList)
        }

        val homeInstance = ThingHomeSdk.newHomeInstance(homeId)
        homeInstance.getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                if (bean != null) {
                    Log.d(tag, "✅ Data Home Terupdate dari Server")
                    registerAllDevices(bean.deviceList)
                }
            }
            override fun onError(code: String?, error: String?) {
                Log.e(tag, "Gagal refresh home: $error")
            }
        })

        if (homeStatusListener == null) {
            homeStatusListener = object : IThingHomeStatusListener {
                override fun onDeviceAdded(devId: String?) {
                    if (devId != null) {

                        val device = ThingHomeSdk.getDataInstance().getDeviceBean(devId)
                        val cat = device?.productBean?.category ?: "unknown"

                        deviceCategoryMap[devId] = cat

                        registerListenerForDevice(devId)

                        // proses DP yang mungkin datang duluan
                        processPendingDp(devId)
                    }
                }
                override fun onDeviceRemoved(devId: String?) {
                }
                override fun onGroupAdded(groupId: Long) {}
                override fun onGroupRemoved(groupId: Long) {}
                override fun onMeshAdded(meshId: String?) {}
            }
            homeInstance.registerHomeStatusListener(homeStatusListener)
        }
    }

    private fun registerAllDevices(devices: List<DeviceBean>?) {
        devices?.forEach { dev ->
            if (!activeListeners.containsKey(dev.devId)) {

                // ✅ SET CATEGORY DULU
                val cat = dev.productBean.category ?: "unknown"
                deviceCategoryMap[dev.devId] = cat

                // ✅ BARU REGISTER LISTENER
                registerListenerForDevice(dev.devId)

                if (!userMappingCache.containsKey(dev.devId)) {
                    deviceRepo.observeUserMapping(dev.devId) { mapping ->
                        userMappingCache[dev.devId] = mapping
                    }
                }

                // ✅ PROSES DP YANG KE-QUEUE (kalau ada)
                processPendingDp(dev.devId)
            }
        }
    }

    private fun registerListenerForDevice(devId: String) {
        if (activeListeners.containsKey(devId)) return

        val iDevice = ThingHomeSdk.newDeviceInstance(devId) ?: return

        val listener = object : IDevListener {

            override fun onDpUpdate(devId: String, dpStr: String) {
                Log.w(tag, "devId : $devId Update: $dpStr")
                filterDp(devId, dpStr)
            }

            override fun onStatusChanged(devId: String, online: Boolean) {
                Log.d(tag, "Status Cloud Device $devId: $online")
                updateOnlineStatus(devId, online)
            }

            override fun onNetworkStatusChanged(devId: String, status: Boolean) {
                Log.d(tag, "Status Network Device $devId: $status")
                updateOnlineStatus(devId, status)
            }

            override fun onDevInfoUpdate(devId: String) {
            }

            override fun onRemoved(devId: String) {
                Log.d(tag, "Device Removed: $devId")
                activeListeners.remove(devId)
            }

            private fun updateOnlineStatus(devId: String, status: Boolean){
                deviceRepo.updateOnline(devId, status)
            }
        }

        iDevice.registerDevListener(listener)

        activeListeners[devId] = listener
    }

    private val userMappingCache = mutableMapOf<String, Map<String, String>>()
    private fun filterDp(devId: String, dpStr: String) {
        try {
            Log.d(tag, "Filter DP: $devId")

            val category = deviceCategoryMap[devId]

            if (category == null) {
                Log.w("DP_DEBUG", "Queue DP, category not ready: $devId")

                val queue = pendingDpQueue.getOrPut(devId) { mutableListOf() }
                queue.add(dpStr)

                return
            }

            val json = JSONObject(dpStr)

            val currentData = localDeviceCache.getOrPut(devId) { mutableMapOf() }
            val changes = mutableMapOf<String, Any>()
            val now = System.currentTimeMillis()
            val dateFormat = java.text.SimpleDateFormat("dd MMM HH:mm:ss", java.util.Locale.getDefault())
            val waktu = dateFormat.format(java.util.Date(now))

            val keys = json.keys()
            while (keys.hasNext()) {
                val dpId = keys.next()
                val newValue = json.get(dpId)
                val oldValue = currentData[dpId]

                currentData[dpId] = newValue
                changes["tuyaInfo/dps/$dpId"] = newValue

                when (category) {
                    "mcs" -> { // Door Sensor
                        if (newValue != oldValue && dpId == "1") {
                            val isOpen = newValue.toString().toBoolean()
                            changes["status"] = if (isOpen) 0 else 1
                            val historyMap = mapOf(
                                "status" to (if (isOpen) "OPEN" else "CLOSE"),
                                "description" to (if (isOpen) "Pintu Terbuka" else "Pintu Tertutup"),
                                "time" to waktu
                            )
                            deviceRepo.addLogHistory(devId, historyMap, timestampId = now)
//                            printHistory(devId, historyMap)
                        }
                        if (newValue != oldValue && dpId == "2") {
                            val battLevel = newValue.toString().toDoubleOrNull()?.toInt() ?: 0
                            changes["tuyaInfo/batt"] = battLevel
                        }
                    }
                    "ms" -> { // Smart Lock
                        if (json.length() == 1 || newValue != oldValue) {
                            if(dpId == "1" || dpId == "2" || dpId == "5"){
                                val userId = newValue.toString()
                                val deviceKamus = userMappingCache[devId] ?: emptyMap()
                                val finalName = deviceKamus[userId] ?: "Unknown ID ($userId)"
                                val historyMap = mapOf(
                                    "status" to "OPEN",
                                    "method" to if(dpId == "1") "FINGERPRINT" else if (dpId == "2") "PASSWORD" else "CARD",
                                    "description" to "Dibuka oleh $finalName",
                                    "userId" to userId,
                                    "time" to waktu
                                )
                                deviceRepo.addLogHistory(devId, historyMap, timestampId = now)
//                                printHistory(devId,  historyMap)
                            }
                        }
                    }

                    "wsdcg" -> { // Temp Sensor
                        if(newValue != oldValue) {
                            if (dpId == "1"){
                                val temp = newValue.toString().toInt() / 10.0
                                changes["tuyaInfo/temperature"] = temp
                            }
                            if (dpId == "2"){
                                val hum = newValue.toString().toDouble()
                                changes["tuyaInfo/humidity"] = hum
                            }
                        }
                    }

                    "mk" -> { // Smart Lock

                        when (dpId) {

                            // =========================
                            // ACCESS EVENT
                            // =========================
                            "10", "11" -> {

                                if (json.length() == 1 || newValue != oldValue) {

                                    val rawValue = newValue.toString()

                                    val userId = try {
                                        rawValue.substring(6, 8)
                                            .toInt(16)
                                            .toString()
                                    } catch (e: Exception) {
                                        rawValue
                                    }

                                    val deviceKamus =
                                        userMappingCache[devId] ?: emptyMap()

                                    val finalName =
                                        deviceKamus[userId] ?: "Unknown ID ($userId)"

                                    val method = when (dpId) {
                                        "10" -> "FINGERPRINT"
                                        "11" -> "PIN"
                                        else -> "UNKNOWN"
                                    }

                                    val historyMap = mapOf(
                                        "status" to "OPEN",
                                        "method" to method,
                                        "description" to "Dibuka oleh $finalName",
                                        "userId" to userId,
                                        "time" to waktu
                                    )

                                    deviceRepo.addLogHistory(
                                        devId,
                                        historyMap,
                                        timestampId = now
                                    )
                                }
                            }

                            // =========================
                            // DOOR STATUS
                            // =========================
                            "35" -> {

                                if (newValue != oldValue) {

                                    when (newValue.toString()) {

                                        "010001" -> {
                                            changes["tuyaInfo/status"] = "OPEN"
                                        }

                                        "010002" -> {
                                            changes["tuyaInfo/status"] = "CLOSE"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (changes.isNotEmpty()) {
//                printLog(devId, changes)
                deviceRepo.updateDp(devId,changes)
            }

        } catch (e: Exception) {
            Log.e(tag, "Parse Error", e)
        }
    }

    private fun printLog(devId: String, changes: Map<String, Any>) {
        val name = ThingHomeSdk.getDataInstance().getDeviceBean(devId)?.name ?: "Unknown"

        val sb = StringBuilder()
        sb.append("\n╔════ UPDATE: $name ════\n")

        changes.forEach { (key, value) ->
            sb.append(String.format("║ %-8s : %s\n", key, value.toString()))
        }

        sb.append("╚════════════════════════════════════════")

        Log.i(tag, sb.toString())
    }

    private fun processPendingDp(devId: String) {
        val queue = pendingDpQueue[devId] ?: return

        Log.d("DP_DEBUG", "Processing ${queue.size} queued DP for $devId")

        queue.forEach { dp ->
            filterDp(devId, dp)
        }

        pendingDpQueue.remove(devId)
    }

    private fun stopAllListeners() {
        activeListeners.forEach { (devId) ->
            val iDevice = ThingHomeSdk.newDeviceInstance(devId)
            iDevice?.unRegisterDevListener()
        }
        activeListeners.clear()
        Log.d(tag, "Semua listener dibersihkan")
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(tag, "Service dihentikan. Mematikan semua monitoring...")
        stopAllListeners()
        deviceRepo.removeDevicesListener() // Matikan semua listener rumah
        super.onDestroy()
    }
}