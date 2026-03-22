package io.github.Earth1283.bashCraft.commands.fs

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class CpCommand : LinuxCommand("cp", "Copy files and directories", "cp [-r] source dest") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> =
        listOf("bash: cp: only available in filesystem mode (/fs on)")

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (!sender.hasPermission("bashcraft.fs.write")) return listOf("cp: Permission denied (requires bashcraft.fs.write)")
        val (flags, rest) = parseFlags(args, "r", "R", "f", "v")
        val recursive = "r" in flags || "R" in flags
        val force = "f" in flags
        val verbose = "v" in flags

        if (rest.size < 2) return listOf("cp: missing file operand")

        val player = sender as? Player
        val srcStr = PathUtil.resolve(session, rest[0], player)
        val dstStr = PathUtil.resolve(session, rest[1], player)
        val src = Paths.get(srcStr)
        val dst = Paths.get(dstStr)

        if (!Files.exists(src)) return listOf("cp: cannot stat '$srcStr': No such file or directory")
        if (Files.isDirectory(src) && !recursive) return listOf("cp: -r not specified; omitting directory '$srcStr'")

        val lines = mutableListOf<String>()
        try {
            if (Files.isDirectory(src)) {
                val finalDst = if (Files.exists(dst)) dst.resolve(src.fileName) else dst
                Files.walk(src).use { walk ->
                    walk.forEach { file ->
                        val rel = src.relativize(file)
                        val target = finalDst.resolve(rel)
                        if (Files.isDirectory(file)) Files.createDirectories(target)
                        else {
                            Files.createDirectories(target.parent)
                            val opts = if (force) arrayOf(StandardCopyOption.REPLACE_EXISTING) else emptyArray()
                            Files.copy(file, target, *opts)
                            if (verbose) lines += "'$file' -> '$target'"
                        }
                    }
                }
            } else {
                val finalDst = if (Files.isDirectory(dst)) dst.resolve(src.fileName) else dst
                val opts = if (force) arrayOf(StandardCopyOption.REPLACE_EXISTING) else emptyArray()
                Files.copy(src, finalDst, *opts)
                if (verbose) lines += "'$src' -> '$finalDst'"
            }
            sender.server.logger.warning("[BashCraft] ${sender.name} copied $srcStr -> $dstStr")
        } catch (e: Exception) {
            lines += "cp: error copying '$srcStr' to '$dstStr': ${e.message}"
        }
        return lines
    }
}
