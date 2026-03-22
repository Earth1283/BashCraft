package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class SedCommand : LinuxCommand("sed", "Stream editor for filtering and transforming text", "sed 's/pattern/replacement/[g]' [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (args.isEmpty()) return listOf("sed: missing script")

        val scriptArg: String
        val rest: List<String>
        if (args[0] == "-e" && args.size > 1) {
            scriptArg = args[1]
            rest = args.drop(2).toList()
        } else {
            scriptArg = args[0]
            rest = args.drop(1).toList()
        }

        val input = pipedInput ?: rest.ifEmpty { return listOf("sed: no input") }

        // Parse s/pattern/replacement/flags
        val sCmd = parseSCommand(scriptArg)
            ?: return listOf("sed: not an editor command: $scriptArg")

        val options = mutableSetOf<RegexOption>()
        if ('i' in sCmd.flags) options += RegexOption.IGNORE_CASE
        val regex = try { Regex(sCmd.pattern, options) } catch (e: Exception) {
            return listOf("sed: invalid regex: ${sCmd.pattern}")
        }

        return input.map { line ->
            if ('g' in sCmd.flags) regex.replace(line, sCmd.replacement)
            else regex.replaceFirst(line, sCmd.replacement)
        }
    }

    private data class SCommand(val pattern: String, val replacement: String, val flags: String)

    private fun parseSCommand(script: String): SCommand? {
        if (!script.startsWith("s")) return null
        val delim = script.getOrNull(1) ?: return null
        val parts = script.drop(2).split(delim)
        if (parts.size < 2) return null
        val pattern = parts[0]
        val replacement = parts[1]
        val flags = parts.getOrElse(2) { "" }
        return SCommand(pattern, replacement, flags)
    }
}
