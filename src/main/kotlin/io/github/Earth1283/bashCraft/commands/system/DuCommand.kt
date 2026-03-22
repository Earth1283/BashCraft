package io.github.Earth1283.bashCraft.commands.system

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import io.github.Earth1283.bashCraft.util.ServerUtil
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths

class DuCommand : LinuxCommand("du", "Estimate file/world space usage", "du [-h] [path]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, rest) = parseFlags(args, "h", "s")
        val human = "h" in flags
        fun fmt(b: Long) = if (human) Term.humanBytes(b) else "${b / 1024}"

        val lines = mutableListOf<String>()
        val worlds = if (rest.isNotEmpty()) {
            listOfNotNull(Bukkit.getWorld(rest[0]))
                .ifEmpty { return listOf("du: '${rest[0]}': No such world") }
        } else {
            Bukkit.getWorlds()
        }

        for (world in worlds) {
            val size = ServerUtil.worldFolderSize(world)
            val chunks = world.loadedChunks.size
            lines += "${Term.pad(fmt(size), 10)}  /mc/${world.name}  (${chunks} chunks loaded, ${ServerUtil.entityCount(world)} entities)"
        }
        return lines
    }

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, rest) = parseFlags(args, "h", "s", "d")
        val human = "h" in flags
        val summarize = "s" in flags
        val player = sender as? Player
        val targetStr = if (rest.isNotEmpty()) PathUtil.resolve(session, rest[0], player) else session.workingPath
        val path = Paths.get(targetStr)

        if (!Files.exists(path)) return listOf("du: cannot access '$targetStr': No such file or directory")

        fun fmt(b: Long) = if (human) Term.humanBytes(b) else "${b / 1024}"

        return try {
            if (Files.isRegularFile(path)) {
                listOf("${fmt(Files.size(path))}\t$targetStr")
            } else {
                val lines = mutableListOf<String>()
                Files.walk(path, if (summarize) 0 else Int.MAX_VALUE).use { stream ->
                    stream.filter { Files.isDirectory(it) }.forEach { dir ->
                        val size = Files.walk(dir)
                            .filter { Files.isRegularFile(it) }
                            .mapToLong { Files.size(it) }.sum()
                        lines += "${Term.pad(fmt(size), 8)}\t$dir"
                    }
                }
                if (summarize) {
                    val total = Files.walk(path).filter { Files.isRegularFile(it) }.mapToLong { Files.size(it) }.sum()
                    listOf("${fmt(total)}\t$targetStr")
                } else lines
            }
        } catch (e: Exception) {
            listOf("du: ${e.message}")
        }
    }
}
