package io.github.Earth1283.bashCraft

import java.io.File

class TerminalSession(
    val senderKey: String,
    var workingPath: String = "/",
    var fsMode: Boolean = false,
    var promptMode: Boolean = BashCraftConfig.promptMode,
    val joinTime: Long = System.currentTimeMillis(),
    val history: ArrayDeque<String> = ArrayDeque(),
    val variables: MutableMap<String, String> = mutableMapOf(
        "HOME" to "/",
        "USER" to determineUser(),
    ).also { it.putAll(BashCraftConfig.defaultVariables) },
    val aliases: MutableMap<String, String> = BashCraftConfig.defaultAliases.toMutableMap(),
) {
    fun addHistory(line: String) {
        if (line.isNotBlank()) {
            history.addLast(line)
            while (history.size > BashCraftConfig.historySize) history.removeFirst()
        }
    }

    companion object {
        private fun determineUser(): String {
            // Target a hidden file inside the plugin's data folder
            val lockFile = File("plugins/bashCraft/.installed.lock")

            return if (!lockFile.exists()) {
                // First run — create the marker and greet as linus
                lockFile.parentFile.mkdirs()
                try { lockFile.createNewFile() } catch (_: Exception) {}
                "linus"
            } else {
                "root"
            }
        }
    }
}