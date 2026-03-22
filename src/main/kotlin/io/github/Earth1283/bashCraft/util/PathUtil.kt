package io.github.Earth1283.bashCraft.util

import io.github.Earth1283.bashCraft.TerminalSession
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.nio.file.Paths

object PathUtil {

    /** Resolve a path string against the session's current working path.
     *  In fsMode, resolves against the real filesystem.
     *  In Minecraft mode, resolves against the virtual world hierarchy. */
    fun resolve(session: TerminalSession, input: String, player: Player?): String {
        if (session.fsMode) {
            return when {
                input.isEmpty() || input == "~" ->
                    System.getProperty("user.dir")
                input.startsWith("/") ->
                    Paths.get(input).normalize().toAbsolutePath().toString()
                else ->
                    Paths.get(session.workingPath).resolve(input).normalize().toAbsolutePath().toString()
            }
        }

        // Virtual Minecraft FS
        val spawnPath = player?.let { "/${it.world.name}" } ?: "/"
        val base = session.workingPath.trimEnd('/')

        val raw = when (input) {
            "", "." -> base.ifEmpty { "/" }
            "~" -> spawnPath
            ".." -> {
                val parts = base.split("/").filter { it.isNotEmpty() }
                if (parts.isEmpty()) "/" else "/" + parts.dropLast(1).joinToString("/")
            }
            else -> if (input.startsWith("/")) input else "$base/$input"
        }

        val normalized = "/" + raw.split("/").filter { it.isNotEmpty() }.joinToString("/")
        return if (normalized == "/") "/" else normalized
    }

    fun toWorld(path: String): World? {
        val parts = path.split("/").filter { it.isNotEmpty() }
        if (parts.isEmpty()) return null
        return Bukkit.getWorld(parts[0])
    }

    fun toLocation(path: String): Location? {
        val parts = path.split("/").filter { it.isNotEmpty() }
        if (parts.size < 4) return null
        val world = Bukkit.getWorld(parts[0]) ?: return null
        return try {
            Location(world, parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble())
        } catch (_: NumberFormatException) { null }
    }

    fun isRoot(path: String) = path == "/"

    fun isWorldDir(path: String): Boolean {
        val parts = path.split("/").filter { it.isNotEmpty() }
        return parts.size == 1
    }

    fun isLocationDir(path: String): Boolean {
        val parts = path.split("/").filter { it.isNotEmpty() }
        return parts.size >= 4
    }

    fun depth(path: String): Int = path.split("/").filter { it.isNotEmpty() }.size
}
