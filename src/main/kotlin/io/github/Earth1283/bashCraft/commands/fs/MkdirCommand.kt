package io.github.Earth1283.bashCraft.commands.fs

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths

class MkdirCommand : LinuxCommand("mkdir", "Make directories", "mkdir [-p] dir...") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> =
        listOf("bash: mkdir: only available in filesystem mode (/fs on)")

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (!sender.hasPermission("bashcraft.fs.write")) return listOf("mkdir: Permission denied (requires bashcraft.fs.write)")
        val (flags, rest) = parseFlags(args, "p", "v")
        val parents = "p" in flags
        val verbose = "v" in flags

        if (rest.isEmpty()) return listOf("mkdir: missing operand")

        val player = sender as? Player
        val lines = mutableListOf<String>()

        for (arg in rest) {
            val targetStr = PathUtil.resolve(session, arg, player)
            val path = Paths.get(targetStr)
            try {
                if (Files.exists(path)) {
                    if (!parents) lines += "mkdir: cannot create directory '$targetStr': File exists"
                } else {
                    if (parents) Files.createDirectories(path)
                    else Files.createDirectory(path)
                    if (verbose) lines += "mkdir: created directory '$targetStr'"
                }
            } catch (e: Exception) {
                lines += "mkdir: cannot create directory '$targetStr': ${e.message}"
            }
        }
        return lines
    }
}
