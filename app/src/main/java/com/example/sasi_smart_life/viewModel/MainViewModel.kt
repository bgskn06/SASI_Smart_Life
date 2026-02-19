package com.example.sasi_smart_life.viewModel

import android.util.Log
import androidx.collection.intIntMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sasi_smart_life.data.models.*
import com.example.sasi_smart_life.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IResultCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.Result.Companion.success

class MainViewModel(
    private val userRepo: FBUserRepository,
    private val homeRepo: FBHomeRepository,
    private val roomRepo: FBRoomRepository,
    private val deviceRepo: FBDeviceRepository,
    private val sceneRepo: FBSceneRepository,
    private val categoryRepo: FBCategoryRepository,
    private val tuyaAuthRepo: TuyaAuthRepository,
) : ViewModel() {


    // -----------------------------------------
    // GLOBAL UI STATE
    // -----------------------------------------
    private val _uiState = MutableStateFlow(AppState())
    val uiState = _uiState.asStateFlow()

    val tag = "MainViewModel_SASI"

    private val auth = FirebaseAuth.getInstance()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    info = "Logged in Firebase as ${user.email}. Connecting to Tuya..."
                )

                // --- SHADOW LOGIN KE TUYA ---
                val uid = user.uid
                tuyaAuthRepo.loginOrRegisterTuya(uid) { success, error ->
                    if (success) {
//                        Log.d(tag, "Tuya Login Success!")
                        loadAllData()
                    } else {
//                        Log.e(tag, "Tuya Login Failed: $error")
                        _uiState.value = _uiState.value.copy(error = "Tuya Error: $error")
                    }
                }

            } else {
                tuyaAuthRepo.logout()
                _uiState.value = AppState(isLoggedIn = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        deviceRepo.removeDevicesListener()
        if (sceneEventListener != null) {
            dbRef.removeEventListener(sceneEventListener!!)
        }
    }


    // ----------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------
    fun logout() {
        auth.signOut()
        tuyaAuthRepo.logout()
        _uiState.value = AppState()
    }


    // ----------------------------------------------------
    // LOAD SEMUA DATA (SETELAH LOGIN)
    // ----------------------------------------------------
    fun loadAllData() {
        val uid = auth.currentUser?.uid ?: return
        loadUser(uid)
        loadHomes(uid)
    }


    // ----------------------------------------------------
    // USER
    // ----------------------------------------------------
    private fun loadUser(uid: String) {
        userRepo.getUser(uid) { user ->
            _uiState.value = _uiState.value.copy(currentUser = user)
        }
    }


    // ----------------------------------------------------
    // HOMES
    // ----------------------------------------------------
    fun selectHome(home: Home) {
        deviceRepo.removeDevicesListener()
        _uiState.value = _uiState.value.copy(
            selectedHomeId = home,
            rooms = emptyList(),
            devices = emptyList(),
            scenes = emptyList(),
            categories = emptyList()
        )

        loadHomeChildData(home.homeId)
    }

    private fun loadHomes(uid: String) {
        homeRepo.getHomesByUser(uid) { list ->
            val homes = list.map { m ->
                Home(
                    homeId = m["homeId"] as String,
                    name = m["name"] as? String ?: "",
                    ownerUid = m["ownerUid"] as? String ?: "",
                    imageUrl = m["image"] as? String ?: "",
                    tuyaHomeId = (m["tuyaHomeId"] as? Number)?.toLong() ?: 0L
                )
            }

            if (homes.isNotEmpty()) {
                val needsSelection = _uiState.value.selectedHomeId == null

                _uiState.value = _uiState.value.copy(
                    homes = homes,
                    selectedHomeId = if (needsSelection) homes.first() else _uiState.value.selectedHomeId,
                    error = null // Hapus error sebelumnya jika ada
                )

                if (needsSelection) {
                    loadHomeChildData(homes.first().homeId)
                }
                syncExistingHomesWithTuya(homes)

            } else {
                _uiState.value = _uiState.value.copy(error = "No homes found for this user.")
            }
        }
    }

    private fun syncExistingHomesWithTuya(homes: List<Home>) {
        val uid = auth.currentUser?.uid ?: return

        homes.forEach { home ->
            // Cek apakah rumah ini belum punya ID Tuya (masih 0)
            if (home.tuyaHomeId == 0L) {
//                Log.d(tag, "MIGRATION: Syncing home '${home.name}' to Tuya Cloud...")

                // --- LANGSUNG PANGGIL SDK TUYA DI SINI ---
                ThingHomeSdk.getHomeManagerInstance().createHome(
                    home.name,
                    0.0, // lon default
                    0.0, // lat default
                    home.name, // geoName
                    listOf("Default Room"), // Tuya wajib minimal 1 ruangan
                    object : IThingHomeResultCallback {
                        override fun onSuccess(bean: HomeBean?) {
                            val newTuyaId = bean?.homeId
                            if (newTuyaId != null) {
//                                Log.i(tag, "Tuya Home Created! ID: $newTuyaId. Updating Firebase...")

                                // Update ke Firebase menggunakan Repo Home yang lama
                                homeRepo.updateTuyaHomeId(home.homeId, newTuyaId) { success ->
                                    if (success) {
//                                        Log.i(tag, "SUCCESS: Home '${home.name}' is now linked (Firebase <-> Tuya)")
                                        // Refresh data UI agar ID baru termuat
                                        loadHomes(uid)
                                    }
                                }
                            }
                        }

                        override fun onError(errorCode: String?, errorMsg: String?) {
//                            Log.e(tag, "Tuya Create Home Failed: $errorCode - $errorMsg")
                        }
                    }
                )
            }
        }
    }

    // Helper simple untuk list ruangan (bisa dikosongkan jika repot)
    private fun listRoomNames(homeId: String): List<String> {
        return listOf("Default Room") // Tuya butuh minimal 1 ruangan saat create
    }

    private fun loadHomeChildData(homeId: String) {
        loadRooms(homeId)
        loadDevices(homeId)
        loadScenes(homeId)
        loadCategories(homeId)
    }


    fun createHome(name: String, imageUrl: String = "") {
        val uid = auth.currentUser?.uid ?: return

        homeRepo.createHome(
            name = name,
            ownerUid = uid,
            imageUrl = imageUrl
        ) { success, error ->

            if (success) {
                loadHomes(uid)
                _uiState.value = _uiState.value.copy(info = "Home created successfully")
            } else {
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }


    // ----------------------------------------------------
    // ROOMS
    // ----------------------------------------------------
    private fun loadRooms(homeId: String) {
        roomRepo.getRooms(homeId) { list ->
            val rooms = list.map { m ->
                val xValue = (m["x"] as? Number)?.toFloat() ?: 0f
                val yValue = (m["y"] as? Number)?.toFloat() ?: 0f
                val isMapValue = when (val status = m["isMap"]) {
                    is Boolean -> status
                    is Long -> status == 1L
                    else -> false
                }
                Room(
                    roomId = m["roomId"].toString(),
                    homeId = m["homeId"] as? String ?: "",
                    name = m["name"] as? String ?: "",
                    x = xValue,
                    y = yValue,
                    isMap = isMapValue
                )
            }

            _uiState.value = _uiState.value.copy(rooms = rooms)
        }
    }

    fun createRoom(
        homeId: String,
        name: String,
        img: String = "",
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        val homeName = _uiState.value.selectedHomeId?.name ?: "default"
        val roomId = "room_${homeName}_${System.currentTimeMillis()}"
        roomRepo.createRoom(roomId, homeId, name, img) { success, error ->
            if (success) {
                loadRooms(homeId)
            }
            onResult(success, error)
        }
    }

    fun updateRoomPosition(roomId: String, x: Float, y: Float) {
        roomRepo.updateRoomPosition(roomId, x, y) { success, error ->
            if (!success) {
                _uiState.value = _uiState.value.copy(error = "Failed to save room position: $error")
                val currentHomeId = _uiState.value.selectedHomeId?.homeId
                if (currentHomeId != null) {
                    loadRooms(currentHomeId)
                }
            }
        }
    }

    fun updateRoomIsMap(roomId: String, isMap: Boolean) {
        roomRepo.updateRoomIsMap(roomId, isMap) { success, error ->
            if (success) {
                val currentHomeId = _uiState.value.selectedHomeId?.homeId
                if (currentHomeId != null) {
                    loadRooms(currentHomeId)
                }
            } else {
                _uiState.value = _uiState.value.copy(error = "Failed to update room floor plan status: $error")
            }
        }
    }

    fun deleteRoom(roomId: String) {
        val currentHomeId = _uiState.value.selectedHomeId?.homeId ?: return

        roomRepo.deleteRoom(roomId) { success, message ->
            if (success) {
                loadRooms(currentHomeId)

                _uiState.value = _uiState.value.copy(
                    info = "Ruangan berhasil dihapus",
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = message ?: "Gagal menghapus ruangan"
                )
            }
        }
    }

    // ----------------------------------------------------
    // DEVICES
    // ----------------------------------------------------
    private fun loadDevices(homeId: String) {
        deviceRepo.getDevicesByHomeListener(homeId) { list ->
            val devices = list.map { m ->
                val devId = m["devId"] as? String ?: ""
                val isOnlineValue = when (val status = m["isOnline"]) {
                    is Boolean -> status
                    is Long -> status == 1L
                    else -> false
                }
                val isSceneValue = when (val status = m["isScene"]) {
                    is Boolean -> status
                    is Long -> status == 1L
                    else -> false
                }
                val statusValue = when (val status = m["status"]) {
                    is Boolean -> status
                    is Long -> status == 1L
                    else -> false
                }

                val scheduleData = m["schedule"] as? Map<String, Map<String, Any>>
                val schedule = scheduleData?.mapValues { entry ->
                    val data = entry.value
                    Schedule(
                        On = data["On"] as? String ?: "",
                        Off = data["Off"] as? String ?: "",
                        Status = (data["Status"] as? Long)?.toInt() ?: 0
                    )
                } ?: emptyMap()


                val nodesData = m["nodes"] as? Map<String, Any>
                val finalDeviceNodes: List<DeviceNode>

                if (nodesData != null && nodesData.isNotEmpty()) {
                    finalDeviceNodes = nodesData.values.mapNotNull { nodeValue ->
                        (nodeValue as? Map<String, Any>)?.let { nodeMap ->
                            DeviceNode(
                                id = nodeMap["id"] as? String ?: "",
                                categoryId = nodeMap["categoryId"] as? String ?: "",
                                x = (nodeMap["x"] as? Number)?.toFloat() ?: 0f,
                                y = (nodeMap["y"] as? Number)?.toFloat() ?: 0f,
                                rotation = (nodeMap["rotation"] as? Number)?.toFloat() ?: 0f,
                                mirror = nodeMap["mirror"] as? Boolean ?: false
                            )
                        }
                    }
                } else {
                    val fallbackCategoryId = m["category"] as? String ?: ""
                    val fallbackX = (m["x"] as? Number)?.toFloat() ?: 0f
                    val fallbackY = (m["y"] as? Number)?.toFloat() ?: 0f
                    finalDeviceNodes = listOf(
                        DeviceNode(
                            id = "node_1",
                            categoryId = fallbackCategoryId,
                            x = fallbackX,
                            y = fallbackY
                        )
                    )
                }

                val topLevelCategory = m["category"] as? String
                val finalCategory = topLevelCategory ?: finalDeviceNodes.firstOrNull()?.categoryId

                val isTuyaValue = when (val status = m["isTuya"]) {
                    is Boolean -> status
                    is Long -> status == 1L
                    else -> false
                }

                val tuyaInfo = m["tuyaInfo"] as? Map<String, Any>

                val finalTuyaInfo = if (tuyaInfo != null) {
                    TuyaInfo(
                        iconUrl = tuyaInfo["iconUrl"] as? String,
                        dps = (tuyaInfo["dps"] as? Map<String, Any>) ?: emptyMap(),
                        isOnline = when (val s = tuyaInfo["isOnline"]) {
                            is Boolean -> s
                            is Long -> s == 1L
                            else -> false
                        },
                        category = tuyaInfo["category"] as? String,
                        ip = tuyaInfo["ip"] as? String,
                        mac = tuyaInfo["mac"] as? String,
                        batt = (tuyaInfo["batt"]?.toString())?.toDoubleOrNull()?.toInt()
                    )
                } else {
                    null
                }

                Device(
                    devId = devId,
                    name = m["name"] as? String ?: "",
                    homeId = m["homeId"] as? String ?: "",
                    roomId = m["roomId"] as? String,
                    isOnline = isOnlineValue,
                    isScene = isSceneValue,
                    status = statusValue,
                    schedule = schedule,
                    nodes = finalDeviceNodes,
                    category = finalCategory,
                    isTuya = isTuyaValue,
                    tuyaInfo = finalTuyaInfo
                )
            }

            _uiState.value = _uiState.value.copy(devices = devices)
        }
    }

    fun setDeviceStatus(devId: String, roomId: String, newStatus: Boolean) {
        val status = if (newStatus) 1 else 0
        deviceRepo.updateDeviceStatus(devId, roomId, status) { success, error ->
            if (!success) {
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }

    fun updateDeviceNodePosition(devId: String, nodeId: String, x: Float, y: Float) {
        deviceRepo.updateDeviceNodePosition(devId, nodeId, x, y) { success, error ->
            if (!success) {
                _uiState.value = _uiState.value.copy(error = "Failed to save node position: $error")
            }
        }
    }

    fun addDevice(
        name: String,
        categoryId: String,
        homeId: String,
        roomId: String?,
        devId: String? = null,
        isTuya: Boolean = false,
        iconUrl: String? = null,
        ip: String? = null,
        mac: String? = null,
        initialDps: Map<String, Any> = emptyMap()
    ) {
        val roomName = _uiState.value.rooms.find { it.roomId == roomId }?.name ?: "default"

        val newDeviceId = if (isTuya && devId != null) {
            devId
        } else {
            val deviceCountInRoom = _uiState.value.devices.count { it.roomId == roomId }
            val nextDeviceNumber = deviceCountInRoom + 1
            val formattedDeviceNumber = String.format("%03d", nextDeviceNumber)
            "dev_${roomName}_${formattedDeviceNumber}"
        }

        val node = DeviceNode(
            id = "node_1",
            categoryId = categoryId,
            x = 0f,
            y = 0f,
            rotation = 0f,
            mirror = false
        )

        val tuyaInfo = TuyaInfo(
            iconUrl = iconUrl,
            dps = initialDps,
            isOnline = true,
            category = categoryId,
            ip = ip,
            mac = mac
        )

        val newDevice = Device(
            devId = newDeviceId,
            name = name,
            category = if(isTuya) null else categoryId,
            homeId = homeId,
            roomId = roomId,
            isOnline = true,
            isScene = false,
            status = false,
            nodes = listOf(node),
            isTuya = isTuya,
            tuyaInfo = if(isTuya) tuyaInfo else null
        )

        deviceRepo.saveDevice(newDevice) { success, error ->
            if (success) {
                // No need to call loadDevices, listener will do it
            } else {
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }

    fun updateDevice(name: String, devId: String, categoryId: String) {
        deviceRepo.updateDeviceInformation(name, devId, categoryId)
    }

    fun addNodeToDevice(devId: String, categoryId: String) {
        val device = _uiState.value.devices.find { it.devId == devId } ?: return

        val newNodeId = "node_${device.nodes.size + 1}"

        val newNode = DeviceNode(
            id = newNodeId,
            categoryId = categoryId,
            x = 0f,
            y = 0f
        )

        deviceRepo.addDeviceNode(devId, newNode) { success, error ->
            if (success) {
                // No need to call loadDevices, listener will do it
            } else {
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }

    fun updateDeviceSchedule(devId: String, newSchedule: Map<String, Any>) {
        deviceRepo.updateDeviceSchedule(devId, newSchedule) { success, error ->
            if (!success) {
                _uiState.value = _uiState.value.copy(error = "Failed to update schedule: $error")
            }
        }
    }

    fun deleteDevice(device: Device) {

        val onFirebaseComplete: (Boolean, String?) -> Unit = { success, message ->
            if (success) {
            } else {
            }
        }

        if (device.isTuya) {
            ThingHomeSdk.newDeviceInstance(device.devId)?.removeDevice(object : IResultCallback {
                    override fun onSuccess() {
//                        Log.d(tag, "Berhasil unbind Tuya: ${device.devId}")
                        deviceRepo.deleteDevice(device.devId, onFirebaseComplete)
                    }

                    override fun onError(code: String?, error: String?) {
//                        Log.e(tag, "Gagal unbind Tuya: ${device.devId} $error")
                        deviceRepo.deleteDevice(device.devId, onFirebaseComplete)
                    }
                }
            )
        } else {
            deviceRepo.deleteDevice(device.devId, onFirebaseComplete)
        }
    }

    private val _smartLockLogs = MutableStateFlow<List<SmartLockLog>>(emptyList())
    val smartLockLogs = _smartLockLogs.asStateFlow()

    fun listenToSmartLockLogs(devId: String) {
        viewModelScope.launch {
            deviceRepo.getSmartLockLogs(devId)
                .catch { e ->
//                    Log.e("MainVM", "Gagal load logs: ${e.message}")
                }
                .collect { logs ->
                    _smartLockLogs.value = logs
                }
        }
    }

    private val _userMappingState = MutableStateFlow<Map<String, String>>(emptyMap())
    val userMappingState = _userMappingState.asStateFlow()

    fun loadDeviceUsers(devId: String) {
        deviceRepo.observeUserMapping(devId) { mapping ->
            _userMappingState.value = mapping
        }
    }

    private val _doorSensorLogs = MutableStateFlow<List<DoorSensorLog>>(emptyList())
    val doorSensorLogs = _doorSensorLogs.asStateFlow()

    fun listenToDoorSensorLogs(devId: String) {
        viewModelScope.launch {
            deviceRepo.getDoorSensorLogs(devId)
                .catch { e ->
//                    Log.e("MainVM", "Gagal load logs: ${e.message}")
                }
                .collect { logs ->
                    _doorSensorLogs.value = logs
                }
        }
    }

    fun addDeviceUser(devId: String, userId: String, userName: String) {
        deviceRepo.addDeviceUser(devId, userId, userName)
    }

    fun deleteDeviceUser(devId: String, userId: String) {
        deviceRepo.deleteDeviceUser(devId, userId)
    }


    // ----------------------------------------------------
    // DEVICE CATEGORY
    // ----------------------------------------------------
    private fun loadCategories(homeId: String) {
        categoryRepo.getCategories(homeId) { list ->
            val categories = list.map { m ->
                DeviceCategory(
                    categoryId = m["categoryId"] as? String ?: "",
                    name = m["name"] as? String ?: "",
                    image = m["image"] as? String ?: "",
                    imageUrlOn = m["imageUrlOn"] as? String ?: "",
                    imageUrlOff = m["imageUrlOff"] as? String ?: "",
                    homeId = m["homeId"] as? String ?: ""
                )
            }
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }

    fun createCategory(name: String,image: String = "", imageUrlOn: String = "", imageUrlOff: String = "") {
        val homeId = _uiState.value.selectedHomeId?.homeId ?: return
        val categoryId = "cat_${System.currentTimeMillis()}" // Placeholder ID generation

        categoryRepo.createCategory(categoryId, name, homeId, image, imageUrlOn, imageUrlOff) { success, error ->
            if (success) {
                loadCategories(homeId)
            } else {
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }


    // ----------------------------------------------------
    // SCENES
    // ----------------------------------------------------
    private var sceneEventListener: ChildEventListener? = null
    private val dbRef = FirebaseDatabase.getInstance("https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("device")

    private fun loadScenes(homeId: String) {
        sceneRepo.getScenes(homeId) { list ->
            val scenes = list.map { m ->
                SmartScene(
                    sceneId = m["sceneId"].toString(),
                    homeId = homeId,
                    isActive = m["isActive"] as? Boolean ?: true,
                    name = m["name"] as? String ?: "Unknown",
                    ifData = SceneCondition(
                        devId = (m["if"] as? Map<*, *>)?.get("devId") as? String ?: "",
                        operator = (m["if"] as? Map<*, *>)?.get("operator") as? String
                            ?: "==",
                        status = ((m["if"] as? Map<*, *>)?.get("status") as? Number)?.toInt()
                            ?: 0
                    ),
                    schedule = SceneSchedule(
                        enabled = (m["schedule"] as? Map<*, *>)?.get("enabled") as? Boolean
                            ?: false,
                        startTime = (m["schedule"] as? Map<*, *>)?.get("startTime") as? String
                            ?: "00:00",
                        endTime = (m["schedule"] as? Map<*, *>)?.get("endTime") as? String
                            ?: "23:59",
                        days = (m["schedule"] as? Map<*, *>)?.get("days") as? Map<String, Boolean>
                            ?: emptyMap()
                    ),
                    thenAction = (m["then"] as? List<Map<String, Any>>)?.map {
                        SceneAction(it["devId"] as String, (it["status"] as Number).toInt())
                    } ?: emptyList()
                )
            }
            _uiState.value = _uiState.value.copy(scenes = scenes)
        }
    }

    fun addScene(rawScene: SmartScene) {
        val currentHome = _uiState.value.selectedHomeId

        if (currentHome == null) {
            _uiState.value = _uiState.value.copy(error = "No Home Selected!")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        val finalScene = rawScene.copy(
            homeId = currentHome.homeId
        )

        sceneRepo.addScene(finalScene) { success ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    info = "Scene berhasil dibuat!"
                )
                loadScenes(currentHome.homeId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Gagal menyimpan scene."
                )
            }
        }
    }

    fun deleteScene(sceneId: String) {
        val currentHomeId = _uiState.value.selectedHomeId?.homeId ?: return

        sceneRepo.deleteScene(sceneId) { success, message ->
            if (success) {
                loadScenes(currentHomeId)

                _uiState.value = _uiState.value.copy(
                    info = "Ruangan berhasil dihapus",
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = message ?: "Gagal menghapus ruangan"
                )
            }
        }
    }

    fun toggleSceneActive(sceneId: String, currentStatus: Boolean) {
        val newStatus = !currentStatus
        sceneRepo.updateSceneStatus(sceneId, newStatus) { success ->
            if (success) {
                val currentHomeId = _uiState.value.selectedHomeId?.homeId
                if (currentHomeId != null) loadScenes(currentHomeId)

            }
        }
    }
}