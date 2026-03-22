package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class YesCommand : LinuxCommand("yes", "Output a string repeatedly", "yes [string]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val text = if (args.isNotEmpty()) args.joinToString(" ") else "y"
        return List(20) { text }  // hard cap at 20 to avoid spam
    }
}
