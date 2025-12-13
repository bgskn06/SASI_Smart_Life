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
        db.orderByChild("homeId").equalTo(homeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        onComplete(emptyList())
                        return
                    }

                    val roomList = snapshot.children.mapNotNull { childSnapshot ->
                        val roomData = childSnapshot.value as? MutableMap<String, Any>
                        // Pastikan ID masuk ke map agar bisa dipakai untuk update/delete
                        roomData?.set("roomId", childSnapshot.key ?: "")
                        roomData
                    }
//                    Log.d("ROOM_SASI", "Rooms fetched: ${roomList.size}")
                    onComplete(roomList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ROOM_SASI", "Error fetching rooms: ${error.message}")
                    onComplete(emptyList())
                }
            })
    }

    fun deleteRoom(roomId: String, onComplete: (Boolean, String?) -> Unit) {
        if (roomId.isBlank()) {
            onComplete(false, "Invalid Room ID")
            return
        }

        // 1. Buat referensi ke node "device"
        // Karena variabel 'db' kamu saat ini mengarah ke "/rooms",
        // kita perlu mengakses root database lalu ke child "device".
        val devicesRef = db.database.reference.child("device")

        // 2. Cek apakah ada device yang memiliki roomId ini
        devicesRef.orderByChild("roomId").equalTo(roomId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Jika snapshot.exists() dan childrenCount > 0, berarti ada device di ruangan ini
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
//                        val count = snapshot.childrenCount
//                        val message = "Gagal menghapus. Masih ada $count perangkat yang terhubung ke ruangan ini. Pindahkan atau hapus perangkat terlebih dahulu."
//                        onComplete(false, message)
                        Log.d("ROOM_SASI", "room tidak bisa didelete masih ada device")
                    } else {
                        // 3. Jika aman (tidak ada device), lakukan penghapusan Room
                        performDeleteRoom(roomId, onComplete)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gagal saat membaca node device (misal koneksi putus)
                    onComplete(false, "Error checking devices: ${error.message}")
                }
            })
    }

    // Fungsi bantuan untuk menghapus node room (private saja)
    private fun performDeleteRoom(roomId: String, onComplete: (Boolean, String?) -> Unit) {
        db.child(roomId).removeValue()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, "Failed to delete room: ${e.message}")
            }
    }
}
