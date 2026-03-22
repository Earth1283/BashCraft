package io.github.Earth1283.bashCraft.commands.fs

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths

class RmCommand : LinuxCommand("rm", "Remove files or directories", "rm [-rf] file...") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> =
        listOf("bash: rm: only available in filesystem mode (/fs on)")

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (!sender.hasPermission("bashcraft.fs.write")) return listOf("rm: Permission denied (requires bashcraft.fs.write)")
        val (flags, rest) = parseFlags(args, "r", "R", "f", "v")
        val recursive = "r" in flags || "R" in flags
        val force = "f" in flags
        val verbose = "v" in flags

        if (rest.isEmpty()) return listOf("rm: missing operand")

        val player = sender as? Player
        val lines = mutableListOf<String>()

        for (arg in rest) {
            val targetStr = PathUtil.resolve(session, arg, player)
            val path = Paths.get(targetStr)

            if (!Files.exists(path)) {
                if (!force) lines += "rm: cannot remove '$targetStr': No such file or directory"
                continue
            }
            if (Files.isDirectory(path) && !recursive) {
                lines += "rm: cannot remove '$targetStr': Is a directory (use -r)"
                continue
            }

            try {
                if (Files.isDirectory(path)) {
                    val deleted = mutableListOf<String>()
                    Files.walk(path).sorted(Comparator.reverseOrder()).forEach { p ->
                        Files.delete(p)
                        deleted += p.toString()
                    }
                    sender.server.logger.warning("[BashCraft] ${sender.name} DELETED directory $targetStr (${deleted.size} entries)")
                    if (verbose) deleted.forEach { lines += "removed '$it'" }
                    else lines += "removed directory '$targetStr' (${deleted.size} entries)"
                } else {
                    Files.delete(path)
                    sender.server.logger.warning("[BashCraft] ${sender.name} deleted $targetStr")
                    if (verbose) lines += "removed '$targetStr'"
                }
            } catch (e: Exception) {
                if (!force) lines += "rm: cannot remove '$targetStr': ${e.message}"
            }
        }
        return lines
    }
}
