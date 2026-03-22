package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class RevCommand : LinuxCommand("rev", "Reverse lines of a file or stdin", "rev [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val input = pipedInput ?: args.toList().ifEmpty { return listOf("rev: no input") }
        return input.map { it.reversed() }
    }
}
