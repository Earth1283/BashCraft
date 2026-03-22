package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class TrCommand : LinuxCommand("tr", "Translate or delete characters", "tr [-d|-s] set1 [set2]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val delete = "-d" in args
        val squeeze = "-s" in args
        val rest = args.filter { it != "-d" && it != "-s" && it != "-ds" && it != "-sd" }

        if (rest.isEmpty()) return listOf("tr: missing operand")

        val set1 = expandSet(rest[0])
        val set2 = if (!delete && rest.size > 1) expandSet(rest[1]) else ""

        val input = pipedInput ?: listOf(rest.drop(if (delete) 1 else 2).joinToString(" "))

        return input.map { line ->
            when {
                delete -> line.filter { c -> c !in set1 }
                squeeze -> {
                    val translated = if (set2.isNotEmpty()) translateChars(line, set1, set2) else line
                    squeezeChars(translated, set2.ifEmpty { set1 })
                }
                set2.isNotEmpty() -> translateChars(line, set1, set2)
                else -> line
            }
        }
    }

    private fun expandSet(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            if (i + 2 < s.length && s[i + 1] == '-') {
                val from = s[i]
                val to = s[i + 2]
                if (from <= to) (from..to).forEach { sb.append(it) }
                else (to..from).reversed().forEach { sb.append(it) }
                i += 3
            } else {
                when (val c = s[i]) {
                    '\\' -> {
                        if (i + 1 < s.length) {
                            sb.append(when (s[i + 1]) {
                                'n' -> '\n'; 't' -> '\t'; '\\' -> '\\'; else -> s[i + 1]
                            })
                            i += 2
                        } else i++
                    }
                    else -> { sb.append(c); i++ }
                }
            }
        }
        return sb.toString()
    }

    private fun translateChars(input: String, set1: String, set2: String): String =
        input.map { c ->
            val idx = set1.indexOf(c)
            if (idx >= 0) set2.getOrElse(idx.coerceAtMost(set2.length - 1)) { set2.last() } else c
        }.joinToString("")

    private fun squeezeChars(input: String, set: String): String {
        val sb = StringBuilder()
        var last: Char? = null
        for (c in input) {
            if (c !in set || c != last) sb.append(c)
            last = c
        }
        return sb.toString()
    }
}
