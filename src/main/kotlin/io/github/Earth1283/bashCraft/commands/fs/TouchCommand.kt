package io.github.Earth1283.bashCraft.commands.fs

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.time.Instant

class TouchCommand : LinuxCommand("touch", "Change file timestamps / create empty file", "touch file...") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> =
        listOf("bash: touch: only available in filesystem mode (/fs on)")

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (!sender.hasPermission("bashcraft.fs.write")) return listOf("touch: Permission denied (requires bashcraft.fs.write)")
        if (args.isEmpty()) return listOf("touch: missing file operand")

        val player = sender as? Player
        val lines = mutableListOf<String>()
        for (arg in args) {
            val targetStr = PathUtil.resolve(session, arg, player)
            val path = Paths.get(targetStr)
            try {
                if (!Files.exists(path)) {
                    Files.createDirectories(path.parent)
                    Files.createFile(path)
                    lines += "created: $targetStr"
                } else {
                    Files.setLastModifiedTime(path, FileTime.from(Instant.now()))
                }
            } catch (e: Exception) {
                lines += "touch: cannot touch '$targetStr': ${e.message}"
            }
        }
        return lines
    }
}
