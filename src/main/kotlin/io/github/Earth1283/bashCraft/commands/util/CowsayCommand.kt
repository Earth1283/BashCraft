package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class CowsayCommand : LinuxCommand("cowsay", "Configurable speaking cow", "cowsay [-e eyes] [-T tongue] message", listOf("cowthink")) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val isThink = false // cowthink uses bubbles
        var eyes = "oo"
        var tongue = "  "
        val rest = mutableListOf<String>()

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "-e" -> { i++; if (i < args.size) eyes = args[i].take(2).padEnd(2) }
                "-T" -> { i++; if (i < args.size) tongue = args[i].take(2).padEnd(2) }
                "-d" -> { eyes = "xx"; tongue = "U " }
                "-g" -> { eyes = "$$"; tongue = "  " }
                "-p" -> { eyes = "@@"; tongue = "  " }
                "-s" -> { eyes = "**"; tongue = "U " }
                "-y" -> { eyes = ".."; tongue = "  " }
                "-b" -> { eyes = "=="; tongue = "  " }
                "-w" -> { eyes = "OO"; tongue = "  " }
                else -> rest += args[i]
            }
            i++
        }

        val text = (pipedInput?.joinToString(" ") ?: rest.joinToString(" ")).ifEmpty { "..." }
        val wordWrap = text.chunked(38)
        val width = wordWrap.maxOf { it.length }
        val border = "-".repeat(width + 2)

        val lines = mutableListOf<String>()
        lines += " $border"
        if (wordWrap.size == 1) {
            lines += "< ${wordWrap[0].padEnd(width)} >"
        } else {
            wordWrap.forEachIndexed { idx, line ->
                val padded = line.padEnd(width)
                lines += when (idx) {
                    0 -> "/ $padded \\"
                    wordWrap.lastIndex -> "\\ $padded /"
                    else -> "| $padded |"
                }
            }
        }
        lines += " $border"
        lines += "        \\   ^__^"
        lines += "         \\  ($eyes)\\_______"
        lines += "            (__)\\       )\\/\\"
        lines += "             $tongue ||----w |"
        lines += "                ||     ||"
        return lines
    }
}
