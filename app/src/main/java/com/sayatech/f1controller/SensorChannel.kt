package com.sayatech.f1controller

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.round

class SensorChannel(
    private val socketHandler: SocketHandler
): SensorEventListener {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val events = Channel<SensorEvent>()

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { offer(it) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun offer(event: SensorEvent) = runBlocking { events.send(event) }

    fun process(buttonValueCallback: () -> Float) = scope.launch {
        events.consumeEach {
            val rotationMatrix = FloatArray(16)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
            val remappedRotationMatrix = FloatArray(16)
            SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remappedRotationMatrix
            )
            val orientations = FloatArray(3)
            SensorManager.getOrientation(remappedRotationMatrix, orientations)
            val buttonValue = buttonValueCallback()
            socketHandler.orientationChange(
                round((
                    orientations[2] + ORIENTATION_BIAS) * 1000.0) / 1000.0,
                buttonValue
            )
        }
    }
}