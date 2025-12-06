package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FBRoomRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference.child("rooms")

    fun createRoom(
        roomId: String,
        homeId: String,
        name: String,
        imageUrl: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.orderByChild("homeId").equalTo(homeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var nameExists = false
                for (roomSnapshot in snapshot.children) {
                    val existingName = roomSnapshot.child("name").getValue(String::class.java)
                    if (existingName?.equals(name, ignoreCase = true) == true) {
                        nameExists = true
                        break
                    }
                }

                if (nameExists) {
                    onComplete(false, "A room with the name '$name' already exists.")
                } else {
                    val data = mapOf(
                        "homeId" to homeId,
                        "name" to name,
                        "image" to imageUrl,
                        "isMap" to false,
                        "x" to 0f,
                        "y" to 0f
                    )

                    db.child(roomId)
                        .setValue(data)
                        .addOnSuccessListener { onComplete(true, null) }
                        .addOnFailureListener { e -> onComplete(false, e.message) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }
        })
    }

    fun updateRoomPosition(roomId: String, x: Float, y: Float, onComplete: (Boolean, String?) -> Unit) {
        val positionUpdates = mapOf(
            "x" to x,
            "y" to y
        )
        db.child(roomId).updateChildren(positionUpdates)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun updateRoomIsMap(roomId: String, isMap: Boolean, onComplete: (Boolean, String?) -> Unit) {
        val isMapUpdate = mapOf("isMap" to isMap)
        db.child(roomId).updateChildren(isMapUpdate)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun getRooms(homeId: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        // ================== DIAGNOSTIC CODE ==================
        // Mengambil seluruh node /rooms untuk debugging
        db.get()
            .addOnSuccessListener { snapshot ->
                Log.d("ROOM_SASI", "Snapshot received. Exists: ${snapshot.exists()}. Children count: ${snapshot.childrenCount}")

                if (!snapshot.exists()) {
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                // Filter manual di sisi aplikasi
                val allRooms = snapshot.children.mapNotNull { childSnapshot ->
                    val roomData = childSnapshot.value as? MutableMap<String, Any>
                    roomData?.set("roomId", childSnapshot.key ?: "")
                    roomData
                }

                Log.d("ROOM_SASI", "Total rooms fetched: ${allRooms.size}. Now filtering for homeId: $homeId")

                val filteredList = allRooms.filter { room ->
                    val idFromData = room["homeId"] as? String
                    // Log perbandingan untuk setiap item
                    // Log.d("ROOM_SASI", "Comparing db.homeId:'${idFromData}' with active.homeId:'${homeId}'")
                    idFromData == homeId
                }

                Log.d("ROOM_SASI", "Filtered list count: ${filteredList.size}")
                onComplete(filteredList)
            }
            .addOnFailureListener { exception ->
                Log.e("ROOM_SASI", "Failed to read rooms node", exception)
                onComplete(emptyList())
            }
        // ================== END DIAGNOSTIC ==================
    }
}
