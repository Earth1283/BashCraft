package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class ClearCommand : LinuxCommand("clear", "Clear terminal screen", "clear", listOf("cls", "reset")) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        // Send 50 blank lines to push content off-screen
        return List(50) { "" }
    }
}
