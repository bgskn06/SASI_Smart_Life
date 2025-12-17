package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceNode
import com.example.sasi_smart_life.data.models.DoorSensorLog
import com.example.sasi_smart_life.data.models.Schedule
import com.example.sasi_smart_life.data.models.SmartLockLog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class FBDeviceRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference

    private var devicesListener: ValueEventListener? = null

    val tag = "DEVICE_SASI"

    fun saveDevice(device: Device, onComplete: (Boolean, String?) -> Unit) {
        val scheduleForFirebase = device.schedule?.mapValues { entry ->
            mapOf(
                "On" to entry.value.On,
                "Off" to entry.value.Off,
                "Status" to entry.value.Status
            )
        }

        val tuyaInfoMap = if (device.tuyaInfo != null) {
            mapOf(
                "iconUrl" to device.tuyaInfo.iconUrl,
                "dps" to device.tuyaInfo.dps,
                "isOnline" to device.tuyaInfo.isOnline,
                "category" to device.tuyaInfo.category,
                "ip" to device.tuyaInfo.ip,
                "mac" to device.tuyaInfo.mac
            )
        } else null

        val deviceData = mapOf(
            "devId" to device.devId,
            "name" to device.name,
            "category" to device.category,
            "homeId" to device.homeId,
            "roomId" to device.roomId,
            "isOnline" to if (device.isOnline) 1 else 0,
            "isScene" to if (device.isScene) 1 else 0,
            "status" to if (device.status) 1 else 0,
            "schedule" to scheduleForFirebase,
            "nodes" to device.nodes.associateBy { it.id },
            "isTuya" to device.isTuya,
            "tuyaInfo" to tuyaInfoMap
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

    fun deleteDevice(devId: String, onComplete: (Boolean, String?) -> Unit) {

        // Path harus sama persis dengan saveDevice: db.child("device").child(devId)
        db.child("device").child(devId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(tag, "Device $devId berhasil dihapus")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Gagal hapus device $devId", e)
                onComplete(false, e.message)
            }
    }

    fun updateDp(devId: String, updates: Map<String, Any>){
        db.child("device").child(devId)
            .updateChildren(updates)
            .addOnFailureListener { e ->
                Log.e(tag, "Gagal update DP $updates", e)
            }
    }

    fun updateOnline(devId: String, status: Boolean) {
        val statusValue = if (status) 1 else 0

        val updates = mapOf(
            "/device/$devId/isOnline" to statusValue,
        )
        db.updateChildren(updates)
            .addOnFailureListener { e ->
                Log.e(tag, "Gagal update Status", e)
            }

    }

    fun addLogHistory(devId: String, logData: Map<String, Any>, timestampId: Long) {

        val logId = timestampId.toString()

        db.child("device").child(devId).child("logs")
            .child(logId)
            .setValue(logData)
            .addOnFailureListener {
                Log.e(tag, "Gagal simpan log", it)
            }
    }

    fun getSmartLockLogs(devId: String): Flow<List<SmartLockLog>> = callbackFlow {

        // Reference ke node logs
        val logsRef = db.child("device").child(devId).child("logs").limitToLast(50)

        // Buat Listener Firebase
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logsList = mutableListOf<SmartLockLog>()

                for (child in snapshot.children) {
                    try {
                        val log = SmartLockLog(
                            id = child.key ?: "",
                            description = child.child("description").getValue(String::class.java) ?: "",
                            time = child.child("time").getValue(String::class.java) ?: "-",
                            status = child.child("status").getValue(String::class.java) ?: "",
                            method = child.child("method").getValue(String::class.java) ?: "UNKNOWN"
                        )
                        logsList.add(log)
                    } catch (e: Exception) {
                        Log.e(tag, "Error parsing log: ${e.message}")
                    }
                }

                trySend(logsList.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        logsRef.addValueEventListener(listener)

        awaitClose {
            logsRef.removeEventListener(listener)
            Log.d(tag, "Listener Log dilepas untuk $devId")
        }
    }

    fun getDoorSensorLogs(devId: String): Flow<List<DoorSensorLog>> = callbackFlow {

        val logsRef = db.child("device").child(devId).child("logs").limitToLast(50)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logsList = mutableListOf<DoorSensorLog>()

                for (child in snapshot.children) {
                    try {
                        val log = DoorSensorLog(
                            id = child.key ?: "",
                            description = child.child("description").getValue(String::class.java) ?: "",
                            time = child.child("time").getValue(String::class.java) ?: "-",
                            status = child.child("status").getValue(String::class.java) ?: "",
                        )
                        logsList.add(log)
                    } catch (e: Exception) {
                        Log.e(tag, "Error parsing log: ${e.message}")
                    }
                }

                trySend(logsList.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        logsRef.addValueEventListener(listener)

        awaitClose {
            logsRef.removeEventListener(listener)
            Log.d(tag, "Listener Log dilepas untuk $devId")
        }
    }

    fun observeUserMapping(devId: String, onUpdate: (Map<String, String>) -> Unit) {
        // Arahkan ke node "device_users" -> "devId"
        val ref = db.child("device_users").child(devId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMapping = mutableMapOf<String, String>()

                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val name = child.value.toString()
                    newMapping[id] = name
                }

//                Log.d(tag, "User Mapping Updated untuk $devId: $newMapping")
                onUpdate(newMapping)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Gagal load user mapping: ${error.message}")
            }
        })
    }

    fun addDeviceUser(devId: String, userId: String, userName: String){
        db.child("device_users")
            .child(devId)
            .child(userId)
            .setValue(userName)
            .addOnSuccessListener { Log.d(tag, "User $userId berhasil disimpan") }

    }

    fun deleteDeviceUser(devId: String, userId: String) {
        db.child("device_users")
            .child(devId)
            .child(userId)
            .removeValue()
            .addOnSuccessListener { Log.d(tag, "User $userId berhasil dihapus") }
            .addOnFailureListener { Log.e(tag, "Gagal hapus user: ${it.message}") }
    }
}
