package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.BashCraftConfig
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class HistoryCommand : LinuxCommand("history", "Command history", "history [n]", listOf("h", "hist")) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val n = args.firstOrNull()?.toIntOrNull() ?: BashCraftConfig.historyDefaultLines
        val hist = session.history.takeLast(n)
        val offset = (session.history.size - hist.size) + 1
        return hist.mapIndexed { i, cmd ->
            "  ${(offset + i).toString().padStart(5)}  $cmd"
        }
    }
}
