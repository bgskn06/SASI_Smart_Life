package com.example.sasi_smart_life.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sasi_smart_life.data.repository.FBDeviceRepository
import com.example.sasi_smart_life.data.repository.TuyaPairingRepository
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.api.WifiSignalListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.enums.ActivatorModelEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                cachedToken = token
                _pairingState.value = PairingUiState(step = PairingStep.IDLE)
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
    //      CONTROL DEVICE
    // ===========================

    fun controlDevice(devId: String, dpId: String, value: Int) {
        val isMqttConnected = ThingHomeSdk.getServerInstance().isServerConnect()
        Log.d(tag, "MQTT connected: $isMqttConnected")

        if (!isMqttConnected) {
            Log.e(tag, "Device is not connected to the server.")
            return
        }

        val mDevice = ThingHomeSdk.newDeviceInstance(devId)

        try {
            val dps = "{\"$dpId\": $value}"

            mDevice.publishDps(dps, object : IResultCallback {
                override fun onError(code: String, error: String?) {
                    Log.e(tag, "Error updating DP: $code, ${error ?: "Unknown error"}")
                }

                override fun onSuccess() {
                    Log.d(tag, "Successfully updated dp $dpId to $value")
                    getDP(devId, dpId)
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Error: ${e.message}")
        }
    }

    fun getDP(devId: String, dpId: String){
        val mDevice = ThingHomeSdk.newDeviceInstance(devId)
        val device = ThingHomeSdk.getDataInstance().getDeviceBean(devId)

        mDevice.getDp(dpId, object : IResultCallback {
            override fun onError(code: String, error: String?) {
                Log.e(tag, "Error getting DP $dpId: $code, ${error ?: "Unknown error"}")
            }

            override fun onSuccess() {
                val currentValue = device?.dps[dpId]
                Log.d(tag, "Successfully got DP $dpId value: $currentValue")
            }
        })
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
//        stopAllListeners()
    }

//    private fun stopAllListeners() {
//        activeListeners.forEach { (devId) ->
//            val iDevice = ThingHomeSdk.newDeviceInstance(devId)
//            iDevice?.unRegisterDevListener()
//        }
//        activeListeners.clear()
//        Log.d(tag, "Semua listener dibersihkan")
//    }
}