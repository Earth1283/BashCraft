package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class CutCommand : LinuxCommand("cut", "Cut out sections from each line", "cut [-d delim] [-f fields] [-c chars] [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        var delim = "\t"
        var fields: List<Int>? = null
        var chars: IntRange? = null
        val rest = mutableListOf<String>()

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "-d" -> { i++; if (i < args.size) delim = args[i] }
                "-f" -> { i++; if (i < args.size) fields = parseFields(args[i]) }
                "-c" -> { i++; if (i < args.size) chars = parseRange(args[i]) }
                else -> rest += args[i]
            }
            i++
        }

        val input = pipedInput ?: rest.ifEmpty { return listOf("cut: no input") }

        return input.map { line ->
            when {
                chars != null -> {
                    val r = chars
                    if (r.first <= line.length) line.substring(r.first - 1, r.last.coerceAtMost(line.length)) else ""
                }
                fields != null -> {
                    val cols = line.split(delim)
                    fields.mapNotNull { f -> cols.getOrNull(f - 1) }.joinToString(delim)
                }
                else -> line
            }
        }
    }

    private fun parseFields(s: String): List<Int> {
        val result = mutableListOf<Int>()
        s.split(",").forEach { part ->
            if (part.contains("-")) {
                val (a, b) = part.split("-", limit = 2)
                val from = a.toIntOrNull() ?: 1
                val to = b.toIntOrNull() ?: Int.MAX_VALUE
                (from..to.coerceAtMost(100)).forEach { result += it }
            } else {
                part.toIntOrNull()?.let { result += it }
            }
        }
        return result.distinct().sorted()
    }

    private fun parseRange(s: String): IntRange {
        return if (s.contains("-")) {
            val (a, b) = s.split("-", limit = 2)
            (a.toIntOrNull() ?: 1)..(b.toIntOrNull() ?: Int.MAX_VALUE)
        } else {
            val n = s.toIntOrNull() ?: 1
            n..n
        }
    }
}
