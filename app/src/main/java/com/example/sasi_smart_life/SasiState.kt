package com.example.sasi_smart_life

import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.sdk.bean.DeviceBean

data class SasiState(
    val isLoggedIn: Boolean = false,
    val devices: List<DeviceBean> = emptyList(),
    val homeList: List<HomeBean> = emptyList(),
    val homeDetail: HomeBean? = null,
    val error: String? = null,
    val info: String? = null,
    val currentHomeName: String? = null,
    val currentHomeId: Long? = null,
    val pairingStep: PairingStep = PairingStep.IDLE
) {
    fun asLoggedIn(isLoggedIn: Boolean, info: String?) = copy(
        isLoggedIn = isLoggedIn,
        info = info,
        error = null
    )

    fun withError(error: String?) = copy(error = error, info = null)

    fun withInfo(info: String?) = copy(info = info, error = null)

    fun clearMessages() = copy(info = null, error = null)

    fun withPairingStep(pairingStep: PairingStep) = copy(pairingStep = pairingStep)

    fun withHomeDetail(bean: HomeBean) = copy(
        devices = bean.deviceList,
        homeDetail = bean,
        info = "Home data loaded",
        error = null,
        currentHomeName = bean.name,
        currentHomeId = bean.homeId
    )

    fun withHomeList(homeBeans: List<HomeBean>) = copy(
        homeList = homeBeans
    )

    enum class PairingStep {
        IDLE,
        GETTING_TOKEN,
        SCANNING,
        CONNECTING,
        SUCCESS,
        ERROR
    }
}
