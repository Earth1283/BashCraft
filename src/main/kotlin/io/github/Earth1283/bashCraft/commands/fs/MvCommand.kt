package io.github.Earth1283.bashCraft.commands.fs

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class MvCommand : LinuxCommand("mv", "Move (rename) files", "mv [-f] source dest") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> =
        listOf("bash: mv: only available in filesystem mode (/fs on)")

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (!sender.hasPermission("bashcraft.fs.write")) return listOf("mv: Permission denied (requires bashcraft.fs.write)")
        val (flags, rest) = parseFlags(args, "f", "n", "v")
        val force = "f" in flags
        val noOverwrite = "n" in flags
        val verbose = "v" in flags

        if (rest.size < 2) return listOf("mv: missing file operand")

        val player = sender as? Player
        val srcStr = PathUtil.resolve(session, rest[0], player)
        val dstStr = PathUtil.resolve(session, rest[1], player)
        val src = Paths.get(srcStr)
        val dst = Paths.get(dstStr)

        if (!Files.exists(src)) return listOf("mv: cannot stat '$srcStr': No such file or directory")

        val finalDst = if (Files.isDirectory(dst)) dst.resolve(src.fileName) else dst

        if (Files.exists(finalDst) && noOverwrite) return listOf("mv: not overwriting '$dstStr' (use -f to force)")

        return try {
            val opts = if (force) arrayOf(StandardCopyOption.REPLACE_EXISTING) else emptyArray()
            Files.move(src, finalDst, *opts)
            sender.server.logger.warning("[BashCraft] ${sender.name} moved $srcStr -> ${finalDst}")
            if (verbose) listOf("renamed '$srcStr' -> '$finalDst'") else emptyList()
        } catch (e: Exception) {
            listOf("mv: cannot move '$srcStr' to '$dstStr': ${e.message}")
        }
    }
}
