package com.tplus.tesla.vehicle

data class VehicleData(
    val connection: ConnectionData = ConnectionData(),
    val vehicleState: VehicleStateData = VehicleStateData(),
    val speedKmh: Double? = null,
    val speedLimitKmh: Double? = null,
    val batterySoc: Double? = null,
    val rangeKm: Double? = null,
    val powerKw: Double? = null,
    val tpms: TpmsData = TpmsData(),
    val temperatures: TemperatureData = TemperatureData(),
    val energy: EnergyData = EnergyData(),
    val trip: TripData = TripData(),
    val charging: ChargingData = ChargingData(),
    val timestampMs: Long = System.currentTimeMillis()
)

data class ConnectionData(
    val connected: Boolean = false,
    val adapterName: String? = null,
    val adapterAddress: String? = null
)

data class VehicleStateData(
    val gear: Gear = Gear.UNKNOWN,
    val ready: Boolean = false,
    val driving: Boolean = false,
    val charging: Boolean = false,
    val parked: Boolean = false,
    val vehicleOn: Boolean = false
)

enum class Gear { P, R, N, D, UNKNOWN }

data class TpmsData(
    val frontLeftBar: Double? = null,
    val frontRightBar: Double? = null,
    val rearLeftBar: Double? = null,
    val rearRightBar: Double? = null
)

data class TemperatureData(
    val outsideC: Double? = null,
    val batteryC: Double? = null,
    val motorC: Double? = null,
    val inverterC: Double? = null,
    val cabinC: Double? = null
)

data class EnergyData(
    val instantaneousWhPerKm: Double? = null,
    val averageWhPerKm: Double? = null,
    val tripConsumptionKwh: Double? = null,
    val energyUsedKwh: Double? = null,
    val energyRecoveredKwh: Double? = null
)

data class TripData(
    val distanceKm: Double? = null,
    val energyKwh: Double? = null,
    val averageWhPerKm: Double? = null,
    val durationSeconds: Long? = null
)

data class ChargingData(
    val active: Boolean = false,
    val powerKw: Double? = null,
    val currentA: Double? = null,
    val voltageV: Double? = null,
    val soc: Double? = null,
    val timeRemainingSeconds: Long? = null
)
