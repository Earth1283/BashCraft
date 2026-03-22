package io.github.Earth1283.bashCraft.commands.filesystem

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths

class CdCommand : LinuxCommand("cd", "Change directory", "cd [path]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val player = sender as? Player
        val input = args.firstOrNull() ?: "~"
        val target = PathUtil.resolve(session, input, player)

        // Validate target
        val valid = when {
            PathUtil.isRoot(target) -> true
            PathUtil.isWorldDir(target) -> PathUtil.toWorld(target) != null
            PathUtil.isLocationDir(target) -> PathUtil.toLocation(target) != null
            else -> false
        }

        if (!valid) return listOf("cd: $target: No such directory")

        session.workingPath = target

        // Teleport player if at a location path
        if (player != null && PathUtil.isLocationDir(target)) {
            val loc = PathUtil.toLocation(target) ?: return listOf("cd: $target: Invalid coordinates")
            player.teleport(loc)
            return listOf("Teleported to ${loc.world?.name} ${loc.blockX},${loc.blockY},${loc.blockZ}")
        }

        // Teleport to world spawn if cd into world dir
        if (player != null && PathUtil.isWorldDir(target)) {
            val world = PathUtil.toWorld(target)
            if (world != null && world != player.world) {
                player.teleport(world.spawnLocation)
                return listOf("Changed to world: ${world.name}")
            }
        }

        return emptyList()
    }

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val player = sender as? Player
        val input = args.firstOrNull() ?: "~"
        val target = PathUtil.resolve(session, input, player)

        val path = Paths.get(target)
        return when {
            !Files.exists(path) -> listOf("cd: $target: No such file or directory")
            !Files.isDirectory(path) -> listOf("cd: $target: Not a directory")
            else -> {
                session.workingPath = path.toAbsolutePath().toString()
                emptyList()
            }
        }
    }
}
