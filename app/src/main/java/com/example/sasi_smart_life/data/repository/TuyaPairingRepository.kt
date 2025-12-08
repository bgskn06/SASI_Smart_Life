package com.example.sasi_smart_life.data.repository

import android.content.Context
import android.util.Log
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder
import com.thingclips.smart.sdk.api.IThingActivator
import com.thingclips.smart.sdk.api.IThingActivatorGetToken
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.enums.ActivatorModelEnum

class TuyaPairingRepository {

    private var mThingActivator: IThingActivator? = null
    private val tag = "TuyaPairing_SASI"

    /**
     * Tahap 1: Dapatkan Token Pairing dari Server Tuya
     */
    fun getPairingToken(homeId: Long, onResult: (String?) -> Unit) {
        ThingHomeSdk.getActivatorInstance().getActivatorToken(
            homeId,
            object : IThingActivatorGetToken {
                override fun onSuccess(token: String) {
                    Log.d(tag, "Token received: $token")
                    onResult(token)
                }

                override fun onFailure(errorCode: String?, errorMsg: String?) {
                    Log.e(tag, "Get Token Failed: $errorMsg")
                    onResult(null)
                }
            }
        )
    }

    /**
     * Tahap 2: Mulai Scanning (EZ Mode)
     */
    fun startPairing(
        context: Context,
        ssid: String,
        password: String,
        token: String,
        onDeviceFound: (DeviceBean) -> Unit,
        onError: (String) -> Unit
    ) {
        // Hentikan proses lama jika ada
        stopPairing()

        // Setup Listener (Callback saat device ditemukan/gagal)
        val listener = object : IThingSmartActivatorListener {
            override fun onStep(step: String?, data: Any?) {
                // Info step pairing (misal: "device found, registering to cloud...")
                Log.d(tag, "Step: $step")
            }

            override fun onActiveSuccess(devBean: DeviceBean?) {
                Log.d(tag, "PAIRING SUCCESS! Device: ${devBean?.name}")
                if (devBean != null) {
                    onDeviceFound(devBean)
                }
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
                Log.e(tag, "Pairing Error: $errorCode - $errorMsg")
                onError(errorMsg ?: "Unknown error")
            }
        }

        // Konfigurasi Activator (EZ Mode)
        mThingActivator = ThingHomeSdk.getActivatorInstance().newMultiActivator(
            ActivatorBuilder()
                .setContext(context)
                .setSsid(ssid)
                .setPassword(password)
                .setActivatorModel(ActivatorModelEnum.THING_EZ) // Mode EZ (Kedip Cepat)
                .setTimeOut(100) // Timeout 100 detik
                .setToken(token)
                .setListener(listener)
        )

        // Mulai!
        mThingActivator?.start()
    }

    fun stopPairing() {
        mThingActivator?.stop()
        mThingActivator?.onDestroy()
        mThingActivator = null
    }
}