package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.thingclips.smart.android.user.api.ILoginCallback
import com.thingclips.smart.android.user.api.IRegisterCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk

class TuyaAuthRepository {

    private val COMMON_PASSWORD = "SasiUser#2024"
    private val COUNTRY_CODE = "62"
    private val tag = "TuyaAuth_SASI"


    fun loginOrRegisterTuya(
        uid: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (ThingHomeSdk.getUserInstance().isLogin) {
            Log.d(tag, "Already logged in as: ${ThingHomeSdk.getUserInstance().user?.username}")
            onResult(true, null)
            return
        }

        val email = "$uid@sasi.app"

        ThingHomeSdk.getUserInstance().loginWithEmail(
            COUNTRY_CODE,
            email,
            COMMON_PASSWORD,
            object : ILoginCallback {
                override fun onSuccess(user: User?) {
                    Log.d(tag, "Login Success: ${user?.username}")
                    onResult(true, null)
                }

                override fun onError(code: String?, error: String?) {
                    Log.w(tag, "Login failed ($code: $error). Trying to register...")
                    registerTuya(email, onResult)
                }
            }
        )
    }

    private fun registerTuya(email: String, onResult: (Boolean, String?) -> Unit) {
        ThingHomeSdk.getUserInstance().registerAccountWithEmail(
            COUNTRY_CODE,
            email,
            COMMON_PASSWORD,
            object : IRegisterCallback {
                override fun onSuccess(user: User?) {
                    Log.d(tag, "Register Success. Now auto logging in...")
                    loginAfterRegister(email, onResult)
                }

                override fun onError(code: String?, error: String?) {
                    Log.e(tag, "Register Failed: $code - $error")
                    onResult(false, "Tuya Auth Failed: $error")
                }
            }
        )
    }

    private fun loginAfterRegister(email: String, onResult: (Boolean, String?) -> Unit) {
        ThingHomeSdk.getUserInstance().loginWithEmail(
            COUNTRY_CODE,
            email,
            COMMON_PASSWORD,
            object : ILoginCallback {
                override fun onSuccess(user: User?) {
                    onResult(true, null)
                }
                override fun onError(code: String?, error: String?) {
                    onResult(false, "Login after register failed: $error")
                }
            }
        )
    }

    fun logout() {
        if (ThingHomeSdk.getUserInstance().isLogin) {
            ThingHomeSdk.getUserInstance().logout(null)
        }
    }
}