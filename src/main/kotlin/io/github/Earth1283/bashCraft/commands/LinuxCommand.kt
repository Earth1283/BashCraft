package io.github.Earth1283.bashCraft.commands

import io.github.Earth1283.bashCraft.CommandRegistry
import io.github.Earth1283.bashCraft.SessionManager
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

abstract class LinuxCommand(
    name: String,
    description: String,
    usageMessage: String,
    aliases: List<String> = emptyList(),
) : Command(name, description, "/$usageMessage", aliases) {

    /** Minecraft virtual-FS mode. */
    abstract fun run(
        sender: CommandSender,
        args: Array<String>,
        session: TerminalSession,
        pipedInput: List<String>?,
    ): List<String>

    /** Real filesystem mode. Default: delegate to run() so pure text/system commands work in both modes.
     *  Override only when FS mode needs genuinely different behaviour (ls, cd, cat, find, stat, mv, cp…). */
    open fun runFs(
        sender: CommandSender,
        args: Array<String>,
        session: TerminalSession,
        pipedInput: List<String>?,
    ): List<String> = run(sender, args, session, pipedInput)

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        val session = SessionManager.getOrCreate(sender)

        // Split on "|" to build pipeline stages
        val stages = splitPipes(commandLabel, args, session)

        var pipeInput: List<String>? = null

        for ((index, stage) in stages.withIndex()) {
            val (cmdName, stageArgs) = stage
            val cmd: LinuxCommand? = if (index == 0) this else CommandRegistry.get(cmdName)

            if (cmd == null) {
                sender.sendMessage(Term.err("$cmdName: command not found"))
                return true
            }

            pipeInput = try {
                if (session.fsMode) cmd.runFs(sender, stageArgs, session, pipeInput)
                else cmd.run(sender, stageArgs, session, pipeInput)
            } catch (e: Exception) {
                listOf("bash: ${cmd.name}: ${e.message ?: "unexpected error"}")
            }
        }

        Term.send(sender, pipeInput ?: emptyList())

        val fullLine = buildString {
            append(commandLabel)
            if (args.isNotEmpty()) { append(' '); append(args.joinToString(" ")) }
        }
        session.addHistory(fullLine)

        if (session.promptMode) {
            sender.sendMessage(Term.prompt(sender.name, session.workingPath))
        }

        return true
    }

    /** Split args on the "|" token into (cmdName, args) pairs.
     *  The first stage uses [label] as the command name. */
    private fun splitPipes(
        label: String,
        args: Array<String>,
        session: TerminalSession,
    ): List<Pair<String, Array<String>>> {
        val stages = mutableListOf<Pair<String, Array<String>>>()
        var currentCmd = label
        var currentArgs = mutableListOf<String>()

        for (token in args) {
            if (token == "|") {
                stages += currentCmd to currentArgs.toTypedArray()
                currentCmd = ""
                currentArgs = mutableListOf()
            } else if (currentCmd.isEmpty()) {
                // Expand alias for pipe-stage commands
                val alias = session.aliases[token]
                if (alias != null) {
                    val parts = alias.trim().split(Regex("\\s+"))
                    currentCmd = parts[0]
                    currentArgs = parts.drop(1).toMutableList()
                } else {
                    currentCmd = token
                }
            } else {
                currentArgs += token
            }
        }
        stages += currentCmd to currentArgs.toTypedArray()
        return stages
    }

    /** Parse common flags from args. Returns remaining non-flag args. */
    protected fun parseFlags(args: Array<String>, vararg flags: String): Pair<Set<String>, List<String>> {
        val found = mutableSetOf<String>()
        val rest = mutableListOf<String>()
        for (a in args) {
            when {
                a.startsWith("--") -> {
                    // Support both --flag and --flag=value; silently drop unrecognised long flags
                    val key = a.removePrefix("--").substringBefore("=")
                    if (key in flags) found.add(key)
                }
                a.startsWith("-") -> a.removePrefix("-").forEach { c ->
                    if (c.toString() in flags) found.add(c.toString())
                }
                else -> rest.add(a)
            }
        }
        return found to rest
    }
}
