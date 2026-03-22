package io.github.Earth1283.bashCraft.commands.system

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.ServerUtil
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.io.File
import java.nio.file.Files

class DfCommand : LinuxCommand("df", "Report disk space usage (worlds)", "df [-h]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, _) = parseFlags(args, "h", "H", "k")
        val human = "h" in flags || "H" in flags

        fun fmt(b: Long): String = if (human) Term.humanBytes(b) else "${b / 1024}"
        val unit = if (human) "" else "1K-blocks"

        val lines = mutableListOf<String>()
        lines += "Filesystem            ${Term.pad("Size", 8)}  ${Term.pad("Used", 8)}  ${Term.pad("Avail", 8)}  Use%  Mounted on"

        // Real server disk
        val serverDir = File(System.getProperty("user.dir"))
        val totalDisk = serverDir.totalSpace
        val freeDisk = serverDir.usableSpace
        val usedDisk = totalDisk - freeDisk
        val usePct = if (totalDisk > 0) (usedDisk * 100 / totalDisk).toInt() else 0
        lines += "${Term.pad("/dev/sda1", 22)} ${Term.pad(fmt(totalDisk), 8)}  ${Term.pad(fmt(usedDisk), 8)}  ${Term.pad(fmt(freeDisk), 8)}  $usePct%  /"

        // Each world as a "mount"
        for (world in Bukkit.getWorlds()) {
            val size = ServerUtil.worldFolderSize(world)
            val chunks = world.loadedChunks.size
            val entities = ServerUtil.entityCount(world)
            val mount = "/mc/${world.name}"
            val chunkStr = if (human) "$chunks chunks" else fmt(size)
            val entStr = "$entities entities"
            lines += "${Term.pad("minecraft:${world.name}", 22)} ${Term.pad(chunkStr, 8)}  ${Term.pad(fmt(size), 8)}  ${Term.pad(entStr, 8)}  -     $mount"
        }

        return lines
    }
}
