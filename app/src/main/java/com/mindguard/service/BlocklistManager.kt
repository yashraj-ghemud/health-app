package com.mindguard.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistManager @Inject constructor() {
    
    private val mutex = Mutex()
    
    // Blocked packages with their unblock time
    private val blockedPackages = ConcurrentHashMap<String, Long>()
    
    // StateFlow for observing blocked packages
    private val _blockedPackagesSet = MutableStateFlow<Set<String>>(emptySet())
    val blockedPackagesSet: StateFlow<Set<String>> = _blockedPackagesSet.asStateFlow()
    
    data class BlockInfo(
        val packageName: String,
        val unblockTime: Long,
        val reason: String,
        val ruleId: String
    )
    
    suspend fun addToBlocklist(
        packageName: String,
        durationMinutes: Int,
        reason: String = "Rule violation",
        ruleId: String = "unknown"
    ) {
        mutex.withLock {
            val unblockTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            blockedPackages[packageName] = unblockTime
            
            // Update StateFlow
            _blockedPackagesSet.value = blockedPackages.keys.toSet()
            
            // Schedule automatic unblock
            scheduleUnblock(packageName, unblockTime)
        }
    }
    
    suspend fun removeFromBlocklist(packageName: String) {
        mutex.withLock {
            blockedPackages.remove(packageName)
            _blockedPackagesSet.value = blockedPackages.keys.toSet()
        }
    }
    
    suspend fun isBlocked(packageName: String): Boolean {
        return mutex.withLock {
            val unblockTime = blockedPackages[packageName]
            if (unblockTime == null) {
                false
            } else {
                val currentTime = System.currentTimeMillis()
                if (currentTime >= unblockTime) {
                    // Block has expired, remove it
                    blockedPackages.remove(packageName)
                    _blockedPackagesSet.value = blockedPackages.keys.toSet()
                    false
                } else {
                    true
                }
            }
        }
    }
    
    suspend fun getBlockInfo(packageName: String): BlockInfo? {
        return mutex.withLock {
            val unblockTime = blockedPackages[packageName] ?: return@withLock null
            val currentTime = System.currentTimeMillis()
            
            if (currentTime >= unblockTime) {
                blockedPackages.remove(packageName)
                _blockedPackagesSet.value = blockedPackages.keys.toSet()
                null
            } else {
                BlockInfo(
                    packageName = packageName,
                    unblockTime = unblockTime,
                    reason = "Rule violation",
                    ruleId = "unknown"
                )
            }
        }
    }
    
    suspend fun getRemainingBlockTime(packageName: String): Long {
        return mutex.withLock {
            val unblockTime = blockedPackages[packageName] ?: return@withLock 0L
            val currentTime = System.currentTimeMillis()
            maxOf(0L, unblockTime - currentTime)
        }
    }
    
    suspend fun clearAllBlocks() {
        mutex.withLock {
            blockedPackages.clear()
            _blockedPackagesSet.value = emptySet()
        }
    }
    
    suspend fun getBlockedPackagesCount(): Int {
        return mutex.withLock {
            // Clean up expired blocks first
            val currentTime = System.currentTimeMillis()
            val expiredPackages = blockedPackages.filter { entry ->
                currentTime >= entry.value
            }.keys
            
            expiredPackages.forEach { pkg ->
                blockedPackages.remove(pkg)
            }
            
            if (expiredPackages.isNotEmpty()) {
                _blockedPackagesSet.value = blockedPackages.keys.toSet()
            }
            
            blockedPackages.size
        }
    }
    
    suspend fun getAllBlockInfo(): List<BlockInfo> {
        return mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val activeBlocks = mutableListOf<BlockInfo>()
            val expiredPackages = mutableListOf<String>()
            
            blockedPackages.forEach { entry ->
                val packageName = entry.key
                val unblockTime = entry.value
                if (currentTime >= unblockTime) {
                    expiredPackages.add(packageName)
                } else {
                    activeBlocks.add(
                        BlockInfo(
                            packageName = packageName,
                            unblockTime = unblockTime,
                            reason = "Rule violation",
                            ruleId = "unknown"
                        )
                    )
                }
            }
            
            // Remove expired blocks
            expiredPackages.forEach { pkg ->
                blockedPackages.remove(pkg)
            }
            
            if (expiredPackages.isNotEmpty()) {
                _blockedPackagesSet.value = blockedPackages.keys.toSet()
            }
            
            activeBlocks
        }
    }
    
    private fun scheduleUnblock(packageName: String, unblockTime: Long) {
        // This would typically use a timer or WorkManager to schedule the unblock
        // For now, we'll rely on the isBlocked check to clean up expired blocks
        // In a production app, you might want to use a more sophisticated scheduling mechanism
    }
    
    suspend fun extendBlock(packageName: String, additionalMinutes: Int): Boolean {
        return mutex.withLock {
            val currentUnblockTime = blockedPackages[packageName] ?: return@withLock false
            val newUnblockTime = currentUnblockTime + (additionalMinutes * 60 * 1000L)
            blockedPackages[packageName] = newUnblockTime
            true
        }
    }
    
    suspend fun permanentlyBlock(packageName: String) {
        mutex.withLock {
            // Use a very large timestamp to represent permanent block
            blockedPackages[packageName] = Long.MAX_VALUE
            _blockedPackagesSet.value = blockedPackages.keys.toSet()
        }
    }
    
    suspend fun isPermanentlyBlocked(packageName: String): Boolean {
        return mutex.withLock {
            blockedPackages[packageName] == Long.MAX_VALUE
        }
    }
}
