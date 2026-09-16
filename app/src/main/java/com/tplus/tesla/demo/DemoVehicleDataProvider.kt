package com.tplus.tesla.demo

import com.tplus.tesla.vehicle.*

class DemoVehicleDataProvider {
    fun current(): VehicleData = VehicleData(
        connection = ConnectionData(
            connected = true,
            adapterName = "DEMO",
            adapterAddress = null
        ),
        vehicleState = VehicleStateData(
            gear = Gear.D,
            ready = true,
            driving = true,
            vehicleOn = true
        ),
        speedKmh = 92.0,
        speedLimitKmh = 110.0,
        batterySoc = 78.0,
        rangeKm = 342.0,
        powerKw = 85.0
    )
}
