package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class AliasCommand : LinuxCommand("alias", "Define or display aliases", "alias [name[=value]]", listOf("unalias")) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        // unalias subcommand
        if (args.firstOrNull() == "-d" || args.firstOrNull() == "-r") {
            val target = args.getOrNull(1) ?: return listOf("unalias: usage: unalias name")
            if (session.aliases.remove(target) != null) return listOf("unalias: removed '$target'")
            return listOf("unalias: $target: not found")
        }

        if (args.isEmpty()) {
            return if (session.aliases.isEmpty()) listOf("(no aliases defined)")
            else session.aliases.entries.map { (k, v) -> "alias $k='$v'" }
        }

        val lines = mutableListOf<String>()
        for (arg in args) {
            if (arg.contains("=")) {
                val eq = arg.indexOf('=')
                val name = arg.substring(0, eq)
                val value = arg.substring(eq + 1).removeSurrounding("'").removeSurrounding("\"")
                session.aliases[name] = value
                // Don't output anything on success (bash behavior)
            } else {
                // Display alias
                val value = session.aliases[arg]
                if (value != null) lines += "alias $arg='$value'"
                else lines += "alias: $arg: not found"
            }
        }
        return lines
    }
}
