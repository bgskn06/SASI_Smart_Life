package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thingclips.smart.android.user.api.ILoginCallback
import com.thingclips.smart.android.user.api.IRegisterCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback

class TuyaAuthRepository {

    private val COMMON_PASSWORD = "SasiUser#2024"
    private val COUNTRY_CODE = "62"
    private val tag = "TuyaAuth_SASI"

    private val db = FirebaseDatabase.getInstance("https://sasi-smart-life-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

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

    fun getUserHomeId(uid: String, callback: (homeId: Long) -> Unit) {
        db.child("users").child(uid).child("tuyaHomeId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val homeId: Long = snapshot.getValue(Long::class.java) ?: 0L
                    callback(homeId)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(tag, "Failed to get homeId: $error")
                    callback(0L)
                }
            })
    }

    fun createHome(homeName: String, callback: (homeId: Long?, error: String?) -> Unit) {
        ThingHomeSdk.getHomeManagerInstance().createHome(
            homeName,
            0.0, // lon default
            0.0, // lat default
            "", // geoName
            listOf("Default Room"), // Tuya wajib minimal 1 ruangan
            object : IThingHomeResultCallback {
                override fun onSuccess(bean: HomeBean?) {
                    callback(bean?.homeId, null)
                }

                override fun onError(errorCode: String?, errorMsg: String?) {
                    callback(0L, errorMsg ?: "Unknown error")
                }
            }
        )
    }

    fun updateTuyaHomeId(uid: String, homeId: Long?, callback: (success: Boolean) -> Unit) {
        db.child("users").child(uid).child("tuyaHomeId").setValue(homeId)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { e ->
                Log.e(tag, "Failed to update homeId: $e")
                callback(false)
            }
    }
}