package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class BannerCommand : LinuxCommand("banner", "Display text as large ASCII art", "banner <text>", listOf("figlet")) {

    private val FONT: Map<Char, Array<String>> = mapOf(
        'A' to a(" ██ ", "█  █", "████", "█  █", "█  █"),
        'B' to a("███ ", "█  █", "███ ", "█  █", "███ "),
        'C' to a("████", "█   ", "█   ", "█   ", "████"),
        'D' to a("███ ", "█  █", "█  █", "█  █", "███ "),
        'E' to a("████", "█   ", "███ ", "█   ", "████"),
        'F' to a("████", "█   ", "███ ", "█   ", "█   "),
        'G' to a("████", "█   ", "█ ██", "█  █", "████"),
        'H' to a("█  █", "█  █", "████", "█  █", "█  █"),
        'I' to a("███ ", " █  ", " █  ", " █  ", "███ "),
        'J' to a("████", "   █", "   █", "█  █", "████"),
        'K' to a("█  █", "█ █ ", "██  ", "█ █ ", "█  █"),
        'L' to a("█   ", "█   ", "█   ", "█   ", "████"),
        'M' to a("█  █", "████", "█ ██", "█  █", "█  █"),
        'N' to a("█  █", "██ █", "█ ██", "█  █", "█  █"),
        'O' to a("████", "█  █", "█  █", "█  █", "████"),
        'P' to a("███ ", "█  █", "███ ", "█   ", "█   "),
        'Q' to a(" ██ ", "█  █", "█  █", "█ ██", " ███"),
        'R' to a("███ ", "█  █", "███ ", "█ █ ", "█  █"),
        'S' to a("████", "█   ", "████", "   █", "████"),
        'T' to a("████", " ██ ", " ██ ", " ██ ", " ██ "),
        'U' to a("█  █", "█  █", "█  █", "█  █", "████"),
        'V' to a("█  █", "█  █", "█  █", " ██ ", " ██ "),
        'W' to a("█  █", "█  █", "█ ██", "████", "█  █"),
        'X' to a("█  █", " ██ ", " ██ ", " ██ ", "█  █"),
        'Y' to a("█  █", "█  █", " ██ ", " ██ ", " ██ "),
        'Z' to a("████", "  █ ", " █  ", "█   ", "████"),
        '0' to a("████", "█  █", "█  █", "█  █", "████"),
        '1' to a(" █  ", "██  ", " █  ", " █  ", "███ "),
        '2' to a("████", "   █", "████", "█   ", "████"),
        '3' to a("████", "   █", "████", "   █", "████"),
        '4' to a("█  █", "█  █", "████", "   █", "   █"),
        '5' to a("████", "█   ", "████", "   █", "████"),
        '6' to a("████", "█   ", "████", "█  █", "████"),
        '7' to a("████", "   █", "  █ ", " █  ", " █  "),
        '8' to a(" ██ ", "█  █", " ██ ", "█  █", " ██ "),
        '9' to a("████", "█  █", "████", "   █", "████"),
        '!' to a(" █  ", " █  ", " █  ", "    ", " █  "),
        '?' to a("████", "   █", " ██ ", "    ", " ██ "),
        '.' to a("    ", "    ", "    ", "    ", " ██ "),
        ',' to a("    ", "    ", "    ", " █  ", " █  "),
        ' ' to a("    ", "    ", "    ", "    ", "    "),
    )

    private fun a(vararg rows: String) = arrayOf(*rows)

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val text = (pipedInput?.firstOrNull() ?: args.joinToString(" ")).uppercase()
        if (text.isBlank()) return listOf("banner: no text")
        if (text.length > 12) return listOf("banner: text too long (max 12 chars for readability)")

        val chars = text.map { c -> FONT[c] ?: FONT['?']!! }
        val rows = (0 until 5).map { row ->
            chars.joinToString(" ") { it[row] }
        }
        return rows
    }
}
