package de.interaapps.pastefy.auth.ratelimit

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryRateLimiter {
    private val requests = ConcurrentHashMap<String, Window>()

    fun tryAcquire(key: String, limit: Int, windowMillis: Long): Boolean {
        require(limit > 0) { "Rate limit must be positive" }
        require(windowMillis > 0) { "Rate-limit window must be positive" }
        val now = System.currentTimeMillis()
        val window = requests.computeIfAbsent(key) { Window() }
        synchronized(window) {
            window.removeExpired(now - windowMillis)
            if (window.timestamps.size >= limit) return false
            window.timestamps.addLast(now)
            return true
        }
    }

    @Scheduled(fixedDelay = 600_000)
    fun removeInactiveWindows() {
        val oldestUsefulTimestamp = System.currentTimeMillis() - 600_000
        requests.entries.removeIf { (_, window) ->
            synchronized(window) {
                window.removeExpired(oldestUsefulTimestamp)
                window.timestamps.isEmpty()
            }
        }
    }

    private class Window {
        val timestamps = ArrayDeque<Long>()

        fun removeExpired(minimumTimestamp: Long) {
            while (timestamps.firstOrNull()?.let { it <= minimumTimestamp } == true) {
                timestamps.removeFirst()
            }
        }
    }
}
