package io.github.Earth1283.bashCraft

import java.io.File

class TerminalSession(
    val senderKey: String,
    var workingPath: String = "/",
    var fsMode: Boolean = false,
    var promptMode: Boolean = false,
    val joinTime: Long = System.currentTimeMillis(),
    val history: ArrayDeque<String> = ArrayDeque(),
    val variables: MutableMap<String, String> = mutableMapOf(
        "HOME" to "/",
        "SHELL" to "/bin/bash",
        "USER" to determineUser(), // Calls the persistence check
        "PATH" to "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
        "EDITOR" to "vi",
        "TERM" to "xterm-256color",
        "LANG" to "en_US.UTF-8",
    ),
    val aliases: MutableMap<String, String> = mutableMapOf(
        "ll" to "ls -la",
        "la" to "ls -a",
        "l" to "ls -CF",
        "grep" to "grep --color=auto",
    ),
) {
    fun addHistory(line: String) {
        if (line.isNotBlank()) {
            history.addLast(line)
            while (history.size > 500) history.removeFirst()
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