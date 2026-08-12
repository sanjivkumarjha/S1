package com.example.domain.vehicle

enum class VehicleType(val displayName: String, val emoji: String) {
    CAR("Car", "🚗"),
    SUV("SUV", "🚙"),
    MOTORCYCLE("Motorcycle", "🏍️"),
    E_MOTORCYCLE("Electric Motorcycle", "⚡🏍️"),
    SCOOTER("Scooter", "🛵"),
    E_SCOOTER("Electric Scooter", "⚡🛵"),
    BICYCLE("Bicycle", "🚲"),
    E_BICYCLE("Electric Bicycle", "⚡🚲"),
    VAN("Van", "🚐"),
    TRUCK("Truck", "🚚"),
    OTHER("Smart Vehicle", "🚘")
}

enum class FuelType(val displayName: String) {
    PETROL("Petrol"),
    DIESEL("Diesel"),
    CNG("CNG"),
    LPG("LPG"),
    ELECTRIC("Electric ⚡"),
    HYBRID("Hybrid 🔋⛽"),
    PHEV("Plug-in Hybrid 🔌")
}

enum class ConnectionStatus(val label: String, val badge: String, val colorHex: Long) {
    CONNECTED("Connected", "🟢 Connected", 0xFF10B981),
    CONNECTING("Connecting...", "🟡 Connecting", 0xFFF59E0B),
    SYNCING("Syncing...", "🔵 Syncing", 0xFF3B82F6),
    LIMITED_DATA("Limited Data", "🟠 Limited Data", 0xFFF97316),
    DISCONNECTED("Disconnected", "🔴 Disconnected", 0xFFEF4444),
    OFFLINE("Offline", "⚫ Offline", 0xFF6B7280)
}

enum class ChargingStatus(val label: String) {
    NOT_CHARGING("Not Charging"),
    CHARGING("Charging ⚡"),
    CHARGED("Fully Charged 🔋✓"),
    ERROR("Charging Error ⚠️")
}

data class SmartVehicleEntity(
    val id: String,
    val name: String,
    val manufacturer: String,
    val model: String,
    val variant: String = "",
    val vehicleType: VehicleType = VehicleType.CAR,
    val fuelType: FuelType = FuelType.ELECTRIC,
    val modelYear: Int = 2025,
    val vin: String = "",
    val connectionType: String = "Bluetooth / BLE",
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isTrusted: Boolean = true,
    val isPrimary: Boolean = false,
    val batteryPercent: Int = 82,
    val fuelPercent: Int = 0,
    val estimatedRangeKm: Int = 340,
    val chargingStatus: ChargingStatus = ChargingStatus.NOT_CHARGING,
    val chargingPowerKw: Float = 0f,
    val chargingTimeRemainingMinutes: Int = 0,
    val odometerKm: Int = 12450,
    val tirePressurePsi: List<Float> = listOf(34f, 34f, 34f, 34f),
    val engineStatus: String = "Off",
    val climateStatus: String = "Off",
    val isLocked: Boolean = true,
    val isTrunkOpen: Boolean = false,
    val isLightsOn: Boolean = false,
    val isHornActive: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val dataSource: String = "Vehicle Live Data",
    val supportedCapabilities: List<String> = listOf("LOCK", "UNLOCK", "CLIMATE", "CHARGING", "LOCATION", "LIGHTS", "HORN", "TRUNK")
)
