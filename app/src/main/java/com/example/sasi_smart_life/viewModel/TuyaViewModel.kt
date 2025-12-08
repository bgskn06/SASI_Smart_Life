package com.example.sasi_smart_life.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sasi_smart_life.data.repository.TuyaPairingRepository
import com.thingclips.smart.sdk.bean.DeviceBean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Data Class & Enum Tetap Sama ---
enum class PairingStep {
    IDLE,       // Diam / Standby
    GET_TOKEN,  // Sedang minta token ke server
    SCANNING,   // Sedang mencari device (EZ Mode)
    CONNECTING, // Device ketemu, sedang register ke cloud
    SUCCESS,    // Berhasil
    ERROR       // Gagal
}

data class PairingUiState(
    val step: PairingStep = PairingStep.IDLE,
    val error: String? = null
)

// --- GANTI NAMA CLASS ---
class TuyaViewModel : ViewModel() {

    private val pairingRepo = TuyaPairingRepository()

    private val _pairingState = MutableStateFlow(PairingUiState())
    val pairingState = _pairingState.asStateFlow()

    // Fungsi Pairing (Sama seperti sebelumnya)
    fun startPairing(
        context: Context,
        homeId: Long,
        ssid: String,
        password: String,
        onSuccess: (DeviceBean) -> Unit
    ) {
        _pairingState.value = PairingUiState(step = PairingStep.GET_TOKEN)

        pairingRepo.getPairingToken(homeId) { token ->
            if (token == null) {
                _pairingState.value = PairingUiState(step = PairingStep.ERROR, error = "Failed to get token")
                return@getPairingToken
            }

            _pairingState.value = _pairingState.value.copy(step = PairingStep.SCANNING)

            pairingRepo.startPairing(
                context = context,
                ssid = ssid,
                password = password,
                token = token,
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
    }

    // Nanti kita akan tambahkan fungsi control di sini:
    // fun toggleDevice(devId: String, currentStatus: Boolean) { ... }
}