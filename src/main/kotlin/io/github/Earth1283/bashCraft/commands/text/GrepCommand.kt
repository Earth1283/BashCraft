package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender
import java.nio.file.Files
import java.nio.file.Paths

class GrepCommand : LinuxCommand("grep", "Search for pattern in text", "grep [-ivn] pattern [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, rest) = parseFlags(args, "i", "v", "n", "c", "l", "r", "E", "F")
        val caseInsensitive = "i" in flags
        val invert = "v" in flags
        val lineNumbers = "n" in flags
        val countOnly = "c" in flags

        if (rest.isEmpty()) return listOf("grep: missing pattern")

        val pattern = rest[0]
        val options = if (caseInsensitive) setOf(RegexOption.IGNORE_CASE) else emptySet()
        val regex = try {
            if ("F" in flags) Regex.fromLiteral(pattern).let { if (caseInsensitive) Regex(Regex.escape(pattern), RegexOption.IGNORE_CASE) else it }
            else Regex(pattern, options)
        } catch (e: Exception) {
            return listOf("grep: invalid pattern: $pattern")
        }

        val input: List<String> = when {
            pipedInput != null -> pipedInput
            rest.size > 1 -> {
                // Read from files in FS mode
                val lines = mutableListOf<String>()
                for (f in rest.drop(1)) {
                    val path = Paths.get(f)
                    if (Files.exists(path) && Files.isRegularFile(path)) {
                        lines += Files.readAllLines(path)
                    } else {
                        lines += listOf("grep: $f: No such file or directory")
                    }
                }
                lines
            }
            else -> return listOf("grep: no input (pipe something to grep)")
        }

        var matchCount = 0
        val result = mutableListOf<String>()
        input.forEachIndexed { i, line ->
            val matches = regex.containsMatchIn(line)
            val include = if (invert) !matches else matches
            if (include) {
                matchCount++
                if (!countOnly) {
                    val prefix = if (lineNumbers) "${i + 1}:" else ""
                    result += "$prefix$line"
                }
            }
        }

        if (countOnly) return listOf(matchCount.toString())
        return result
    }

}
