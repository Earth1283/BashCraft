package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class TailCommand : LinuxCommand("tail", "Output the last part of input", "tail [-n N] [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        var n = 10
        val rest = mutableListOf<String>()
        var i = 0
        while (i < args.size) {
            when {
                args[i] == "-n" && i + 1 < args.size -> { n = args[++i].toIntOrNull() ?: 10 }
                args[i].matches(Regex("-\\d+")) -> { n = args[i].removePrefix("-").toIntOrNull() ?: 10 }
                else -> rest += args[i]
            }
            i++
        }

        val input = pipedInput ?: rest.ifEmpty { return listOf("tail: no input") }
        return input.takeLast(n)
    }
}
