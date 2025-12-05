package com.example.sasi_smart_life.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder
import com.thingclips.smart.sdk.api.IThingActivator
import com.thingclips.smart.sdk.api.IThingActivatorGetToken
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.enums.ActivatorModelEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PairingStep {
    IDLE,
    GETTING_TOKEN,
    SCANNING,
    CONNECTING,
    SUCCESS,
    ERROR
}

data class PairingState(
    val step: PairingStep = PairingStep.IDLE,
    val error: String? = null
)

class TuyaPairingViewModel : ViewModel() {

    private var mThingActivator: IThingActivator? = null

    private val _uiState = MutableStateFlow(PairingState())
    val uiState = _uiState.asStateFlow()

    fun startPairing(
        context: Context,
        homeId: Long,
        ssid: String,
        pass: String,
        onDevicePaired: (DeviceBean) -> Unit
    ) {
        _uiState.value = PairingState(step = PairingStep.GETTING_TOKEN)
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId, object :
            IThingActivatorGetToken {
            override fun onSuccess(token: String) {
                _uiState.value = PairingState(step = PairingStep.SCANNING)
                easyPairing(context, ssid, pass, token, onDevicePaired)
            }

            override fun onFailure(s: String, s1: String) {
                _uiState.value = PairingState(step = PairingStep.ERROR, error = "Failed to get token: $s1")
            }
        })
    }

    private fun easyPairing(
        context: Context,
        ssid: String,
        pass: String,
        token: String,
        onDevicePaired: (DeviceBean) -> Unit
    ) {
        val builder = ActivatorBuilder()
            .setSsid(ssid)
            .setContext(context)
            .setPassword(pass)
            .setActivatorModel(ActivatorModelEnum.THING_EZ)
            .setTimeOut(100)
            .setToken(token)
            .setListener(object : IThingSmartActivatorListener {
                override fun onError(errorCode: String, errorMsg: String) {
                    _uiState.value = PairingState(step = PairingStep.ERROR, error = errorMsg)
                    stopPairing()
                }

                override fun onActiveSuccess(devResp: DeviceBean) {
                    _uiState.value = PairingState(step = PairingStep.SUCCESS)
                    onDevicePaired(devResp) // Report back to the caller
                }

                override fun onStep(step: String, data: Any) {
                    if ("device_find" == step) {
                        _uiState.value = PairingState(step = PairingStep.CONNECTING)
                    }
                }
            })

        mThingActivator = ThingHomeSdk.getActivatorInstance().newMultiActivator(builder)
        mThingActivator?.start()
    }

    fun stopPairing() {
        mThingActivator?.stop()
        mThingActivator?.onDestroy()
        mThingActivator = null
        _uiState.value = PairingState(step = PairingStep.IDLE)
    }

    override fun onCleared() {
        super.onCleared()
        stopPairing()
    }
}
