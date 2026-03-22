package io.github.Earth1283.bashCraft.commands.filesystem

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.command.CommandSender
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class StatCommand : LinuxCommand("stat", "Display file/entity status", "stat [path|target]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val player = sender as? Player

        if (args.isNotEmpty()) {
            val target = args[0]
            // Try as player name first
            val namedPlayer = org.bukkit.Bukkit.getPlayer(target)
            if (namedPlayer != null) return statPlayer(namedPlayer)
        }

        // Stat the block the player is looking at, or current virtual path
        if (player != null) {
            val block = player.getTargetBlockExact(10)
            if (block != null) return statBlock(block)
        }

        val path = if (args.isNotEmpty()) PathUtil.resolve(session, args[0], player) else session.workingPath
        val loc = PathUtil.toLocation(path)
        if (loc != null) {
            val block = loc.world?.getBlockAt(loc)
            if (block != null) return statBlock(block)
        }

        return listOf("stat: ${args.firstOrNull() ?: "."}: cannot stat")
    }

    private fun statBlock(block: org.bukkit.block.Block): List<String> {
        val world = block.world
        return listOf(
            "  File: ${block.type.name.lowercase()}",
            "  Loc:  (${block.x}, ${block.y}, ${block.z}) in '${world.name}'",
            "  Type: Block",
            "  Biome: ${world.getBiome(block.x, block.y, block.z).name.lowercase()}",
            "  Light: sky=${block.lightFromSky} block=${block.lightFromBlocks}",
            "  Hardness: ${block.type.hardness}",
            "  Blast resistance: ${block.type.blastResistance}",
            "  Solid: ${block.type.isSolid}  Transparent: ${!block.type.isOccluding}",
        )
    }

    private fun statPlayer(p: Player): List<String> {
        val loc = p.location
        return listOf(
            "  File: ${p.name}",
            "  UUID: ${p.uniqueId}",
            "  Type: Player",
            "  World: ${loc.world?.name}  Pos: (${loc.blockX},${loc.blockY},${loc.blockZ})",
            "  Health: ${p.health}/${p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0}  Food: ${p.foodLevel}/20",
            "  Gamemode: ${p.gameMode.name.lowercase()}  OP: ${p.isOp}",
            "  XP level: ${p.level}  Ping: ${p.ping}ms",
        )
    }

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (args.isEmpty()) return listOf("stat: missing operand")
        val player = sender as? Player
        val targetStr = PathUtil.resolve(session, args[0], player)
        val path = Paths.get(targetStr)

        if (!Files.exists(path)) return listOf("stat: cannot statx '$targetStr': No such file or directory")

        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z").withZone(ZoneId.systemDefault())
        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
        val isDir = Files.isDirectory(path)
        val isLink = Files.isSymbolicLink(path)
        val typeStr = when {
            isLink -> "symbolic link"
            isDir -> "directory"
            else -> "regular file"
        }
        val perm = when {
            isLink -> "lrwxrwxrwx"
            isDir -> "drwxr-xr-x"
            Files.isExecutable(path) -> "-rwxr-xr-x"
            else -> "-rw-r--r--"
        }
        return listOf(
            "  File: $targetStr",
            "  Size: ${attrs.size()}  Blocks: ${attrs.size() / 512 + 1}  ${typeStr.replaceFirstChar { it.uppercase() }}",
            "  Access: $perm  Uid: 0 (root)  Gid: 0 (root)",
            "  Access: ${fmt.format(attrs.lastAccessTime().toInstant())}",
            "  Modify: ${fmt.format(attrs.lastModifiedTime().toInstant())}",
            "  Change: ${fmt.format(attrs.creationTime().toInstant())}",
        )
    }
}
