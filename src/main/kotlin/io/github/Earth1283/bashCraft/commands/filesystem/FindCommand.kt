package io.github.Earth1283.bashCraft.commands.filesystem

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths

class FindCommand : LinuxCommand("find", "Search for files/entities", "find [path] [-name pattern] [-type type]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val player = sender as? Player
        val parsed = parseFind(args)
        val searchPath = parsed.path?.let { PathUtil.resolve(session, it, player) } ?: session.workingPath

        val namePattern = parsed.name?.replace("*", ".*")?.replace("?", ".").let {
            if (it != null) Regex(it, RegexOption.IGNORE_CASE) else null
        }
        val typeFilter = parsed.type

        val results = mutableListOf<String>()

        val worlds = if (PathUtil.isRoot(searchPath)) {
            Bukkit.getWorlds()
        } else {
            listOfNotNull(PathUtil.toWorld(searchPath))
        }

        for (world in worlds) {
            world.entities.forEach { entity ->
                val name = if (entity is Player) entity.name else entity.type.name.lowercase()
                val matches = (namePattern == null || namePattern.matches(name)) &&
                    (typeFilter == null || matchesType(entity, typeFilter))
                if (matches) {
                    val loc = entity.location
                    results += "/${world.name}/${loc.blockX}/${loc.blockY}/${loc.blockZ}/${name}"
                }
            }
        }

        if (results.isEmpty()) results += "(no results)"
        return results
    }

    private fun matchesType(entity: org.bukkit.entity.Entity, type: String): Boolean {
        if (type.equals("player", ignoreCase = true)) return entity is Player
        val et = try { EntityType.valueOf(type.uppercase()) } catch (_: Exception) { return false }
        return entity.type == et
    }

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val player = sender as? Player
        val parsed = parseFind(args)
        val searchStr = parsed.path?.let { PathUtil.resolve(session, it, player) } ?: session.workingPath
        val searchPath = Paths.get(searchStr)

        if (!Files.exists(searchPath)) return listOf("find: '$searchStr': No such file or directory")

        val nameRegex = parsed.name?.replace(".", "\\.")?.replace("*", ".*")?.replace("?", ".").let {
            if (it != null) Regex(it, RegexOption.IGNORE_CASE) else null
        }
        val typeFilter = parsed.type   // "f" = file, "d" = dir, "l" = symlink
        val minSize = parsed.minSize
        val maxSize = parsed.maxSize
        val maxDepth = parsed.maxDepth ?: Int.MAX_VALUE

        val results = mutableListOf<String>()
        try {
            Files.walk(searchPath, maxDepth).use { stream ->
                stream.forEach { p ->
                    val name = p.fileName?.toString() ?: return@forEach
                    val isDir = Files.isDirectory(p)
                    val isLink = Files.isSymbolicLink(p)
                    val size = if (Files.isRegularFile(p)) Files.size(p) else 0L

                    val typeMatch = when (typeFilter) {
                        "f" -> !isDir && !isLink
                        "d" -> isDir
                        "l" -> isLink
                        null -> true
                        else -> true
                    }
                    val nameMatch = nameRegex == null || nameRegex.matches(name)
                    val sizeMatch = (minSize == null || size >= minSize) && (maxSize == null || size <= maxSize)

                    if (typeMatch && nameMatch && sizeMatch) {
                        results += p.toString()
                    }
                }
            }
        } catch (e: Exception) {
            return listOf("find: ${e.message}")
        }

        if (results.isEmpty()) results += "(no matches)"
        return results
    }

    private data class FindArgs(
        val path: String? = null,
        val name: String? = null,
        val type: String? = null,
        val minSize: Long? = null,
        val maxSize: Long? = null,
        val maxDepth: Int? = null,
    )

    private fun parseFind(args: Array<String>): FindArgs {
        var path: String? = null
        var name: String? = null
        var type: String? = null
        var minSize: Long? = null
        var maxSize: Long? = null
        var maxDepth: Int? = null

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "-name" -> { i++; if (i < args.size) name = args[i] }
                "-type" -> { i++; if (i < args.size) type = args[i] }
                "-size" -> {
                    i++; if (i < args.size) {
                        val s = args[i]
                        if (s.startsWith("+")) minSize = parseSize(s.drop(1))
                        else if (s.startsWith("-")) maxSize = parseSize(s.drop(1))
                        else minSize = parseSize(s)
                    }
                }
                "-maxdepth" -> { i++; if (i < args.size) maxDepth = args[i].toIntOrNull() }
                else -> if (!args[i].startsWith("-") && path == null) path = args[i]
            }
            i++
        }
        return FindArgs(path, name, type, minSize, maxSize, maxDepth)
    }

    private fun parseSize(s: String): Long {
        val num = s.dropLast(1).toLongOrNull() ?: s.toLongOrNull() ?: return 0L
        return when (s.lastOrNull()?.lowercaseChar()) {
            'k' -> num * 1024
            'm' -> num * 1024 * 1024
            'g' -> num * 1024 * 1024 * 1024
            else -> num
        }
    }
}
