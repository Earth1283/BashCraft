# BashCraft

A Paper 1.21 plugin that emulates a Linux terminal inside Minecraft. Every Unix coreutil is a Minecraft command. Worlds are filesystems. Players are processes. Entities are files.

---

## Requirements

- Paper 1.21
- Java 21
- Kotlin stdlib (shadowed into the JAR — no separate install)

---

## Building

```bash
./gradlew shadowJar
# Output: build/libs/BashCraft-1.0.jar
```

Drop the JAR into `plugins/`. No configuration file is required.

---

## Architecture

```
src/main/kotlin/io/github/Earth1283/bashCraft/
├── BashCraft.kt              # Plugin entry point; stores startTime + instance
├── CommandRegistry.kt        # Reflects into Bukkit's SimpleCommandMap to register all commands
├── SessionManager.kt         # Listener; owns the UUID→TerminalSession map
├── TerminalSession.kt        # Per-sender state: workingPath, fsMode, history, variables, aliases
│
├── util/
│   ├── Term.kt               # Adventure API helpers: colorize(), send(), prompt(), pad(), humanBytes()
│   ├── PathUtil.kt           # Virtual path resolution: resolve(), toWorld(), toLocation(), isRoot()…
│   └── ServerUtil.kt         # TPS via reflection, worldFolderSize(), mcTimeToString()
│
└── commands/
    ├── LinuxCommand.kt       # Abstract base — run() / runFs() / execute() with pipe dispatch
    ├── FsCommand.kt          # /fs on|off|status|tree  (standalone BukkitCommand, not LinuxCommand)
    ├── filesystem/           # Dual-mode commands (Minecraft + real FS): ls, pwd, cd, cat, find, stat
    ├── fs/                   # FS-mode-only commands: mv, cp, touch, rm, mkdir, curl
    ├── system/               # Server info: ps, top, free, uname, uptime, df, du
    ├── user/                 # Identity: whoami, who, w, id, ping
    ├── process/              # Control: kill, killall, sudo, sleep
    ├── text/                 # Pipeline filters: echo, grep, head, tail, sort, uniq, wc, tr, sed, cut
    └── util/                 # Utilities: date, cal, history, clear, alias, env, export, man, bc,
                              #            yes, factor, rev, fortune, cowsay, banner
```

### Dual-mode dispatch

`LinuxCommand.execute()` checks `session.fsMode` and dispatches to either `run()` (Minecraft virtual FS) or `runFs()` (real `java.nio.file.Path` operations). The default `runFs()` delegates to `run()`, so pure text-processing commands (grep, head, sort, etc.) work identically in both modes without any override.

Commands that need genuinely different FS behavior (`ls`, `cd`, `cat`, `find`, `stat`, `du`) override `runFs()` explicitly. FS-only commands (`mv`, `cp`, `rm`, `touch`, `mkdir`, `curl`) keep an error-returning `run()` and a real `runFs()`.

### Pipe operator

`execute()` splits `args` on the `"|"` token into stages. Each stage is looked up via `CommandRegistry.get()` and called directly (bypassing Bukkit dispatch), with the previous stage's `List<String>` passed as `pipedInput`. Alias expansion happens per stage.

### Session state (`TerminalSession`)

| Field | Type | Purpose |
|---|---|---|
| `senderKey` | `String` | UUID for players, `"CONSOLE"` for console |
| `workingPath` | `String` | Current virtual or real path |
| `fsMode` | `Boolean` | Real filesystem mode toggle |
| `promptMode` | `Boolean` | Print `user@bashcraft:path$` after every command |
| `joinTime` | `Long` | `System.currentTimeMillis()` at login — used by `ps` TIME column |
| `history` | `ArrayDeque<String>` | Last 500 commands |
| `variables` | `MutableMap` | Shell variables (`$HOME`, `$USER`, etc.) |
| `aliases` | `MutableMap` | Alias expansions (`ll`, `la`, `l`, `grep`) |

`USER` is initially set by `determineUser()` (returns `"linus"` on first-ever plugin boot, `"root"` thereafter) and immediately overwritten with the player's in-game name by `SessionManager.getOrCreate()`.

### Command registration

`CommandRegistry.register()` uses reflection to get Bukkit's `SimpleCommandMap` and calls `map.register("bashcraft", cmd)` for every `LinuxCommand`. Commands are accessible as both `/ls` and `/bashcraft:ls`. Aliases are stored in `CommandRegistry.commands` for pipe-stage lookup.

### Output / Adventure API

All output goes through `Term.send()`, which calls `Term.colorize()` on every line before sending it as an Adventure `Component`. `colorize()` applies heuristic colors:

- Red — error lines (`bash: …`, `No such file`, `Permission denied`)
- Yellow bold — `=== headers ===`
- Aqua — directory entries (`drwx…` or bare `name/`)
- Green — tagged lines (`[OP]`, `[creative]`, etc.) and executable files
- White — regular file entries, numbered lines
- Dark gray — `total N` metadata lines
- Gray — everything else

---

## Permissions

| Node | Default | Description |
|---|---|---|
| `bashcraft.*` | op | All permissions |
| `bashcraft.kill` | op | Kill players/entities |
| `bashcraft.kill.self` | true | Kill self only |
| `bashcraft.sudo` | op | Run commands as console |
| `bashcraft.shutdown` | op | Shut down the server |
| `bashcraft.fs` | op | Enter real filesystem mode (`/fs on`) |
| `bashcraft.fs.write` | op | Write/delete real files (`rm`, `mv`, `cp`, `touch`, `mkdir`, `curl -o`) |
| `bashcraft.fs.curl` | op | Fetch remote URLs via `curl` |

---

## Virtual filesystem

In Minecraft mode (default), the virtual path hierarchy is:

```
/                          → lists all loaded worlds
/worldname/                → lists players + entity summary for that world
/worldname/x/y/z/          → lists nearby blocks + entities at that location
```

`cd` at the location level teleports the player. `cd ~` goes to world spawn (Minecraft mode) or the server JAR directory (FS mode).

---

## Real filesystem mode (`/fs`)

```
/fs on        enter real-FS mode; CWD = server JAR directory
/fs off       return to Minecraft virtual-FS mode
/fs status    show current mode and CWD
/fs tree      recursive box-drawing tree of current directory (depth ≤ 4)
```

In FS mode, `ls`, `pwd`, `cd`, `cat`, `grep`, `head`, `tail`, `wc`, `find`, `stat` operate on actual `java.nio.file.Path` objects. Additional write commands become available: `mv`, `cp`, `touch`, `rm`, `mkdir`, `curl`.

Destructive operations log at WARN level. No path sandbox is enforced (these commands are for server operators only).

---

## `ps` STAT column

Two-character field:

| Position | Char | Meaning |
|---|---|---|
| 1st | `S` | Survival |
| 1st | `C` | Creative |
| 1st | `A` | Adventure |
| 1st | `R` | Spectator (R = spectato**R**) |
| 2nd | `Z` | Sleeping in a bed |
| 2nd | `F` | Flying (elytra or creative) |
| 2nd | ` ` | Idle |

---

## Adding a new command

1. Create a class in the appropriate `commands/` subpackage extending `LinuxCommand`.
2. Implement `run()` for Minecraft mode. Override `runFs()` only if real-FS behaviour differs.
3. Add an instance to the `linuxCommands` list in `CommandRegistry.register()`.

No `plugin.yml` `commands:` block entry is needed — registration is entirely via reflection.
