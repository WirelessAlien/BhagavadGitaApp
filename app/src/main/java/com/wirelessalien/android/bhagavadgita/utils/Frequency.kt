package com.wirelessalien.android.bhagavadgita.utils

class Frequency {
    private var lastExecutionTime = 0L

    suspend fun throttle(delayMillis: Long = 1000, action: suspend () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastExecutionTime >= delayMillis) {
            lastExecutionTime = currentTime
            action()
        }
    }
}