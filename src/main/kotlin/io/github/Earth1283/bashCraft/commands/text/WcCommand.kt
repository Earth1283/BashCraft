package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class WcCommand : LinuxCommand("wc", "Word, line, character, and byte count", "wc [-l|-w|-c|-m] [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, rest) = parseFlags(args, "l", "w", "c", "m")
        val showLines = "l" in flags
        val showWords = "w" in flags
        val showBytes = "c" in flags
        val showChars = "m" in flags
        val showAll = flags.isEmpty()

        val input = pipedInput ?: rest.ifEmpty { return listOf("wc: no input") }

        val lines = input.size
        val words = input.sumOf { it.trim().split(Regex("\\s+")).count { w -> w.isNotEmpty() } }
        val chars = input.sumOf { it.length } + lines  // +newlines
        val bytes = input.joinToString("\n").toByteArray().size

        val parts = mutableListOf<String>()
        if (showAll || showLines) parts += "$lines"
        if (showAll || showWords) parts += "$words"
        if (showAll || showBytes || showChars) parts += if (showChars) "$chars" else "$bytes"

        val label = when {
            showLines && !showWords && !showBytes && !showChars -> "lines"
            showWords && !showLines && !showBytes && !showChars -> "words"
            showBytes && !showLines && !showWords -> "bytes"
            showChars && !showLines && !showWords -> "chars"
            else -> ""
        }

        return listOf("${parts.joinToString(" ")}${if (label.isNotEmpty()) "  ($label)" else ""}")
    }
}
