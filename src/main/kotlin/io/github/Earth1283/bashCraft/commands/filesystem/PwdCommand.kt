package io.github.Earth1283.bashCraft.commands.filesystem

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PwdCommand : LinuxCommand("pwd", "Print working directory", "pwd") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val lines = mutableListOf(session.workingPath)
        if (sender is Player) {
            val loc = sender.location
            lines += "  (world: ${loc.world?.name}, pos: ${loc.blockX},${loc.blockY},${loc.blockZ})"
        }
        return lines
    }

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        return listOf(session.workingPath)
    }
}
