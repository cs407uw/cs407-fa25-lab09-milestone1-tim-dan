package com.cs407.lab09

/**
 * Represents a ball that can move. (No Android UI imports!)
 *
 * Constructor parameters:
 * - backgroundWidth: the width of the background, of type Float
 * - backgroundHeight: the height of the background, of type Float
 * - ballSize: the width/height of the ball, of type Float
 */
class Ball(
    private val backgroundWidth: Float,
    private val backgroundHeight: Float,
    private val ballSize: Float
) {
    var posX = 0f
    var posY = 0f
    var velocityX = 0f
    var velocityY = 0f
    private var accX = 0f
    private var accY = 0f

    private var isFirstUpdate = true

    init {
        reset()
    }

    /**
     * Updates the ball's position and velocity based on the given acceleration and time step.
     * (See lab handout for physics equations)
     */
    fun updatePositionAndVelocity(xAcc: Float, yAcc: Float, dT: Float) {
        if(isFirstUpdate) {
            isFirstUpdate = false
            accX = xAcc
            accY = yAcc
            return
        }

        // Store previous acceleration
        val a0X = accX
        val a0Y = accY
        
        // Update acceleration
        accX = xAcc
        accY = yAcc
        
        // Store previous velocity
        val v0X = velocityX
        val v0Y = velocityY
        
        // Equation 1: v₁ = v₀ + 1/2 * (a₁ + a₀) * (t₁ - t₀)
        velocityX = v0X + 0.5f * (accX + a0X) * dT
        velocityY = v0Y + 0.5f * (accY + a0Y) * dT
        
        // Equation 2: l = v₀ * (t₁ - t₀) + 1/6 * (t₁ - t₀)² * (3a₀ + a₁)
        val distanceX = v0X * dT + (1.0f / 6.0f) * dT * dT * (3.0f * a0X + accX)
        val distanceY = v0Y * dT + (1.0f / 6.0f) * dT * dT * (3.0f * a0Y + accY)
        
        // Update position
        posX += distanceX
        posY += distanceY
        
        // Check boundaries after updating position
        checkBoundaries()
    }

    /**
     * Ensures the ball does not move outside the boundaries.
     * When it collides, velocity and acceleration perpendicular to the
     * boundary should be set to 0.
     */
    fun checkBoundaries() {
        // Left boundary
        if (posX < 0f) {
            posX = 0f
            velocityX = 0f
            accX = 0f
        }
        
        // Right boundary
        if (posX + ballSize > backgroundWidth) {
            posX = backgroundWidth - ballSize
            velocityX = 0f
            accX = 0f
        }
        
        // Top boundary
        if (posY < 0f) {
            posY = 0f
            velocityY = 0f
            accY = 0f
        }
        
        // Bottom boundary
        if (posY + ballSize > backgroundHeight) {
            posY = backgroundHeight - ballSize
            velocityY = 0f
            accY = 0f
        }
    }

    /**
     * Resets the ball to the center of the screen with zero
     * velocity and acceleration.
     */
    fun reset() {
        // Center the ball on the screen
        posX = (backgroundWidth - ballSize) / 2f
        posY = (backgroundHeight - ballSize) / 2f
        velocityX = 0f
        velocityY = 0f
        accX = 0f
        accY = 0f
        isFirstUpdate = true
    }
}