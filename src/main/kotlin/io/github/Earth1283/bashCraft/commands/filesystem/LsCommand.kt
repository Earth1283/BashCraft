package io.github.Earth1283.bashCraft.commands.filesystem

import io.github.Earth1283.bashCraft.SessionManager
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import io.github.Earth1283.bashCraft.util.ServerUtil
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LsCommand : LinuxCommand(
    "ls", "List directory contents", "ls [-la] [path]",
    listOf("dir", "list")
) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, rest) = parseFlags(args, "l", "a", "R", "h", "1")
        val long = "l" in flags
        val all = "a" in flags
        val human = "h" in flags
        val player = sender as? Player
        val targetPath = if (rest.isNotEmpty()) PathUtil.resolve(session, rest[0], player) else session.workingPath

        return when {
            PathUtil.isRoot(targetPath) -> listRoot(long, all)
            PathUtil.isWorldDir(targetPath) -> {
                val world = PathUtil.toWorld(targetPath)
                    ?: return listOf("ls: cannot access '$targetPath': No such world")
                listWorld(world, long, all)
            }
            PathUtil.isLocationDir(targetPath) -> {
                val loc = PathUtil.toLocation(targetPath)
                    ?: return listOf("ls: cannot access '$targetPath': Invalid path")
                listLocation(loc, long, all, human)
            }
            else -> listOf("ls: cannot access '$targetPath': No such file or directory")
        }
    }

    private fun listRoot(long: Boolean, all: Boolean): List<String> {
        val worlds = Bukkit.getWorlds()
        if (!long) {
            return worlds.map { w ->
                val env = w.environment.name.lowercase()
                "${w.name}/  [$env]"
            }
        }
        val lines = mutableListOf("total ${worlds.size}")
        worlds.forEach { w ->
            val players = w.players.size
            val entities = ServerUtil.entityCount(w)
            val chunks = ServerUtil.chunkCount(w)
            val env = w.environment.name.lowercase()
            val size = ServerUtil.worldFolderSize(w)
            val sizeStr = Term.humanBytes(size)
            lines += "drwxr-xr-x  1 root  root  ${Term.pad(sizeStr, 5)}  ${Term.pad(w.name, 20)} [$env] ${players}p ${entities}e ${chunks} chunks"
        }
        return lines
    }

    private fun listWorld(world: World, long: Boolean, all: Boolean): List<String> {
        val lines = mutableListOf<String>()
        val players = world.players

        if (!long) {
            if (players.isNotEmpty()) {
                lines += "Players: " + players.joinToString("  ") { it.name }
            }
            val counts = world.entities
                .filter { it !is Player }
                .groupBy { it.type.name.lowercase() }
                .mapValues { it.value.size }
                .entries.sortedByDescending { it.value }
            if (all) {
                lines += "Entities:"
                counts.forEach { (type, count) -> lines += "  $type × $count" }
            } else {
                val summary = counts.take(8).joinToString("  ") { "${it.key}×${it.value}" }
                if (summary.isNotEmpty()) lines += "Entities: $summary"
            }
            return lines
        }

        lines += "total ${world.entities.size}"
        players.forEach { p ->
            val gm = gamemodeChar(p.gameMode)
            val op = if (p.isOp) " [OP]" else ""
            val loc = p.location.let { "(${it.blockX},${it.blockY},${it.blockZ})" }
            val health = "♥${p.health.toInt()}/${(p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0).toInt()}"
            lines += "drwxrwxrwx  1 ${Term.pad(p.name, 16)} user  -  $loc  ${Term.pad(p.name, 16)} [PLAYER] $gm$op $health"
        }

        val grouped = world.entities.filter { it !is Player }
            .groupBy { it.type }
            .entries.sortedByDescending { it.value.size }
        val limit = if (all) Int.MAX_VALUE else 20
        grouped.take(limit).forEach { (type, entities) ->
            val typeName = type.name.lowercase()
            lines += "-rw-r--r--  1 root  mob  ${entities.size}  -  $typeName × ${entities.size}"
        }
        if (!all && grouped.size > 20) {
            lines += "... and ${grouped.size - 20} more types (use -a to show all)"
        }
        return lines
    }

    private fun listLocation(loc: org.bukkit.Location, long: Boolean, all: Boolean, human: Boolean = false): List<String> {
        val world = loc.world ?: return listOf("ls: null world")
        val lines = mutableListOf<String>()
        val radius = 5

        // Nearby entities
        val entities = world.getNearbyEntities(loc, 16.0, 16.0, 16.0)
        if (entities.isNotEmpty()) {
            if (!long) {
                lines += "Entities: " + entities.take(20).joinToString("  ") { e ->
                    if (e is Player) e.name else "${e.type.name.lowercase()}[${e.entityId}]"
                }
            } else {
                entities.take(20).forEach { e ->
                    val dist = "%.1f".format(e.location.distance(loc))
                    val name = if (e is Player) e.name else "${e.type.name.lowercase()}[${e.entityId}]"
                    val health = if (e is org.bukkit.entity.LivingEntity) "hp:${e.health.toInt()}/${(e.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0).toInt()}" else ""
                    lines += "-rw-r--r--  entity  ${Term.pad(dist + "m", 8)} ${Term.pad(name, 20)} $health"
                }
            }
        }

        // Block counts + one material sample per type
        val blockCounts = mutableMapOf<String, Int>()
        val blockMats = mutableMapOf<String, org.bukkit.Material>()
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = world.getBlockAt(loc.blockX + x, loc.blockY + y, loc.blockZ + z)
                    val typeName = block.type.name.lowercase()
                    if (all || typeName != "air") {
                        blockCounts[typeName] = (blockCounts[typeName] ?: 0) + 1
                        blockMats[typeName] = block.type
                    }
                }
            }
        }

        val sorted = blockCounts.entries.sortedByDescending { it.value }
        val topBlocks = if (all) sorted else sorted.take(10)

        if (topBlocks.isNotEmpty()) {
            if (!long) {
                lines += "Blocks (${radius}-block radius):"
                topBlocks.forEach { (type, count) ->
                    lines += "  ${Term.pad(type, 24)} × $count"
                }
            } else {
                lines += "total ${topBlocks.sumOf { it.value }} blocks (${radius}-block radius)"
                topBlocks.forEach { (type, count) ->
                    val mat = blockMats[type] ?: return@forEach
                    val perm = if (mat.isOccluding) "drwxr-xr-x" else "-rw-r--r--"
                    val props = blockProps(mat, human)
                    lines += "$perm  ${Term.pad(type, 22)}  ${Term.pad(count.toString(), 5)}  $props"
                }
            }
        }
        return lines
    }

    private fun blockProps(mat: org.bukkit.Material, human: Boolean): String {
        val parts = mutableListOf<String>()

        parts += if (mat.isSolid) "solid" else "transparent"

        if (blockHasGravity(mat)) parts += "gravity=true"

        if (mat.isFlammable) parts += "flammable=true"

        val h = mat.hardness
        parts += if (human) "hardness=${humanHardness(h)}" else "hardness=${"%.2f".format(h)}"

        val br = mat.blastResistance
        if (human || br > 0f) {
            parts += if (human) "blast=${humanBlast(br)}" else "blast=${"%.1f".format(br)}"
        }

        val raw = parts.joinToString(", ")
        return "[${raw.padEnd(52)}]"
    }

    private fun humanHardness(h: Float): String = when {
        h < 0f  -> "unbreakable"
        h == 0f -> "instant"
        h <= 0.5f -> "soft"
        h <= 1.5f -> "medium"
        h <= 3.0f -> "hard"
        h <= 10.0f -> "tough"
        h <= 50.0f -> "very_hard"
        else -> "unbreakable"
    }

    private fun humanBlast(r: Float): String = when {
        r <= 0f    -> "none"
        r <= 2f    -> "fragile"
        r <= 6f    -> "normal"
        r <= 15f   -> "sturdy"
        r <= 1200f -> "resistant"
        else       -> "immune"
    }

    private fun blockHasGravity(mat: org.bukkit.Material): Boolean {
        val n = mat.name
        return n == "SAND" || n == "RED_SAND" || n == "GRAVEL" ||
            n.endsWith("_CONCRETE_POWDER") || n == "ANVIL" ||
            n == "CHIPPED_ANVIL" || n == "DAMAGED_ANVIL" ||
            n == "SCAFFOLDING" || n == "POINTED_DRIPSTONE" ||
            n == "DRAGON_EGG"
    }

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, rest) = parseFlags(args, "l", "a", "h", "R", "1")
        val long = "l" in flags
        val all = "a" in flags
        val human = "h" in flags
        val player = sender as? Player
        val targetStr = if (rest.isNotEmpty()) PathUtil.resolve(session, rest[0], player) else session.workingPath
        val path = java.nio.file.Paths.get(targetStr)

        if (!Files.exists(path)) return listOf("ls: cannot access '$targetStr': No such file or directory")
        if (!Files.isDirectory(path)) {
            return listSingleFile(path, targetStr, long, human)
        }

        val entries = try {
            Files.list(path).use { stream ->
                stream.sorted(Comparator.comparing { p: java.nio.file.Path -> p.fileName.toString() })
                    .collect(java.util.stream.Collectors.toList())
            }
        } catch (e: Exception) {
            return listOf("ls: cannot open directory '$targetStr': ${e.message}")
        }

        val fmt = DateTimeFormatter.ofPattern("MMM dd HH:mm").withZone(ZoneId.systemDefault())

        if (!long) {
            return entries.filter { all || !it.fileName.toString().startsWith(".") }
                .map { p ->
                    val name = p.fileName.toString()
                    if (Files.isDirectory(p)) "$name/" else name
                }
        }

        val lines = mutableListOf<String>()
        var total = 0L
        val fileLines = mutableListOf<String>()

        entries.filter { all || !it.fileName.toString().startsWith(".") }.forEach { p ->
            val name = p.fileName.toString()
            try {
                val attrs = Files.readAttributes(p, BasicFileAttributes::class.java)
                val size = attrs.size()
                total += size
                val sizeStr = if (human) Term.pad(Term.humanBytes(size), 6) else Term.pad(size.toString(), 10)
                val date = fmt.format(attrs.lastModifiedTime().toInstant())
                val isDir = Files.isDirectory(p)
                val isLink = Files.isSymbolicLink(p)
                val perm = when {
                    isLink -> "lrwxrwxrwx"
                    isDir -> "drwxr-xr-x"
                    Files.isExecutable(p) -> "-rwxr-xr-x"
                    else -> "-rw-r--r--"
                }
                val displayName = if (isDir) "$name/" else name
                fileLines += "$perm  1 root  root  $sizeStr  $date  $displayName"
            } catch (e: Exception) {
                fileLines += "??????????  1 ?     ?     ${Term.pad("?", 10)}  ???        $name"
            }
        }
        lines += "total ${Term.humanBytes(total)}"
        lines += fileLines
        return lines
    }

    private fun listSingleFile(path: java.nio.file.Path, display: String, long: Boolean, human: Boolean): List<String> {
        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
        if (!long) return listOf(path.fileName.toString())
        val size = attrs.size()
        val sizeStr = if (human) Term.humanBytes(size) else size.toString()
        val fmt = DateTimeFormatter.ofPattern("MMM dd HH:mm").withZone(ZoneId.systemDefault())
        val date = fmt.format(attrs.lastModifiedTime().toInstant())
        val perm = if (Files.isExecutable(path)) "-rwxr-xr-x" else "-rw-r--r--"
        return listOf("$perm  1 root  root  ${Term.pad(sizeStr, 10)}  $date  ${path.fileName}")
    }

    private fun gamemodeChar(gm: GameMode) = when (gm) {
        GameMode.SURVIVAL -> "survival"
        GameMode.CREATIVE -> "creative"
        GameMode.ADVENTURE -> "adventure"
        GameMode.SPECTATOR -> "spectator"
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): MutableList<String> {
        val session = SessionManager.getOrCreate(sender)
        val prefix = args.lastOrNull() ?: ""
        return if (session.fsMode) {
            completeRealPath(session.workingPath, prefix)
        } else {
            Bukkit.getWorlds().map { it.name }.filter { it.startsWith(prefix) }.toMutableList()
        }
    }

    private fun completeRealPath(cwd: String, prefix: String): MutableList<String> = try {
        val base = java.nio.file.Paths.get(cwd)
        val partial = if (prefix.isEmpty()) base else base.resolve(prefix)
        val parent = if (Files.isDirectory(partial)) partial else partial.parent ?: base
        Files.list(parent).use { s ->
            s.collect(java.util.stream.Collectors.toList())
                .map { it.fileName.toString() }
                .filter { it.startsWith(partial.fileName?.toString() ?: "") }
                .toMutableList()
        }
    } catch (_: Exception) { mutableListOf() }
}
