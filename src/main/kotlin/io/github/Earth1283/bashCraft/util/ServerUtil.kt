package io.github.Earth1283.bashCraft.util

import org.bukkit.Bukkit
import org.bukkit.World

object ServerUtil {

    fun getTPS(): DoubleArray {
        return try {
            val method = Bukkit.getServer().javaClass.getMethod("getTPS")
            method.invoke(Bukkit.getServer()) as DoubleArray
        } catch (_: Exception) {
            doubleArrayOf(20.0, 20.0, 20.0)
        }
    }

    fun formatTPS(tps: Double): String {
        val t = tps.coerceAtMost(20.0)
        return "%.2f".format(t)
    }

    fun serverHostname(): String = try {
        java.net.InetAddress.getLocalHost().hostName
    } catch (_: Exception) { "localhost" }

    fun worldFolderSize(world: World): Long = try {
        java.nio.file.Files.walk(world.worldFolder.toPath())
            .filter { java.nio.file.Files.isRegularFile(it) }
            .mapToLong { java.nio.file.Files.size(it) }
            .sum()
    } catch (_: Exception) { -1L }

    fun entityCount(world: World): Int = world.entityCount

    fun chunkCount(world: World): Int = world.loadedChunks.size

    fun mcTimeToString(time: Long): String {
        // MC time: 0 = 6:00, 6000 = 12:00, 12000 = 18:00, 18000 = 0:00
        val h = ((time + 6000) / 1000 % 24).toInt()
        val m = ((time + 6000) % 1000 * 60 / 1000).toInt()
        val phase = when {
            time < 6000 -> "morning"
            time < 12000 -> "day"
            time < 13000 -> "sunset"
            time < 23000 -> "night"
            else -> "dawn"
        }
        return "%02d:%02d (%s)".format(h, m, phase)
    }

    fun moonPhase(world: World): String {
        val phases = arrayOf("Full Moon", "Waning Gibbous", "Last Quarter", "Waning Crescent",
            "New Moon", "Waxing Crescent", "First Quarter", "Waxing Gibbous")
        return try {
            val method = world.javaClass.getMethod("getMoonPhase")
            val phase = method.invoke(world)
            phase?.let { phases.getOrElse((it as? Int) ?: 0) { phases[0] } } ?: phases[0]
        } catch (_: Exception) {
            val day = world.fullTime / 24000
            phases[(day % 8).toInt()]
        }
    }
}
