# L2Loot - Lineage II Spoil Profit Calculator

**Find the best spoil spots for your fellow Dwarfs!** 🛠️

L2Loot is a command-line tool that helps Spoilers find the most profitable monsters to farm. Analyze Mobs loot tables, calculate expected income, and discover the richest hunting grounds in Aden.

## Features

- 📊 **Profit Analysis**: Find the most profitable Mobs within your level range
- 💰 **Smart Calculations**: Considers both spoil loot and regular drops with current market prices
- 🔧 **Price Updates**: Keep item prices current with market changes
- ⚡ **Fast Queries**: Instantly search through 6,334+ NPCs and their loot tables

## Requirements

- **Java 17+** (OpenJDK or Oracle JDK)
- **OS**: Windows, macOS, or Linux

## Quick Setup

**Download and install in one command:**

### Unix/Linux/macOS
```bash
git clone <your-repo-url>
cd l2_spoil_db
chmod +x install.sh
./install.sh
```

### Windows
```cmd
git clone <your-repo-url>
cd l2_spoil_db
install.bat
```

**That's it!** The setup automatically:
- Builds the application
- Creates database with 6,334 NPCs and loot data
- Loads item prices for profit calculations
- Sets up the `l2loot` command

## Usage

### Find Profitable Spoil Spots

**Unix/Linux/macOS:**
```bash
# Find top 10 profitable mobs for levels 20-40
./bin/l2loot farm-analysis --min-level 20 --max-level 40

# Focus on your exact level range
./bin/l2loot farm-analysis --min-level 28 --max-level 33 --limit 5

# Spoil-only income (excludes regular drops)
./bin/l2loot farm-analysis --min-level 30 --max-level 50 --spoil-only
```

**Windows:**
```cmd
# Find top 10 profitable mobs for levels 20-40
.\bin\l2loot.bat farm-analysis --min-level 20 --max-level 40

# Focus on your exact level range
.\bin\l2loot.bat farm-analysis --min-level 28 --max-level 33 --limit 5

# Spoil-only income (excludes regular drops)
.\bin\l2loot.bat farm-analysis --min-level 30 --max-level 50 --spoil-only
```

**Example output:**
```
Top 5 most profitable warrior mobs (levels 28-33):
============================================================
1. https://l2hub.info/c4/npcs/pit_tomb_corpse_eater (Level 31) - 7832 adena average
2. https://l2hub.info/c4/npcs/pit_tomb_jaguar (Level 28) - 3303 adena average
3. https://l2hub.info/c4/npcs/ol_mahum_chief_leader (Level 30) - 2215 adena average
4. https://l2hub.info/c4/npcs/water_observer (Level 31) - 1756 adena average
5. https://l2hub.info/c4/npcs/sucubuss_kanil (Level 32) - 1755 adena average
```

### Browse NPCs and Loot

**Unix/Linux/macOS:**
```bash
# Search for specific NPCs
./bin/l2loot npcs --name "orc"
./bin/l2loot npcs --min-level 25 --max-level 35

# View loot tables
./bin/l2loot corpse-loot        # Spoil loot
./bin/l2loot group-loot-items   # Regular drops
```

**Windows:**
```cmd
# Search for specific NPCs
.\bin\l2loot.bat npcs --name "orc"
.\bin\l2loot.bat npcs --min-level 25 --max-level 35

# View loot tables
.\bin\l2loot.bat corpse-loot        # Spoil loot
.\bin\l2loot.bat group-loot-items   # Regular drops
```

### Update Market Prices

Keep your profit calculations current:

**Unix/Linux/macOS:**
```bash
# Edit item prices
nano seed-data/sellable_items.json

# Update database with new prices
./bin/l2loot update-prices
```

**Windows:**
```cmd
# Edit item prices (use Notepad++ or any editor)
notepad seed-data\sellable_items.json

# Update database with new prices
.\bin\l2loot.bat update-prices
```

## Price Data Format

Edit `seed-data/sellable_items.json` to update item prices:

| Item Name           | Item ID          |
|---------------------|------------------|
| Adamantite Nugget   | admantite_nugget |
| Animal Bone         | animal_bone      |
| Animal Skin         | animal_skin      |
| Asofe               | asofe            |
| Braided Hemp        | braided_hemp     |
| Charcoal            | charcoal          |
| Coal                | coal              |
| Coarse Bone Powder  |coarse_bone_powder |
| Cokes               | cokes             |
| Cord                | cord              |
| Crafted Leather     | crafted_leather   |
| Durable Metal Plate | reinforcing_plate |
| EAA                 | scrl_of_ench_am_a |
| EAB                 | scrl_of_ench_am_b |
| EAC                 | scrl_of_ench_am_c |
| EAD                 | scrl_of_ench_am_d |
| EAS                 | scrl_of_ench_am_s   |
| Enria               | enria               |
| EWA                 | scrl_of_ench_wp_a   |
| EWB                 | scrl_of_ench_wp_b   |
| EWC                 | scrl_of_ench_wp_c   |
| EWD                 | scrl_of_ench_wp_d   |
| EWS                 | scrl_of_ench_wp_s   |
| High Grade Suede    | high_grade_suede    |
| Iron Ore            | iron_ore            |
| Leather             | leather             |
| Metal Hardener      | reinforcing_agent   |
| Metallic Fiber      | metallic_fiber      |
| Metallic Thread     | iron_thread         |
| Mithril Ore         | mithril_ore         |
| Mold Glue           | mold_glue           |
| Mold Hardener       | mold_hardener       |
| Mold Lubricant      | mold_lubricant      |
| Oriharukon Ore      | oriharukon_ore      |
| Steel               | steel               |
| Stone of Purity     | stone_of_purity     |
| Thons               | thons               |
| Varnish             | varnish             |

```json
{
  "items": [
    {
      "item": "animal_bone",
      "price": 1300
    },
    {
      "item": "coarse_bone_powder", 
      "price": 13000
    }
  ]
}
```

## Commands Reference

| Command | Description | Options |
|---------|-------------|---------|
| `farm-analysis` | Find most profitable NPCs | `--min-level`, `--max-level`, `--limit`, `--spoil-only` |
| `npcs` | Browse NPC database | `--name`, `--min-level`, `--max-level`, `--limit` |
| `corpse-loot` | View spoil loot tables | - |
| `group-loot-items` | View regular drop tables | - |
| `update-prices` | Refresh item prices | `--json` |

## File Structure

```
l2_spoil_db/
├── bin/l2loot          # Your spoil calculator
├── database/           # NPC and loot data  
├── seed-data/          # Edit item prices here
│   └── sellable_items.json
└── README.md
```



## Adding to PATH (Optional)

To use `l2loot` from anywhere:

**Unix/Linux/macOS:**
```bash
echo 'export PATH="/path/to/l2_spoil_db/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

**Windows:**
Add `C:\path\to\l2_spoil_db\bin` to your System PATH via Environment Variables.

## Troubleshooting

**"Java not found"** → Install Java 17+ from [Adoptium](https://adoptium.net/)  
**"No profitable mobs found"** → Try a wider level range or update item prices  
**Command not found** → Run from project directory: `./bin/l2loot`

---

*Happy spoiling, fellow Dwarf! May your pouches be heavy with adena.* ⛏️💰
