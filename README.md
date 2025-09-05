# l2_spoil_db CLI

Read-only CLI for exploring an H2 database of Lineage 2 NPC loot. Built with Kotlin, Clikt, Exposed, and H2.

## Prerequisites
- Java 17+ (JDK). The artifacts are compiled for Java 17 bytecode to ensure compatibility with JRE 17 and newer.
- Internet access for Gradle to download dependencies on first build

## Build
You can run the app directly via Gradle, build a regular runnable jar, or build a single self-contained executable (fat JAR).

Run directly:
```
./gradlew :app:run --args="--help"
```

Build a single executable (fat JAR):
```
./gradlew :app:shadowJar
```
This creates `app/build/libs/l2loot.jar`. Run it directly with Java:
```
java -jar app/build/libs/l2loot.jar --help
```

Build a regular (thin) jar:
```
./gradlew :app:jar
```
The thin jar will be at `app/build/libs/app.jar` and requires the classpath; for most users, prefer the fat jar above.

## Database
- Default DB path: `app/mydb` (H2, file mode). The actual file becomes `app/mydb.mv.db`.
- You can seed the DB from JSON using the `--seed-if-empty` flag. Default JSON path: `seed-data/npc_loot_data_complete.json`.
- To use a different DB location, pass `--db /path/to/dbfile-without-extension`.

## Quick start
Seed the DB (if empty) and list the first 50 NPCs (Gradle-run):
```
./gradlew :app:run --args="--seed-if-empty npcs"
```

Get help on the root command and all subcommands:
```
./gradlew :app:run --args="--help"
```

## Command reference
The CLI name is `l2loot`. When running via Gradle, use `./gradlew :app:run --args="[GLOBAL OPTIONS] <subcommand> [OPTIONS]"`. If you build a runnable jar, the form is `java -cp <all needed jars> org.example.AppKt [GLOBAL OPTIONS] <subcommand> [OPTIONS]` (or build a fat jar by adding the Shadow plugin).

### Global options (root)
- `--db <path>`: H2 database path without extension. Default: `./app/mydb`.
- `--seed-if-empty`: If set, seeds DB from JSON when it’s empty.
- `--json <path>`: Path to `npc_loot_data_complete.json`. Default: `seed-data/npc_loot_data_complete.json`.

Examples:
```
# Use custom DB path
java -jar app/build/libs/app-all.jar --db ./mydata/npcdb npcs

# Seed from a custom JSON file
java -jar app/build/libs/app-all.jar --seed-if-empty --json /tmp/npc_loot.json npcs
```

### Subcommands

#### npcs — List NPCs
Options:
- `--name <substring>`: Filter by NPC name (substring match)
- `--min-level <int>`: Minimum level filter (>= 0)
- `--max-level <int>`: Maximum level filter (>= 0)
- `--limit <int>`: Limit results (default 50)
- `--offset <int>`: Offset results (default 0)

Examples:
```
# First 50 NPCs
java -jar app/build/libs/app-all.jar npcs

# Search by name and level range
java -jar app/build/libs/app-all.jar npcs --name orc --min-level 20 --max-level 40 --limit 100
```

#### corpse-loot — List corpse loot entries
Options:
- `--npc-id <int>`: Filter by NPC ID
- `--npc-name <substring>`: Filter by NPC name (substring)
- `--item <substring>`: Filter by item name (substring)
- `--limit <int>`: Default 50
- `--offset <int>`: Default 0

Examples:
```
# Loot for a specific NPC by id
java -jar app/build/libs/app-all.jar corpse-loot --npc-id 123

# Loot for NPCs with names like "spider", items containing "bone"
java -jar app/build/libs/app-all.jar corpse-loot --npc-name spider --item bone
```

#### group-loot-groups — List group loot groups
Options:
- `--npc-id <int>`
- `--npc-name <substring>`
- `--limit <int>` (default 50)
- `--offset <int>` (default 0)

Example:
```
java -jar app/build/libs/app-all.jar group-loot-groups --npc-name elf
```

#### group-loot-items — List items within a loot group
Options:
- `--group-id <int>`
- `--item <substring>`
- `--limit <int>` (default 50)
- `--offset <int>` (default 0)

Example:
```
java -jar app/build/libs/app-all.jar group-loot-items --group-id 456
```

## Running from Gradle
You can pass arguments to the application via `--args`:
```
./gradlew :app:run --args="--seed-if-empty npcs --name wolf"
```

## Troubleshooting
- If you get an H2 connection error, ensure the path in `--db` is writable and not a directory. The tool expects a file path without the `.mv.db` extension.
- The schema (tables) is auto-created on first run. If you want data, pass `--seed-if-empty` to import from JSON; otherwise, tables will exist but may be empty and queries will return no rows.
- For large outputs, increase `--limit` or use UNIX tools to page results: `... | less`.


## Install as a command (l2loot)
After you build, you can generate native launcher scripts named `l2loot` (Unix) and `l2loot.bat` (Windows) via the Gradle Application plugin.

1) Generate the distribution:
```
./gradlew :app:installDist
```
This creates scripts under `app/build/install/app/bin/`.

2) Option A: Run from the build folder directly:
```
app/build/install/app/bin/l2loot --help
app/build/install/app/bin/l2loot --seed-if-empty npcs --name wolf
```

3) Option B (recommended): Add the bin folder to your PATH (temporary for the current shell):
```
export PATH="$PWD/app/build/install/app/bin:$PATH"
```
Then you can use it from anywhere in the project:
```
l2loot --help
l2loot --db ./app/mydb --seed-if-empty npcs
```

4) Option C: Create a symlink to install system-wide (may require sudo):
```
sudo ln -sf "$PWD/app/build/install/app/bin/l2loot" /usr/local/bin/l2loot
```

Notes:
- The default DB location remains `./app/mydb`. When running from other directories, pass an absolute or appropriate relative `--db` path.
- You can still use the fat JAR if you prefer: `java -jar app/build/libs/l2loot.jar ...`.
