package com.cs407.lab09

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BallViewModel : ViewModel() {

    private var ball: Ball? = null
    private var lastTimestamp: Long = 0L
    private var lastXAcc: Float = 0f
    private var lastYAcc: Float = 0f

    // Expose the ball's position as a StateFlow
    private val _ballPosition = MutableStateFlow(Offset.Zero)
    val ballPosition: StateFlow<Offset> = _ballPosition.asStateFlow()

    /**
     * Called by the UI when the game field's size is known.
     */
    fun initBall(fieldWidth: Float, fieldHeight: Float, ballSizePx: Float) {
        if (ball == null) {
            ball = Ball(fieldWidth, fieldHeight, ballSizePx)

            _ballPosition.value = Offset(ball!!.posX, ball!!.posY)
        }
    }

    /**
     * Called by the SensorEventListener in the UI.
     */
    fun onSensorDataChanged(event: SensorEvent) {
        // Ensure ball is initialized
        val currentBall = ball ?: return

        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            if (lastTimestamp != 0L) {
                // Calculate the time difference (dT) in seconds
                val NS2S = 1.0f / 1000000000.0f
                val dT = (event.timestamp - lastTimestamp) * NS2S

                // Update the ball's position and velocity
                // Sensor coordinate system: x right, y up (out of device), z out of front
                // Screen coordinate system: x right, y down
                // The TYPE_GRAVITY sensor gives the gravity vector in device coordinates
                // When device is upright in portrait, gravity points down (toward bottom of screen)
                // The sensor Y axis already points in the direction we need for screen coordinates
                // So we use the sensor Y value directly (no inversion needed)
                val xAcc = event.values[0]  // x-axis: same direction in both systems
                val yAcc = event.values[1]  // y-axis: use sensor y directly (sensor y already matches screen y direction)

                // Store the current acceleration values
                lastXAcc = xAcc
                lastYAcc = yAcc

                currentBall.updatePositionAndVelocity(xAcc = xAcc, yAcc = yAcc, dT = dT)

                // Update the StateFlow to notify the UI
                _ballPosition.update { Offset(currentBall.posX, currentBall.posY) }
            } else {
                // First event after reset - capture the current phone orientation
                // Convert sensor coordinates to screen coordinates
                // Use sensor values directly (sensor y already matches screen y direction)
                val xAcc = event.values[0]  // x-axis: same direction
                val yAcc = event.values[1]  // y-axis: use sensor y directly
                lastXAcc = xAcc
                lastYAcc = yAcc
                // Initialize the ball with current orientation so it starts moving correctly
                currentBall.initializeAcceleration(xAcc, yAcc)
            }

            // Update the lastTimestamp
            lastTimestamp = event.timestamp
        }
    }

    fun reset() {
        ball?.reset()

        ball?.let {
            _ballPosition.value = Offset(it.posX, it.posY)
            // Initialize the ball with the current phone orientation if we have it
            // This ensures the ball will start moving in the correct direction immediately
            if (lastTimestamp != 0L) {
                it.initializeAcceleration(lastXAcc, lastYAcc)
            }
        }

        // Reset timestamp so next sensor event will initialize if needed
        // but keep the last acceleration values so we can use them
        lastTimestamp = 0L
    }
}