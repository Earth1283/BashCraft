package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class SortCommand : LinuxCommand("sort", "Sort lines of text", "sort [-r|-n|-u] [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, rest) = parseFlags(args, "r", "n", "u", "f", "R")
        val reverse = "r" in flags
        val numeric = "n" in flags
        val unique = "u" in flags
        val ignoreCase = "f" in flags
        val random = "R" in flags

        val input = pipedInput ?: rest.ifEmpty { return listOf("sort: no input") }

        var sorted = if (random) {
            input.shuffled()
        } else if (numeric) {
            input.sortedWith(Comparator { a, b ->
                val na = a.trimStart().toDoubleOrNull() ?: Double.MAX_VALUE
                val nb = b.trimStart().toDoubleOrNull() ?: Double.MAX_VALUE
                na.compareTo(nb)
            })
        } else if (ignoreCase) {
            input.sortedWith(String.CASE_INSENSITIVE_ORDER)
        } else {
            input.sorted()
        }

        if (reverse && !random) sorted = sorted.reversed()
        if (unique) sorted = sorted.distinct()
        return sorted
    }
}
