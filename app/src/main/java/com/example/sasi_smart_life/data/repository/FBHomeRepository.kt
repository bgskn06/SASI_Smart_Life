package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

class FBHomeRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://sasi-smart-life-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).reference.child("homes")

    fun createHome(
        homeId: String,
        name: String,
        ownerUid: String,
        imageUrl: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {

        val data = mapOf(
            "name" to name,
            "image" to imageUrl,
            "ownerUid" to ownerUid
        )

        db.child(homeId).setValue(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun getHomesByUser(uid: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        db.orderByChild("ownerUid").equalTo(uid).get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) {
//                    Log.d("HOME_SASI", "No homes found for user: $uid")
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                val homeList = snapshot.children.mapNotNull { childSnapshot ->
                    val homeData = childSnapshot.value as? MutableMap<String, Any>
                    homeData?.set("homeId", childSnapshot.key ?: "")
                    homeData
                }

//                Log.d("HOME_SASI", "Homes fetched: ${homeList.size}")
                onComplete(homeList)
            }
            .addOnFailureListener { exception ->
                Log.e("HOME_SASI", "Failed to fetch homes", exception)
                onComplete(emptyList())
            }
    }

    fun updateTuyaHomeId(homeId: String, tuyaHomeId: Long, onComplete: (Boolean) -> Unit) {
        db.child(homeId).child("tuyaHomeId").setValue(tuyaHomeId)
            .addOnSuccessListener {
                Log.d("FBHomeRepo", "Success update tuyaId for $homeId")
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e("FBHomeRepo", "Failed update tuyaId: ${it.message}")
                onComplete(false)
            }
    }
}