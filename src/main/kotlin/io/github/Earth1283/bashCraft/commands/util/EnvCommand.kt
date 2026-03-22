package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class EnvCommand : LinuxCommand("env", "Display environment variables", "env", listOf("printenv")) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val filter = args.firstOrNull()
        return if (filter != null) {
            val value = session.variables[filter]
            if (value != null) listOf("$filter=$value") else listOf("env: $filter: unbound variable")
        } else {
            session.variables.entries.map { (k, v) -> "$k=$v" }.sorted()
        }
    }
}
