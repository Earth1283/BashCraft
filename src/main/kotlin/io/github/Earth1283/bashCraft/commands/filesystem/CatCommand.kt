package io.github.Earth1283.bashCraft.commands.filesystem

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import io.github.Earth1283.bashCraft.util.Term
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.nio.file.Files
import java.nio.file.Paths

class CatCommand : LinuxCommand("cat", "Concatenate and print files", "cat [target]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (pipedInput != null) return pipedInput  // cat is transparent in pipes

        val player = sender as? Player ?: return listOf("cat: console: use /cat in filesystem mode (/fs on)")
        val target = args.firstOrNull() ?: "inventory"

        return when (target.lowercase()) {
            "inventory", "inv" -> catInventory(player)
            "equipment", "armor" -> catEquipment(player)
            "stats", "status" -> catStats(player)
            else -> {
                // Try entity selector or look at block
                val block = player.getTargetBlockExact(5)
                if (block != null) {
                    catBlock(block)
                } else {
                    listOf("cat: $target: No such target")
                }
            }
        }
    }

    private fun catInventory(player: Player): List<String> {
        val lines = mutableListOf("=== ${player.name}'s inventory ===")
        val inv = player.inventory
        val slots = (0 until inv.size).mapNotNull { i -> inv.getItem(i)?.let { i to it } }
        if (slots.isEmpty()) return listOf("(empty inventory)")
        slots.forEach { (slot, item) ->
            val name = item.itemMeta?.displayName() ?.let { PlainTextComponentSerializer.plainText().serialize(it) }
                ?: item.type.name.lowercase().replace('_', ' ')
            lines += "  slot[$slot]: $name × ${item.amount}"
        }
        lines += "  [hotbar: ${inv.heldItemSlot}]"
        return lines
    }

    private fun catEquipment(player: Player): List<String> {
        val eq = player.equipment
        val lines = mutableListOf("=== ${player.name}'s equipment ===")
        fun slot(name: String, item: ItemStack?) {
            val display = item?.let { "${it.type.name.lowercase().replace('_', ' ')} × ${it.amount}" } ?: "(empty)"
            lines += "  $name: $display"
        }
        slot("head ", eq.helmet)
        slot("chest", eq.chestplate)
        slot("legs ", eq.leggings)
        slot("feet ", eq.boots)
        slot("main ", eq.itemInMainHand.takeIf { it.type != Material.AIR })
        slot("off  ", eq.itemInOffHand.takeIf { it.type != Material.AIR })
        return lines
    }

    private fun catStats(player: Player): List<String> {
        return listOf(
            "=== ${player.name} ===",
            "  UUID:     ${player.uniqueId}",
            "  Health:   ${player.health}/${player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0}",
            "  Food:     ${player.foodLevel}/20",
            "  XP:       level ${player.level} (${player.exp * 100}%)",
            "  Gamemode: ${player.gameMode.name.lowercase()}",
            "  OP:       ${player.isOp}",
            "  World:    ${player.world.name}",
            "  Pos:      ${player.location.blockX},${player.location.blockY},${player.location.blockZ}",
        )
    }

    private fun catBlock(block: org.bukkit.block.Block): List<String> {
        return when (val state = block.state) {
            is Sign -> {
                val lines = mutableListOf("=== Sign at (${block.x},${block.y},${block.z}) ===")
                state.getSide(Side.FRONT).lines().forEach { line ->
                    lines += "  \"${PlainTextComponentSerializer.plainText().serialize(line)}\""
                }
                lines
            }
            is org.bukkit.block.Chest, is org.bukkit.block.DoubleChest -> {
                val inv = (state as? org.bukkit.block.Container)?.inventory ?: return listOf("(empty container)")
                val lines = mutableListOf("=== Container at (${block.x},${block.y},${block.z}) ===")
                inv.filterNotNull().filter { it.type != Material.AIR }.forEach { item ->
                    val name = item.type.name.lowercase().replace('_', ' ')
                    lines += "  $name × ${item.amount}"
                }
                if (lines.size == 1) lines += "  (empty)"
                lines
            }
            else -> listOf("${block.type.name.lowercase()}: binary data (not a readable block)")
        }
    }

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (pipedInput != null) return pipedInput

        if (args.isEmpty()) return listOf("cat: missing operand")

        val results = mutableListOf<String>()
        for (arg in args) {
            val player = sender as? Player
            val targetStr = PathUtil.resolve(session, arg, player)
            val path = Paths.get(targetStr)

            if (!Files.exists(path)) {
                results += "cat: $arg: No such file or directory"
                continue
            }
            if (Files.isDirectory(path)) {
                results += "cat: $arg: Is a directory"
                continue
            }

            val size = Files.size(path)
            if (size > 64 * 1024) {
                results += "cat: $arg: File too large (${Term.humanBytes(size)}, max 64K). Use head/tail."
                continue
            }

            try {
                val bytes = Files.readAllBytes(path)
                if (isLikelyBinary(bytes)) {
                    results += "=== $arg (binary) ==="
                    bytes.take(256).chunked(16).forEach { chunk ->
                        results += chunk.joinToString(" ") { "%02X".format(it) }
                    }
                    if (bytes.size > 256) results += "... (truncated, ${bytes.size} bytes total)"
                } else {
                    String(bytes, Charsets.UTF_8).lines().forEach { results += it }
                }
            } catch (e: Exception) {
                results += "cat: $arg: ${e.message}"
            }
        }
        return results
    }

    private fun isLikelyBinary(bytes: ByteArray): Boolean {
        var nulls = 0
        for (b in bytes.take(512)) {
            if (b == 0.toByte()) nulls++
        }
        return nulls > 0
    }
}

