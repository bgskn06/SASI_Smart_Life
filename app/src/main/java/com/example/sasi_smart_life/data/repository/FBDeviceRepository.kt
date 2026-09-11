package com.example.sasi_smart_life.data.repository

import android.R
import android.util.Log
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceNode
import com.example.sasi_smart_life.data.models.DoorSensorLog
import com.example.sasi_smart_life.data.models.SmartLockLog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FBDeviceRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://sasi-smart-life-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).reference

    private val deviceListenersMap = mutableMapOf<String, ValueEventListener>()
    private var gatePositionListener: ValueEventListener? = null
    private val gateCommandListeners = mutableMapOf<String, ValueEventListener>()
    private val gateSafetyListeners = mutableMapOf<String, ValueEventListener>()
    private val gateLiveStatusListeners = mutableMapOf<String, ValueEventListener>()
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
                val deviceData = snapshot.value as? Map<String, Any>
                val roomId = deviceData?.get("roomId") as? String

                val updates = mutableMapOf<String, Any?>()

                newSchedule.forEach { (day, newValues) ->
                    if (newValues is Map<*, *>) {
                        val fields = newValues as Map<String, Any>

                        fields.forEach { (field, value) ->
                            updates["/device/$devId/schedule/$day/$field"] = value
                            if (!roomId.isNullOrBlank()) {
                                updates["/status/$roomId/schedule/$day/$devId/$field"] = value
                            }
                        }
                    }
                }

                if (updates.isNotEmpty()) {
                    db.updateChildren(updates)
                        .addOnSuccessListener { onComplete(true, null) }
                        .addOnFailureListener { e -> onComplete(false, e.message) }
                } else {
                    onComplete(true, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }
        })
    }

    fun updateDeviceStatus(devId: String, roomId: String, newStatus: Int, onComplete: (Boolean, String?) -> Unit) {
        val updates = mapOf(
            "/device/$devId/status" to newStatus,
            "/status/$roomId/device/$devId" to newStatus
        )
        db.updateChildren(updates)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun updateGateStatus(
        roomId: String,
        devId: String,
        command: Int,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (command !in 0..3) {
            onComplete(false, "Invalid gate command: $command")
            return
        }

        val updates = mapOf(
            "/status/$roomId/device/$devId" to command
        )

        db.updateChildren(updates)
            .addOnSuccessListener {
                Log.d(
                    tag,
                    "Gate status berhasil diubah: $roomId/$devId = $command"
                )
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(
                    tag,
                    "Gagal mengubah Gate status: ${e.message}",
                    e
                )
                onComplete(false, e.message)
            }
    }

    fun observeGatePositions(onUpdate: (Map<String, Pair<Float, Float>>) -> Unit) {
        val ref = db.child("gate")

        // avoid stacking duplicate listeners if this gets called more than once
        gatePositionListener?.let { ref.removeEventListener(it) }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val positions = mutableMapOf<String, Pair<Float, Float>>()

                for (child in snapshot.children) {
                    val gateId = child.key ?: continue
                    // read as Number first — Firebase may store the value as Long or Double
                    val x = (child.child("x").value as? Number)?.toFloat()
                    val y = (child.child("y").value as? Number)?.toFloat()

                    if (x != null && y != null) {
                        positions[gateId] = Pair(x, y)
                    }
                }

                onUpdate(positions)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Gagal membaca posisi Gate: ${error.message}")
            }
        }

        gatePositionListener = listener
        ref.addValueEventListener(listener)
    }

    fun removeGatePositionsListener() {
        gatePositionListener?.let { db.child("gate").removeEventListener(it) }
        gatePositionListener = null
    }

    fun observeGateStatus(
        roomId: String,
        safetyDevId: String,
        gateDevId: String,
        onUpdate: (safety: Boolean, command: Int) -> Unit
    ) {
        val roomRef = db
            .child("status")
            .child(roomId)
            .child("device")

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val safetyValue =
                    snapshot.child(safetyDevId)
                        .getValue(Int::class.java) ?: 0

                val gateValue =
                    snapshot.child(gateDevId)
                        .getValue(Int::class.java) ?: 0

                val safety = safetyValue == 1

                val command = gateValue.coerceIn(0, 3)

                onUpdate(safety, command)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    tag,
                    "Gagal membaca status Gate: ${error.message}"
                )
            }
        }

        roomRef.addValueEventListener(listener)
    }

    fun observeGateCommand(
        gateKey: String,        // use gate.gateId as the map key
        roomId: String,
        devId: String,          // primary devId to represent this gate's state (see note below)
        onUpdate: (Int) -> Unit
    ) {
        val ref = db.child("status").child(roomId).child("device").child(devId)

        gateCommandListeners[gateKey]?.let {
            db.child("status").child(roomId).child("device").child(devId).removeEventListener(it)
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val command = (snapshot.value as? Number)?.toInt() ?: 0
                onUpdate(command)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Gagal membaca command Gate: ${error.message}")
            }
        }

        gateCommandListeners[gateKey] = listener
        ref.addValueEventListener(listener)
    }

    fun observeGateSafety(
        gateKey: String,
        roomId: String,
        safetyDevId: String,
        onUpdate: (Boolean) -> Unit
    ) {
        val ref = db.child("status").child(roomId).child("device").child(safetyDevId)

        gateSafetyListeners[gateKey]?.let { ref.removeEventListener(it) }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = (snapshot.value as? Number)?.toInt() ?: 0
                onUpdate(value == 1)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Gagal membaca status Safety: ${error.message}")
            }
        }

        gateSafetyListeners[gateKey] = listener
        ref.addValueEventListener(listener)
    }

    fun removeGateSafetyListeners() {
        gateSafetyListeners.clear()
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

    fun updateGatePosition(
        gateId: String,
        x: Float,
        y: Float,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val updates = mapOf(
            "x" to x,
            "y" to y
        )

        db.child("gate")
            .child(gateId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Log.d(
                    tag,
                    "Posisi Gate berhasil disimpan: $gateId x=$x y=$y"
                )
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(
                    tag,
                    "Gagal menyimpan posisi Gate: ${e.message}",
                    e
                )
                onComplete(false, e.message)
            }
    }

    fun observeGateLiveStatus(gateId: String, onUpdate: (String, Int) -> Unit) {
        val ref = db.child("gate").child(gateId)

        gateLiveStatusListeners[gateId]?.let { ref.removeEventListener(it) }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshot.child("state").getValue(String::class.java) ?: "UNKNOWN"
                val progress = (snapshot.child("progress").value as? Number)?.toInt() ?: 0
                onUpdate(state, progress)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Gagal membaca live status Gate: ${error.message}")
            }
        }

        gateLiveStatusListeners[gateId] = listener
        ref.addValueEventListener(listener)
    }

    fun removeGateLiveStatusListeners() {
        gateLiveStatusListeners.clear()
    }

    fun getDevicesByHomeListener(homeId: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        if (deviceListenersMap.containsKey(homeId)) return

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val devices = snapshot.children.mapNotNull { childSnapshot ->
                    val deviceData = childSnapshot.value as? Map<String, Any>
                    deviceData?.toMutableMap()?.apply {
                        put("devId", childSnapshot.key ?: "")
                    }
                }

                onComplete(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, error.message)
            }
        }

        deviceListenersMap[homeId] = listener

        db.child("device")
            .orderByChild("homeId")
            .equalTo(homeId)
            .addValueEventListener(listener)
    }

    fun removeDevicesListener() {
        deviceListenersMap.forEach { (homeId, listener) ->
            db.child("device").removeEventListener(listener)
        }
        deviceListenersMap.clear()
    }

    fun deleteDevice(devId: String, onComplete: (Boolean, String?) -> Unit) {

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

    fun updateDeviceInformation(devId: String, name: String, categoryId: String){
        val updates = mapOf(
            "/device/$devId/name" to name,
            "/device/$devId/nodes/node_1/categoryId" to categoryId
        )
        db.updateChildren(updates)
    }

    fun updateDp(devId: String, updates: Map<String, Any>){
        db.child("device").child(devId)
            .updateChildren(updates)
            .addOnFailureListener { e ->
                Log.e(tag, "Gagal update DP $updates", e)
            }
            .addOnSuccessListener { Log.d(tag, "Berhasil update DP $updates") }
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
        val ref = db.child("device_users").child(devId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMapping = mutableMapOf<String, String>()

                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val name = child.value.toString()
                    newMapping[id] = name
                }

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
