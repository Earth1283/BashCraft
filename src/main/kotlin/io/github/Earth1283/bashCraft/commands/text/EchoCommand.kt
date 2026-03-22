package io.github.Earth1283.bashCraft.commands.text

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class EchoCommand : LinuxCommand("echo", "Display a line of text", "echo [-n] [text...]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val noNewline = args.firstOrNull() == "-n"
        val textArgs = if (noNewline) args.drop(1) else args.toList()
        val raw = textArgs.joinToString(" ")
        val expanded = expandVariables(raw, session)
        return if (noNewline) listOf(expanded) else listOf(expanded)
    }

    private fun expandVariables(text: String, session: TerminalSession): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            if (text[i] == '$' && i + 1 < text.length) {
                val rest = text.substring(i + 1)
                val varName = rest.takeWhile { it.isLetterOrDigit() || it == '_' }
                if (varName.isNotEmpty()) {
                    sb.append(session.variables[varName] ?: "")
                    i += varName.length + 1
                } else {
                    sb.append('$')
                    i++
                }
            } else if (text[i] == '\\' && i + 1 < text.length) {
                sb.append(when (text[i + 1]) {
                    'n' -> '\n'
                    't' -> '\t'
                    'r' -> '\r'
                    '\\' -> '\\'
                    else -> text[i + 1]
                })
                i += 2
            } else {
                sb.append(text[i++])
            }
        }
        return sb.toString()
    }
}
