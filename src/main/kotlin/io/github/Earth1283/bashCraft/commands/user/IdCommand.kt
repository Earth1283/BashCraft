package io.github.Earth1283.bashCraft.commands.user

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class IdCommand : LinuxCommand("id", "Print user identity", "id [player]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val target: Any? = if (args.isNotEmpty()) {
            Bukkit.getPlayer(args[0]) ?: return listOf("id: '${args[0]}': no such player")
        } else sender

        if (target !is Player) {
            return listOf("uid=0(root) gid=0(root) groups=0(root),1(sudo),4(adm)")
        }

        val uid = target.entityId
        val name = target.name
        val uuid = target.uniqueId
        val gm = target.gameMode.name.lowercase()
        val op = if (target.isOp) ",0(sudo),4(ops)" else ""
        val perms = target.effectivePermissions
            .filter { it.value }
            .map { it.permission }
            .take(10)
            .joinToString(",")

        return listOf(
            "uid=$uid($name) gid=1(players) groups=1(players)$op",
            "UUID: $uuid",
            "Gamemode: $gm",
            if (perms.isNotEmpty()) "Permissions (top 10): $perms" else "Permissions: (default only)",
        )
    }
}
