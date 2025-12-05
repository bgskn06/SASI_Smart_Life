package com.example.sasi_smart_life.data.repository

import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceNode
import com.example.sasi_smart_life.data.models.Schedule
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class FBDeviceRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference

    private var devicesListener: ValueEventListener? = null

    suspend fun getAllDevices(): List<Device> {
        val snapshot = db.child("device").get().await()
        if (!snapshot.exists()) {
            return emptyList()
        }
        // Deserialize the data from Firebase manually
        return snapshot.children.mapNotNull { childSnapshot ->
            val deviceData = childSnapshot.value as? Map<String, @JvmSuppressWildcards Any>
            deviceData?.let {
                val devId = childSnapshot.key ?: ""

                val scheduleMap = it["schedule"] as? Map<String, Map<String, Any>>
                val schedule = scheduleMap?.mapValues { entry ->
                    val data = entry.value
                    Schedule(
                        On = data["On"] as? String ?: "",
                        Off = data["Off"] as? String ?: "",
                        Status = (data["Status"] as? Long)?.toInt() ?: 0
                    )
                }

                val nodesMap = it["nodes"] as? Map<String, Map<String, Any>>
                val nodes = nodesMap?.values?.mapNotNull { nodeData ->
                    DeviceNode(
                        id = nodeData["id"] as? String ?: "",
                        categoryId = nodeData["categoryId"] as? String ?: "",
                        x = (nodeData["x"] as? Number)?.toFloat() ?: 0f,
                        y = (nodeData["y"] as? Number)?.toFloat() ?: 0f,
                        rotation = (nodeData["rotation"] as? Number)?.toFloat() ?: 0f
                    )
                } ?: emptyList()

                Device(
                    devId = devId,
                    name = it["name"] as? String ?: "",
                    category = it["category"] as? String ?: "",
                    homeId = it["homeId"] as? String ?: "",
                    roomId = it["roomId"] as? String,
                    isOnline = (it["isOnline"] as? Long)?.toInt() == 1,
                    isScene = (it["isScene"] as? Long)?.toInt() == 1,
                    status = (it["status"] as? Long)?.toInt() == 1,
                    schedule = schedule ?: emptyMap(),
                    nodes = nodes
                )
            }
        }
    }

    fun saveDevice(device: Device, onComplete: (Boolean, String?) -> Unit) {
        val scheduleForFirebase = device.schedule?.mapValues { entry ->
            mapOf(
                "On" to entry.value.On,
                "Off" to entry.value.Off,
                "Status" to entry.value.Status
            )
        }

        val deviceData = mapOf(
            "devId" to device.devId,
            "name" to device.name,
            "category" to device.category, // Denormalized category
            "homeId" to device.homeId,
            "roomId" to device.roomId,
            "isOnline" to if (device.isOnline) 1 else 0,
            "isScene" to if (device.isScene) 1 else 0,
            "status" to if (device.status) 1 else 0,
            "schedule" to scheduleForFirebase,
            "nodes" to device.nodes.associateBy { it.id } // Save nodes as a map
        )

        db.child("device").child(device.devId)
            .setValue(deviceData)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun addDeviceNode(devId: String, node: DeviceNode, onComplete: (Boolean, String?) -> Unit) {
        db.child("device").child(devId).child("nodes").child(node.id)
            .setValue(node)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun updateDeviceSchedule(devId: String, newSchedule: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
        db.child("device").child(devId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deviceData = snapshot.value as? Map<String, @JvmSuppressWildcards Any>
                val roomId = deviceData?.get("roomId") as? String
                val existingSchedule = (deviceData?.get("schedule") as? Map<String, Map<String, Any>>) ?: emptyMap()

                val updates = mutableMapOf<String, Any?>()

                newSchedule.forEach { (day, newValues) ->
                    if (newValues is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        val fieldsToUpdate = newValues as Map<String, Any>

                        fieldsToUpdate.forEach { (field, value) ->
                            updates["/device/$devId/schedule/$day/$field"] = value
                        }

                        // Update denormalized location (full map for the day)
                        if (!roomId.isNullOrBlank()) {
                            val daySchedule = existingSchedule[day]?.toMutableMap() ?: mutableMapOf()
                            daySchedule.putAll(fieldsToUpdate)
                            updates["/status/$roomId/schedule/$day/$devId"] = daySchedule
                        }
                    }
                }

                if (updates.isNotEmpty()) {
                    db.updateChildren(updates)
                        .addOnSuccessListener { onComplete(true, null) }
                        .addOnFailureListener { e -> onComplete(false, e.message) }
                } else {
                    onComplete(true, null) // Nothing to update
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }
        })
    }

    fun updateDeviceStatus(devId: String, roomId: String, newStatus: Boolean, onComplete: (Boolean, String?) -> Unit) {
        val statusValue = if (newStatus) 1 else 0
        val updates = mapOf(
            "/device/$devId/status" to statusValue,
            "/status/$roomId/device/$devId" to statusValue
        )
        db.updateChildren(updates)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    suspend fun updateDeviceStatusSuspend(devId: String, roomId: String, newStatus: Boolean): Boolean =
        suspendCancellableCoroutine { continuation ->
            val statusValue = if (newStatus) 1 else 0
            val updates = mapOf(
                "/device/$devId/status" to statusValue,
                "/status/$roomId/$devId" to statusValue
            )
            db.updateChildren(updates)
                .addOnSuccessListener { continuation.resume(true) }
                .addOnFailureListener { continuation.resume(false) }
        }

    fun updateDeviceNodePosition(devId: String, nodeId: String, x: Float, y: Float, onComplete: (Boolean, String?) -> Unit) {
        val positionUpdates = mapOf(
            "x" to x,
            "y" to y
        )
        db.child("device").child(devId).child("nodes").child(nodeId).updateChildren(positionUpdates)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun getDevicesByHomeListener(homeId: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        devicesListener = db.child("device").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onComplete(emptyList())
                    return
                }

                val allDevices = snapshot.children.mapNotNull { childSnapshot ->
                    val deviceData = childSnapshot.value as? MutableMap<String, Any>
                    deviceData?.set("devId", childSnapshot.key ?: "")
                    deviceData
                }

                val filteredList = allDevices.filter { device ->
                    val idFromData = device["homeId"] as? String
                    idFromData == homeId
                }

                onComplete(filteredList)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(emptyList())
            }
        })
    }

    fun removeDevicesListener() {
        devicesListener?.let {
            db.child("device").removeEventListener(it)
        }
    }
}
