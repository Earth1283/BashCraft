package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class UniqCommand : LinuxCommand("uniq", "Report or omit repeated lines", "uniq [-c|-d|-u] [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, rest) = parseFlags(args, "c", "d", "u", "i")
        val count = "c" in flags
        val duplicatesOnly = "d" in flags
        val uniqueOnly = "u" in flags
        val ignoreCase = "i" in flags

        val input = pipedInput ?: rest.ifEmpty { return listOf("uniq: no input") }
        if (input.isEmpty()) return emptyList()

        data class Run(val line: String, val n: Int)

        val runs = mutableListOf<Run>()
        for (line in input) {
            val key = if (ignoreCase) line.lowercase() else line
            val lastKey = runs.lastOrNull()?.line?.let { if (ignoreCase) it.lowercase() else it }
            if (key == lastKey) {
                runs[runs.lastIndex] = Run(runs.last().line, runs.last().n + 1)
            } else {
                runs += Run(line, 1)
            }
        }

        return runs.filter { r ->
            when {
                duplicatesOnly -> r.n > 1
                uniqueOnly -> r.n == 1
                else -> true
            }
        }.map { r ->
            if (count) "${r.n.toString().padStart(7)} ${r.line}" else r.line
        }
    }
}
