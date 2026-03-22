package io.github.Earth1283.bashCraft.commands.user

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WhoamiCommand : LinuxCommand("whoami", "Print current user identity", "whoami") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val player = sender as? Player ?: return listOf("root")
        return listOf(
            player.name,
            "  UUID:     ${player.uniqueId}",
            "  Gamemode: ${player.gameMode.name.lowercase()}",
            "  OP:       ${player.isOp}",
            "  World:    ${player.world.name}",
            "  Pos:      ${player.location.blockX},${player.location.blockY},${player.location.blockZ}",
            "  Health:   ${player.health}/${player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0}",
            "  Ping:     ${player.ping}ms",
        )
    }
}
