package com.example.sasi_smart_life.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sasi_smart_life.data.repository.FBDeviceRepository
import com.example.sasi_smart_life.data.repository.TuyaPairingRepository
import com.google.firebase.database.FirebaseDatabase
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IDevListener
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

class TuyaViewModel : ViewModel() {

    private val pairingRepo = TuyaPairingRepository()
    private val deviceRepo = FBDeviceRepository()

    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    val tag = "TuyaVM_SASI"

    // Fungsi Add Device //
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


    // Listener //
    private var currentListeningHomeId: Long? = null
    private val activeListeners = mutableMapOf<String, IDevListener>()

    private val localDeviceCache = mutableMapOf<String, MutableMap<String, Any>>()
    private val deviceCategoryMap = mutableMapOf<String, String>()
    private val lastHistoryTime = mutableMapOf<String, Long>()


    private fun registerListenerForDevice(devId: String) {
        if (activeListeners.containsKey(devId)) return

        val iDevice = ThingHomeSdk.newDeviceInstance(devId) ?: return

        val listener = object : IDevListener {

            override fun onDpUpdate(devId: String, dpStr: String) {
                filterDp(devId, dpStr)
            }

            override fun onStatusChanged(devId: String, online: Boolean) {

            }

            override fun onNetworkStatusChanged(devId: String, status: Boolean) {
            }

            override fun onDevInfoUpdate(devId: String) {
            }

            override fun onRemoved(devId: String) {
                Log.d(tag, "Device Removed: $devId")
                activeListeners.remove(devId)
            }
        }

        iDevice.registerDevListener(listener)

        activeListeners[devId] = listener
    }

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

                if (newValue != oldValue) {
                    currentData[dpId] = newValue

                    changes["tuyaInfo/dps/$dpId"] = newValue

                    when (category) {
                        "mcs" -> { // Door Sensor
                            if (dpId == "1") {
                                val isOpen = newValue.toString().toBoolean()
                                changes["status"] = if (isOpen) 0 else 1
                                val historyMap = mapOf(
                                    "status" to (if (isOpen) "OPEN" else "CLOSE"),
                                    "description" to (if (isOpen) "Pintu Terbuka" else "Pintu Tertutup"),
                                    "time" to waktu
                                )
                                deviceRepo.addLogHistory(devId, historyMap, timestampId = now)
//                                printHistory(devId, historyMap)
                            }
                        }
                        "ms" -> { // Smart Lock
                            if(dpId == "1" || dpId == "2" || dpId == "5"){
                                val userId = newValue.toString()
                                val historyMap = mapOf(
                                    "status" to "OPEN",
                                    "method" to if(dpId == "1") "FINGERPRINT" else if (dpId == "2") "PASSWORD" else  "CARD",
                                    "description" to "Dibuka oleh ID: $newValue",
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


    fun startListeningToHome(homeId: Long) {
        if (currentListeningHomeId == homeId) return
        currentListeningHomeId = homeId

        Log.d(tag, "Mulai melacak Home: $homeId")

        val homeInstance = ThingHomeSdk.newHomeInstance(homeId)
        homeInstance.getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                val devices = bean?.deviceList ?: emptyList()
                Log.d(tag, "Ditemukan ${devices.size} device. Mendaftarkan listener...")

                devices.forEach { dev ->
                    registerListenerForDevice(dev.devId)
                    val cat = dev.productBean.category ?: "unknown"
                    deviceCategoryMap[dev.devId] = cat
//                    val dps = dev.dps
//                    if (dps != null) {
//                    }
                }
            }

            override fun onError(code: String?, error: String?) {
                Log.e(tag, "Gagal load home: $error")
            }
        })
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