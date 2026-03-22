package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class ExportCommand : LinuxCommand("export", "Set shell variables", "export KEY=value") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (args.isEmpty()) {
            return session.variables.entries.map { (k, v) -> "declare -x $k=\"$v\"" }.sorted()
        }

        val lines = mutableListOf<String>()
        for (arg in args) {
            if (arg.contains("=")) {
                val eq = arg.indexOf('=')
                val name = arg.substring(0, eq)
                val value = arg.substring(eq + 1).removeSurrounding("\"").removeSurrounding("'")
                if (!name.matches(Regex("[A-Za-z_][A-Za-z0-9_]*"))) {
                    lines += "export: `$name': not a valid identifier"
                } else {
                    session.variables[name] = value
                }
            } else {
                // export existing variable (no-op if already set)
                if (session.variables.containsKey(arg)) lines += "${arg}=${session.variables[arg]}"
                else lines += "export: $arg: not found"
            }
        }
        return lines
    }
}
