package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class FBHomeRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference.child("homes")

    fun createHome(
        name: String,
        ownerUid: String,
        imageUrl: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        val homeId = "home_${System.currentTimeMillis()}"
        val homeRef = db.child(homeId)

        val data = mapOf(
            "name" to name,
            "image" to imageUrl,
            "ownerUid" to ownerUid
        )

        homeRef.setValue(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun getHomesByUser(uid: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        // ================== DIAGNOSTIC CODE ==================
        // Mengambil seluruh node /homes untuk debugging
        db.get()
            .addOnSuccessListener { snapshot ->
                Log.d("HOME_SASI", "Snapshot received. Exists: ${snapshot.exists()}. Children count: ${snapshot.childrenCount}")

                if (!snapshot.exists()) {
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                // Filter manual di sisi aplikasi
                val list = snapshot.children.mapNotNull { childSnapshot ->
                    val homeData = childSnapshot.value as? MutableMap<String, Any>
                    homeData?.set("homeId", childSnapshot.key ?: "")
                    homeData
                }.filter { home ->
                    home["ownerUid"] == uid
                }
                
                Log.d("HOME_SASI", "Filtered list count: ${list.size}")
                onComplete(list)
            }
            .addOnFailureListener { exception ->
                Log.e("HOME_SASI", "Failed to read homes node", exception)
                onComplete(emptyList())
            }
        // ================== END DIAGNOSTIC ==================
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