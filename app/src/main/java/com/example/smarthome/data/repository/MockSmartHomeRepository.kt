package com.example.smarthome.data.repository

import com.example.smarthome.data.model.Area
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.data.model.Floor
import com.example.smarthome.data.model.Notification
import com.example.smarthome.data.model.UsageStat
import com.example.smarthome.data.model.UserProfile
import com.example.smarthome.util.NotificationHelper
import com.example.smarthome.SmartHomeApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MockSmartHomeRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://smart-home-monitor-7c214-default-rtdb.asia-southeast1.firebasedatabase.app")

    private val timerScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + Job())
    private val activeTimers = mutableMapOf<String, Job>()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val floors: StateFlow<List<Floor>> = _currentUser.flatMapLatest { user ->
        if (user == null) return@flatMapLatest flowOf(emptyList<Floor>())
        
        callbackFlow {
            val userId = auth.currentUser?.uid ?: return@callbackFlow
            val floorsRef = database.getReference("users/$userId/floors")
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val floorsList = snapshot.children.mapNotNull { it.getValue<Floor>() }
                    trySend(floorsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            
            floorsRef.addValueEventListener(listener)
            awaitClose { floorsRef.removeEventListener(listener) }
        }
    }.stateIn(
        scope = kotlinx.coroutines.GlobalScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<Notification>> = _currentUser.flatMapLatest { user ->
        if (user == null) return@flatMapLatest flowOf(emptyList<Notification>())
        
        callbackFlow {
            val userId = auth.currentUser?.uid ?: return@callbackFlow
            val ref = database.getReference("users/$userId/notifications")
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue<Notification>() }
                        .sortedByDescending { it.timestamp }
                    trySend(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }.stateIn(
        scope = kotlinx.coroutines.GlobalScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _usageStats = MutableStateFlow(emptyList<UsageStat>())
    val usageStats: StateFlow<List<UsageStat>> = _usageStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun login(email: String, password: String): Result<UserProfile> {
        _isLoading.value = true
        return try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = auth.currentUser!!
            val user = UserProfile(
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName
                    ?: firebaseUser.email!!.substringBefore("@")
            )
            _currentUser.value = user
            _isLoading.value = false
            Result.success(user)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, displayName: String): Result<UserProfile> {
        _isLoading.value = true
        return try {
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = auth.currentUser!!
            val user = UserProfile(
                email = firebaseUser.email ?: "",
                displayName = displayName.ifBlank { firebaseUser.email!!.substringBefore("@") }
            )
            _currentUser.value = user
            _isLoading.value = false
            Result.success(user)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    fun getFloor(floorId: String): Floor? = floors.value.find { it.id == floorId }

    fun getDevice(deviceId: String): Device? {
        return floors.value
            .flatMap { it.areas }
            .flatMap { it.devices }
            .find { it.id == deviceId }
    }

    fun addFloor(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val userId = auth.currentUser?.uid ?: return
        
        val floorsRef = database.getReference("users/$userId/floors")
        val newFloorId = floorsRef.push().key ?: "floor_${System.currentTimeMillis()}"
        
        val newFloor = Floor(
            id = newFloorId,
            name = trimmed,
            devices = emptyList()
        )
        
        floorsRef.child(newFloorId).setValue(newFloor)
            .addOnSuccessListener {
                android.util.Log.d("MockSmartHomeRepository", "Floor added successfully to Firebase")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MockSmartHomeRepository", "Failed to add floor to Firebase", e)
            }
    }

    fun editFloor(floorId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val userId = auth.currentUser?.uid ?: return
        
        database.getReference("users/$userId/floors/$floorId/name").setValue(trimmed)
    }

    fun deleteFloor(floorId: String) {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("users/$userId/floors/$floorId").removeValue()
    }

    fun addArea(floorId: String, name: String, type: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val userId = auth.currentUser?.uid ?: return
        
        val floor = floors.value.find { it.id == floorId } ?: return
        val updatedAreas = floor.areas + Area(
            id = "area_${System.currentTimeMillis()}",
            name = trimmed,
            floorId = floorId,
            type = type
        )
        
        val updatedFloor = floor.copy(areas = updatedAreas)
        database.getReference("users/$userId/floors/$floorId").setValue(updatedFloor)
    }

    fun editArea(floorId: String, areaId: String, newName: String, newType: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val userId = auth.currentUser?.uid ?: return
        
        val floor = floors.value.find { it.id == floorId } ?: return
        val updatedAreas = floor.areas.map { 
            if (it.id == areaId) it.copy(name = trimmed, type = newType) else it
        }
        
        val updatedFloor = floor.copy(areas = updatedAreas)
        database.getReference("users/$userId/floors/$floorId").setValue(updatedFloor)
    }

    fun deleteArea(floorId: String, areaId: String) {
        val userId = auth.currentUser?.uid ?: return
        val floor = floors.value.find { it.id == floorId } ?: return
        val updatedAreas = floor.areas.filter { it.id != areaId }
        
        val updatedFloor = floor.copy(areas = updatedAreas)
        database.getReference("users/$userId/floors/$floorId").setValue(updatedFloor)
    }

    fun deleteDevice(floorId: String, areaId: String, deviceId: String) {
        val userId = auth.currentUser?.uid ?: return
        val floor = floors.value.find { it.id == floorId } ?: return
        
        val updatedAreas = floor.areas.map { area ->
            if (area.id == areaId) {
                area.copy(devices = area.devices.filter { it.id != deviceId })
            } else {
                area
            }
        }
        
        val updatedFloor = floor.copy(areas = updatedAreas)
        database.getReference("users/$userId/floors/$floorId").setValue(updatedFloor)
    }

    fun addNotification(title: String, message: String, type: String = "INFO") {
        val userId = auth.currentUser?.uid ?: return
        val ref = database.getReference("users/$userId/notifications")
        val id = ref.push().key ?: "notif_${System.currentTimeMillis()}"
        val notification = Notification(id = id, title = title, message = message, type = type)
        ref.child(id).setValue(notification)
    }

    fun clearNotifications() {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("users/$userId/notifications").removeValue()
    }

    fun addDeviceToArea(floorId: String, areaId: String, device: Device) {
        val userId = auth.currentUser?.uid ?: return
        val floor = floors.value.find { it.id == floorId } ?: return
        
        val updatedAreas = floor.areas.map { 
            if (it.id == areaId) {
                it.copy(devices = it.devices + device)
            } else {
                it
            }
        }
        
        val updatedFloor = floor.copy(areas = updatedAreas)
        database.getReference("users/$userId/floors/$floorId").setValue(updatedFloor)
    }

    fun editDevice(floorId: String, areaId: String, deviceId: String, newName: String, newMaxDuration: Int) {
        val userId = auth.currentUser?.uid ?: return
        val floor = floors.value.find { it.id == floorId } ?: return
        
        val updatedAreas = floor.areas.map { area ->
            if (area.id == areaId) {
                area.copy(devices = area.devices.map { device ->
                    if (device.id == deviceId) {
                        device.copy(name = newName, maxDurationMinutes = newMaxDuration)
                    } else {
                        device
                    }
                })
            } else {
                area
            }
        }
        
        val updatedFloor = floor.copy(areas = updatedAreas)
        database.getReference("users/$userId/floors/$floorId").setValue(updatedFloor)
    }

    fun toggleOutlet(deviceId: String) {
        updateDevice(deviceId) { device ->
            if (!device.state.isControllable) return@updateDevice device
            val newState = if (device.state == DeviceState.ON) DeviceState.OFF else DeviceState.ON
            device.copy(state = newState)
        }
    }

    fun toggleSwitch(deviceId: String, switchId: String) {
        updateDevice(deviceId) { device ->
            if (!device.state.isControllable) return@updateDevice device
            val updatedSwitches = device.switches.map { switch ->
                if (switch.id == switchId) switch.copy(isOn = !switch.isOn) else switch
            }
            val anyOn = updatedSwitches.any { it.isOn }
            device.copy(
                switches = updatedSwitches,
                state = if (anyOn) DeviceState.ON else DeviceState.OFF
            )
        }
    }

    fun toggleScheduledDevice(deviceId: String) {
        updateDevice(deviceId) { device ->
            if (!device.state.isControllable) return@updateDevice device
            val newState = if (device.state == DeviceState.ON) DeviceState.OFF else DeviceState.ON
            device.copy(state = newState)
        }
    }

    fun updateScheduledDevice(
        deviceId: String,
        maxDurationMinutes: Int,
        onTime: String?,
        offTime: String?
    ) {
        updateDevice(deviceId) { device ->
            device.copy(
                maxDurationMinutes = maxDurationMinutes.coerceIn(1, 480),
                onTime = onTime,
                offTime = offTime
            )
        }
    }

    fun toggleCameraStream(deviceId: String) {
        updateDevice(deviceId) { device ->
            if (device.state == DeviceState.DISCONNECTED) return@updateDevice device
            device.copy(isStreaming = !device.isStreaming)
        }
    }

    private fun updateDevice(
        deviceId: String,
        transform: (Device) -> Device
    ) {
        val userId = auth.currentUser?.uid ?: return
        val currentFloors = floors.value
        
        var floorToUpdate: Floor? = null
        var deviceAfterTransform: Device? = null
        
        currentFloors.forEach { floor ->
            val updatedAreas = floor.areas.map { area ->
                val updatedDevices = area.devices.map { device ->
                    if (device.id == deviceId) {
                        val newDevice = transform(device)
                        deviceAfterTransform = newDevice
                        newDevice
                    } else {
                        device
                    }
                }
                if (updatedDevices != area.devices) {
                    area.copy(devices = updatedDevices)
                } else {
                    area
                }
            }
            
            if (updatedAreas != floor.areas) {
                floorToUpdate = floor.copy(areas = updatedAreas)
            }
        }
        
        floorToUpdate?.let {
            database.getReference("users/$userId/floors/${it.id}").setValue(it)
        }

        // Handle auto-off timer logic
        deviceAfterTransform?.let { device ->
            activeTimers[device.id]?.cancel()
            if (device.state == DeviceState.ON && device.maxDurationMinutes > 0) {
                val job = timerScope.launch {
                    delay(device.maxDurationMinutes * 60 * 1000L)
                    // Auto-off the device after delay
                    updateDevice(device.id) { it.copy(state = DeviceState.OFF) }
                    
                    // Send notification if it's an Iron
                    if (device.type == com.example.smarthome.data.model.DeviceType.SCHEDULED_DEVICE && 
                        device.deviceKind == com.example.smarthome.data.model.ScheduledKind.IRON) {
                        NotificationHelper.sendSafetyNotification(SmartHomeApp.instance, device.name)
                        addNotification(
                            title = "Safety Cutoff",
                            message = "${device.name} has been automatically turned off for safety.",
                            type = "SAFETY"
                        )
                    }
                }
                activeTimers[device.id] = job
            }
        }
    }

    companion object {
        val instance = MockSmartHomeRepository()
    }
}
