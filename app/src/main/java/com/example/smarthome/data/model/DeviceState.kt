package com.example.smarthome.data.model

enum class DeviceState {
    ON,
    OFF,
    ERROR,
    DISCONNECTED;

    val isControllable: Boolean
        get() = this == ON || this == OFF
}
