package com.example.sasi_smart_life.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sasi_smart_life.data.repository.FBDeviceRepository
import com.example.sasi_smart_life.data.repository.TuyaPairingRepository
import com.google.firebase.database.FirebaseDatabase
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.api.IThingHomeStatusListener
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IDevListener
import com.thingclips.smart.sdk.api.WifiSignalListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.enums.ActivatorModelEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class PairingStep {
    IDLE, GET_TOKEN, SCANNING, CONNECTING, SUCCESS, ERROR
}

data class PairingUiState(
    val step: PairingStep = PairingStep.IDLE,
    val error: String? = null
)

data class WifiSignalUiState(
    val isLoading: Boolean = false,
    val signalValue: String? = null,
    val error: String? = null
)

class TuyaViewModel : ViewModel() {

    private val pairingRepo = TuyaPairingRepository()
    private val deviceRepo = FBDeviceRepository()

    val tag = "TuyaVM_SASI"


    private val _wifiSignalState = MutableStateFlow(WifiSignalUiState())
    val wifiSignalState = _wifiSignalState.asStateFlow()

    fun checkWifiSignal(devId: String) {
        _wifiSignalState.value = WifiSignalUiState(isLoading = true)

        val device = ThingHomeSdk.newDeviceInstance(devId)

        if (device == null) {
            _wifiSignalState.value = WifiSignalUiState(error = "Device Instance not found")
            return
        }

        Log.d(tag, "Requesting Wifi Signal for $devId")

        device.requestWifiSignal(object : WifiSignalListener {

            override fun onSignalValueFind(signal: String?) {
                Log.d(tag, "Wifi Signal Found: $signal")
                _wifiSignalState.value = WifiSignalUiState(
                    isLoading = false,
                    signalValue = signal ?: "Unknown"
                )
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
                Log.e(tag, "Wifi Signal Error: $errorCode - $errorMsg")
                _wifiSignalState.value = WifiSignalUiState(
                    isLoading = false,
                    error = "$errorMsg ($errorCode)"
                )
            }
        })
    }

    // Panggil ini saat dialog ditutup agar state kembali bersih
    fun resetWifiSignalState() {
        _wifiSignalState.value = WifiSignalUiState()
    }
    // ===========================
    //          PAIRING
    // ===========================
    private var cachedToken: String? = null
    private val _pairingState = MutableStateFlow(PairingUiState())
    val pairingState = _pairingState.asStateFlow()

    fun getApModeToken(homeId: Long, onResult: (Boolean) -> Unit) {
        _pairingState.value = PairingUiState(step = PairingStep.GET_TOKEN)

        pairingRepo.getPairingToken(homeId) { token ->
            if (token != null) {
                cachedToken = token // SIMPAN TOKEN DI SINI
                _pairingState.value = PairingUiState(step = PairingStep.IDLE) // Balik ke IDLE tapi sudah punya token
                onResult(true)
            } else {
                _pairingState.value = PairingUiState(step = PairingStep.ERROR, error = "Gagal ambil token. Pastikan ada internet.")
                onResult(false)
            }
        }
    }

    fun startPairing(
        context: Context,
        homeId: Long,
        ssid: String,
        password: String,
        isAPMode: Boolean,
        onSuccess: (DeviceBean) -> Unit
    ) {
        if (isAPMode ) {
            if (cachedToken == null) {
                _pairingState.value = PairingUiState(step = PairingStep.ERROR, error = "Token belum diambil! Lakukan langkah 1 dulu.")
                return
            }

            _pairingState.value = PairingUiState(step = PairingStep.SCANNING)
            Log.d(tag, "Mengirim SSID: '$ssid' dan Password: '$password'")
            Log.d(tag, "Pairing Mode AP")

            pairingRepo.startPairing(
                context = context,
                ssid = ssid,
                password = password,
                token = cachedToken!!,
                mode = ActivatorModelEnum.THING_AP,
                onDeviceFound = { device ->
                    viewModelScope.launch { onSuccess(device) }
                },
                onError = { err ->
                    viewModelScope.launch { _pairingState.value = PairingUiState(step = PairingStep.ERROR, error = err) }
                }
            )
        } else {
            _pairingState.value = PairingUiState(step = PairingStep.GET_TOKEN)

            pairingRepo.getPairingToken(homeId) { token ->
                if (token == null) {
                    _pairingState.value = PairingUiState(step = PairingStep.ERROR, error = "Failed to get token")
                    return@getPairingToken
                }

                _pairingState.value = _pairingState.value.copy(step = PairingStep.SCANNING)
                Log.d(tag, "Mengirim SSID: '$ssid' dan Password: '$password'")
                Log.d(tag, "Pairing Mode EZ")

                pairingRepo.startPairing(
                    context = context,
                    ssid = ssid,
                    password = password,
                    token = token,
                    mode = ActivatorModelEnum.THING_EZ,
                    onDeviceFound = { device ->
                        viewModelScope.launch {
                            _pairingState.value = _pairingState.value.copy(step = PairingStep.SUCCESS)
                            onSuccess(device)
                        }
                    },
                    onError = { errorMsg ->
                        viewModelScope.launch {
                            _pairingState.value = PairingUiState(step = PairingStep.ERROR, error = errorMsg)
                        }
                    }
                )
            }
        }
    }


    // ===========================
    //          LISTENER
    // ===========================
    private var currentListeningHomeId: Long? = null
    private val activeListeners = mutableMapOf<String, IDevListener>()

    private val localDeviceCache = mutableMapOf<String, MutableMap<String, Any>>()
    private val deviceCategoryMap = mutableMapOf<String, String>()

    private var homeStatusListener: IThingHomeStatusListener? = null

    fun startListeningToHome(homeId: Long) {
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
                    if(devId != null) registerListenerForDevice(devId)
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
                registerListenerForDevice(dev.devId)

                val cat = dev.productBean.category ?: "unknown"
                deviceCategoryMap[dev.devId] = cat

                if (!userMappingCache.containsKey(dev.devId)) {
                    deviceRepo.observeUserMapping(dev.devId) { mapping ->
                        userMappingCache[dev.devId] = mapping
                    }
                }
            }
        }
    }

    private fun registerListenerForDevice(devId: String) {
        if (activeListeners.containsKey(devId)) return

        val iDevice = ThingHomeSdk.newDeviceInstance(devId) ?: return

        val listener = object : IDevListener {

            override fun onDpUpdate(devId: String, dpStr: String) {
                Log.w(tag, "Received DP Update: $dpStr")
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
            val json = JSONObject(dpStr)

            val currentData = localDeviceCache.getOrPut(devId) { mutableMapOf() }
            val changes = mutableMapOf<String, Any>()
            var category = deviceCategoryMap[devId]
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
                                    "method" to if(dpId == "1") "FINGERPRINT" else if (dpId == "2") "PASSWORD" else  "CARD",
                                    "description" to "Dibuka oleh $finalName",
                                    "userId" to userId,
                                    "time" to waktu
                                )
                                deviceRepo.addLogHistory(devId, historyMap, timestampId = now)
//                                printHistory(devId,  historyMap)
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

    private fun printHistory(devId: String, logData: Map<String, Any>) {
        val name = ThingHomeSdk.getDataInstance().getDeviceBean(devId)?.name ?: "Unknown"
        Log.w(tag, """
            
            📜 [HISTORY PREVIEW]
            ------------------------------------------------
            Device      : $name ($devId)
            Data        : $logData
            
        """.trimIndent())
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


    fun stopPairing() {
        pairingRepo.stopPairing()
        _pairingState.value = PairingUiState(step = PairingStep.IDLE)
    }

    fun resetState() {
        _pairingState.value = PairingUiState(step = PairingStep.IDLE, error = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopPairing()
        stopAllListeners()
    }

    private fun stopAllListeners() {
        activeListeners.forEach { (devId) ->
            val iDevice = ThingHomeSdk.newDeviceInstance(devId)
            iDevice?.unRegisterDevListener()
        }
        activeListeners.clear()
        Log.d(tag, "Semua listener dibersihkan")
    }
}