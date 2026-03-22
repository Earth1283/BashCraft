package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class FactorCommand : LinuxCommand("factor", "Factor numbers", "factor number...") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val nums = (pipedInput ?: args.toList()).flatMap { it.trim().split(Regex("\\s+")) }
        if (nums.isEmpty()) return listOf("factor: no input")
        return nums.mapNotNull { s ->
            val n = s.toLongOrNull() ?: return@mapNotNull "factor: '$s': not a number"
            if (n < 1) return@mapNotNull "factor: '$n': must be positive"
            "$n: ${factorize(n).joinToString(" ")}"
        }
    }

    private fun factorize(n: Long): List<Long> {
        if (n == 1L) return listOf(1L)
        val factors = mutableListOf<Long>()
        var remaining = n
        var d = 2L
        while (d * d <= remaining) {
            while (remaining % d == 0L) {
                factors += d
                remaining /= d
            }
            d++
        }
        if (remaining > 1) factors += remaining
        return factors
    }
}
