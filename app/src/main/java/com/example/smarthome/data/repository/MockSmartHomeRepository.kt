package com.example.smarthome.data.repository

import com.example.smarthome.data.model.Area
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.data.model.Floor
import com.example.smarthome.data.model.UsageStat
import com.example.smarthome.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

class MockSmartHomeRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://smart-home-monitor-7c214-default-rtdb.asia-southeast1.firebasedatabase.app")

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
        
        currentFloors.forEach { floor ->
            val updatedAreas = floor.areas.map { area ->
                val updatedDevices = area.devices.map { device ->
                    if (device.id == deviceId) {
                        transform(device)
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
    }

    companion object {
        val instance = MockSmartHomeRepository()
    }
}
