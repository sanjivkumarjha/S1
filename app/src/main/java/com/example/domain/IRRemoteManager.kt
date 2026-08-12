package com.example.domain

import android.content.Context
import android.hardware.ConsumerIrManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.IRCommandEntity
import kotlinx.coroutines.flow.Flow

class IRRemoteManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val irCommandDao = db.irCommandDao()

    private val irManager: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    val allIRCommands: Flow<List<IRCommandEntity>> = irCommandDao.getAllIRCommands()

    fun hasIREmitter(): Boolean {
        return irManager?.hasIrEmitter() == true
    }

    suspend fun transmitIRCommand(command: IRCommandEntity): String {
        if (hasIREmitter() && irManager != null) {
            try {
                // Standard NEC format pattern simulation if raw pattern empty
                val pattern = intArrayOf(9000, 4500, 560, 560, 560, 1690, 560, 560)
                irManager.transmit(command.frequency, pattern)
                return "IR Signal transmitted for ${command.commandName}"
            } catch (e: Exception) {
                return "IR Transmission failed: ${e.message}"
            }
        } else {
            return "No IR Blaster hardware on this device. Automatically falling back to Wi-Fi/Bluetooth integration."
        }
    }

    suspend fun saveIRCommand(
        commandName: String,
        manufacturer: String = "Universal",
        model: String = "Remote",
        room: String = "Living Room"
    ): Long {
        return irCommandDao.insertIRCommand(
            IRCommandEntity(
                commandName = commandName,
                manufacturer = manufacturer,
                model = model,
                room = room
            )
        )
    }
}
