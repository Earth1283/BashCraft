# BashCraft

**Linux terminal emulator for Minecraft.** Every Unix coreutil is a Minecraft command. Worlds are filesystems. Players are processes. Entities are files.

Got a Linux hangover? This is the cure.

---

## Commands

### Filesystem (Minecraft world)

| Command | Description |
|---|---|
| `ls [-la]` | At `/`: list worlds. At `/world/`: list players + entities. At a coordinate: list nearby blocks + entities with block properties |
| `pwd` | Print current virtual path |
| `cd [path]` | Navigate the virtual FS; at coordinate level, teleports you there |
| `cat [target]` | Read inventory, equipment, stats, signs, chests |
| `find` | Search for entities by name or type |
| `stat [path]` | Block/biome/entity details at the current location |

### Real Filesystem (`/fs on`)

Toggle real filesystem mode with `/fs on`. Your shell starts at the server JAR directory. All standard navigation and read commands switch to operating on actual files.

| Command | Description |
|---|---|
| `ls` | List real directory contents with permissions, size, and dates |
| `cat` | Read text files (up to 64 KB); binary files show a hex preview |
| `cd` | Navigate real directories |
| `find` | Walk directory tree with `-name`, `-type`, `-size` filters |
| `stat` | File attributes from `BasicFileAttributes` |
| `mv` | Move or rename files |
| `cp [-r]` | Copy files or directories |
| `touch` | Create file or update timestamp |
| `rm [-r]` | Delete files or recursively delete directories |
| `mkdir [-p]` | Create directories |
| `curl [-o file]` | Fetch a URL; display response or save to file |

### System Info

| Command | Description |
|---|---|
| `ps [-aux]` | List online players as processes — PID, world, STAT, per-player session time, location |
| `top` | Live snapshot: TPS, JVM memory bar, chunk/entity counts, player table |
| `free [-h]` | JVM heap usage (total / used / free / max) |
| `uname [-a]` | Server brand, MC version, Java version, OS, hostname |
| `uptime` | Time since server start, TPS load averages |
| `df [-h]` | Each loaded world as a mounted filesystem with size and chunk counts |
| `du [-h]` | World folder size on disk |

### Identity

| Command | Description |
|---|---|
| `whoami` | Name, UUID, gamemode, OP status, health, location |
| `who` | Brief one-line summary for every online player |
| `w` | Detailed table: ping, session time, last command |
| `id [player]` | UUID + permissions list |
| `ping [player]` | Connection latency in ms with a quality label |

### Process Control

| Command | Description |
|---|---|
| `kill [-9] <player>` | Deal lethal damage (`-9` bypasses armour) |
| `killall <type>` | Kill all entities of a given type in the current world |
| `sudo <command>` | Run a command as the server console |
| `sleep <ticks>` | Wait before sending a response (capped at 600 ticks) |

### Text Processing (pipeline filters)

All of these accept piped input and work in both Minecraft and real-FS mode.

`echo` · `grep` · `head` · `tail` · `sort` · `uniq` · `wc` · `tr` · `sed` · `cut`

### Utilities

`date` · `cal` · `history` · `clear` · `alias` · `env` · `export` · `man` · `bc` · `yes` · `factor` · `rev` · `fortune` · `cowsay` · `banner`

---

## Pipes

Commands can be chained with `|`:

```
/ps | grep Steve
/ls | grep region
/cat server.properties | grep level-name
/ps | sort | head -5
/ls | wc -l
```

---

## `ls -l` block listing

Running `ls -l` at a coordinate path shows each nearby block type with Unix-style attributes:

```
drwxr-xr-x  stone                  847  [solid, hardness=1.50, blast=6.0]
-rw-r--r--  dirt                   366  [solid, hardness=0.50, blast=0.5]
drwxr-xr-x  oak_leaves              93  [transparent, flammable=true, hardness=0.20]
-rw-r--r--  sand                    41  [solid, gravity=true, hardness=0.50]
```

Add `-h` to humanise hardness and blast resistance into descriptive labels (`soft`, `medium`, `hard`, `tough`, `very_hard`, `unbreakable`).

---

## `ps` STAT column

```
  PID     TTY              STAT  TIME        COMMAND
  312     world            S     14m 22s     Steve [OP] (42,64,-18)
  891     world_nether     CF    2m 8s       Alex (100,70,200)
```

| Char | Meaning |
|---|---|
| `S` | Survival |
| `C` | Creative |
| `A` | Adventure |
| `R` | Spectator |
| `Z` | Sleeping |
| `F` | Flying |

TIME shows how long the player has been online this session.

---

## Permissions

| Node | Default | Description |
|---|---|---|
| `bashcraft.*` | OP | All permissions |
| `bashcraft.kill` | OP | Kill players/entities |
| `bashcraft.kill.self` | Everyone | Kill self only |
| `bashcraft.sudo` | OP | Run as console |
| `bashcraft.fs` | OP | Real filesystem access |
| `bashcraft.fs.write` | OP | Write/delete real files |
| `bashcraft.fs.curl` | OP | Fetch remote URLs |

All commands are usable from both in-game and the server console.

---

## First boot Easter egg

The first time BashCraft loads on a server, your `$USER` is set to `linus`. After that, it's `root`. You know why.
