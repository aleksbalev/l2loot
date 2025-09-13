# L2Loot - Complete Setup Guide for Windows Users 🪟

**A step-by-step guide for Windows users with no programming experience**

This guide will help you install and use L2Loot, a tool that helps Lineage II Spoilers find the most profitable monsters to farm. No programming knowledge required!

## What is L2Loot?

L2Loot is a tool that:
- 📊 Shows you which monsters give the best loot for your level
- 💰 Calculates how much money you can make from spoiling
- 🔧 Lets you update item prices to keep calculations accurate
- ⚡ Searches through 6,334+ monsters instantly

## Part 1: Installing Java (Required)

L2Loot needs Java to run. Here's how to install it:

### Step 1: Download Java
1. Go to https://adoptium.net/
2. Click the big **"Download"** button
3. This will download a file like `OpenJDK21U-jdk_x64_windows_hotspot_21.0.x.x.msi`

### Step 2: Install Java
1. **Double-click** the downloaded file
2. Click **"Next"** through all the screens
3. Click **"Install"** when asked
4. Click **"Finish"** when done

### Step 3: Verify Java is Installed
1. Press **Windows Key + R**
2. Type `cmd` and press **Enter**
3. In the black window that opens, type: `java -version`
4. Press **Enter**
5. You should see something like `openjdk version "21.0.x"`

✅ **If you see a version number, Java is installed correctly!**
❌ **If you see "java is not recognized", restart your computer and try again**

## Part 2: Getting L2Loot

### Option A: If you have the files already
If someone gave you a folder with L2Loot files, skip to **Part 3**.

### Option B: Download from GitHub (if available)
1. Go to the [L2Loot GitHub](https://github.com/aleksbalev/l2loot) page (you might be already here)
2. Click the green **"Code"** button
3. Click **"Download ZIP"**
4. Extract the ZIP file to a folder like `C:\L2Loot\`

## Part 3: Setting Up L2Loot

### Complete Setup in PowerShell
1. Go to the l2loot-main folder
2. **Right-click** in an empty area and select **"Open in Terminal"** it should open terminal in your current folder
3. **Copy and paste** the following commands one by one (press Enter after each):

**Step 1: Build the project (downloads dependencies and builds - takes 3-5 minutes)**
```powershell
.\gradlew.bat build
```
Wait for this to complete. You should see "BUILD SUCCESSFUL" at the end.

**Step 2: Run the setup**
```powershell
.\install.bat
```
Wait for the setup to complete. You'll see messages like "Database initialized successfully".

✅ **Setup is complete when you see "Setup complete!"**

**That's it!** Keep this PowerShell window open - you'll use it to run L2Loot commands.

## Part 4: Using L2Loot

### Using the Same PowerShell Window
If you still have the PowerShell window open from setup, great! If not:
1. Go to your L2Loot folder
2. **Right-click** in an empty area and select **"Open PowerShell window here"**

### Basic Commands - Copy and Paste These

#### Find the Most Profitable Monsters for Your Level
**Copy this command and replace the numbers with your level range:**
```powershell
.\bin\l2loot.bat farm-analysis --min-level 20 --max-level 40
```

**Example for level 30-35 character:**
```powershell
.\bin\l2loot.bat farm-analysis --min-level 30 --max-level 35
```

#### Show Only Top 5 Results
```powershell
.\bin\l2loot.bat farm-analysis --min-level 30 --max-level 35 --limit 5
```

#### Show Only Spoil Income (No Regular Drops)
```powershell
.\bin\l2loot.bat farm-analysis --min-level 30 --max-level 35 --spoil-only
```

#### Search for Specific Monsters
```powershell
.\bin\l2loot.bat npcs --name "orc"
```

#### Find Monsters in a Level Range
```powershell
.\bin\l2loot.bat npcs --min-level 25 --max-level 35
```

#### See All Available Commands
```powershell
.\bin\l2loot.bat --help
```

### Understanding the Results

When you run a farm analysis, you'll see something like:
```
Top 5 most profitable warrior mobs (levels 20-40):
================================================================================
1. pit_tomb_corpse_eater (Level 31)
   L2Hub Link: https://l2hub.info/c4/npcs/pit_tomb_corpse_eater
   Average income per kill: 7100 adena
   Breakdown: corpse 6750 adena + group 349 adena

2. vault_sentinel (Level 35)
   L2Hub Link: https://l2hub.info/c4/npcs/vault_sentinel
   Average income per kill: 5862 adena
   Breakdown: corpse 5457 adena + group 404 adena

3. lith_warlord (Level 36)
   L2Hub Link: https://l2hub.info/c4/npcs/lith_warlord
   Average income per kill: 3672 adena
   Breakdown: corpse 3492 adena + group 180 adena
```

This shows you:
- **Monster Name**: The NPC you should hunt
- **Level**: The monster's level
- **L2Hub Link**: Click to see detailed monster information
- **Average Income**: Total adena you can expect per kill
- **Breakdown**: How much comes from spoil (corpse) vs regular drops (group)

## Part 5: Updating Item Prices

The tool comes with default prices, but you should update them with current server prices for accurate calculations.

### Step 1: Open the Price File
1. In your L2Loot folder, go to the `seed-data` folder
2. **Right-click** on `sellable_items.json`
3. Select **"Open with"** → **"Notepad"** (or Notepad++ if you have it)

### Step 2: Edit Prices
The file looks like this:
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

**To change a price:**
1. Find the item you want to update
2. Change the number after `"price":`
3. **Save the file** (Ctrl + S)

### Step 3: Update the Database
After editing prices, **copy and paste this command** in your PowerShell window:
```powershell
.\bin\l2loot.bat update-prices
```

### Complete Items List with IDs and Abbreviations
| Item Name | Item ID in File | Abbreviation |
|-----------|----------------|--------------|
| Adamantite Nugget | admantite_nugget | an |
| Animal Bone | animal_bone | ab |
| Animal Skin | animal_skin | as |
| Asofe | asofe | asofe |
| Braided Hemp | braided_hemp | bh |
| Charcoal | charcoal | charcoal |
| Coal | coal | coal |
| Coarse Bone Powder | coarse_bone_powder | cbp |
| Cokes | cokes | cokes |
| Cord | cord | cord |
| Crafted Leather | crafted_leather | cl |
| Crystal A | crystal_a | ax |
| Crystal B | crystal_b | bx |
| Crystal C | crystal_c | cx |
| Crystal D | crystal_d | dx |
| Crystal S | crystal_s | sx |
| Durable Metal Plate | reinforcing_plate | dmp |
| EAA | scrl_of_ench_am_a | eaa |
| EAB | scrl_of_ench_am_b | eab |
| EAC | scrl_of_ench_am_c | eac |
| EAD | scrl_of_ench_am_d | ead |
| EAS | scrl_of_ench_am_s | eas |
| Enria | enria | enria |
| EWA | scrl_of_ench_wp_a | ewa |
| EWB | scrl_of_ench_wp_b | ewb |
| EWC | scrl_of_ench_wp_c | ewc |
| EWD | scrl_of_ench_wp_d | ewd |
| EWS | scrl_of_ench_wp_s | ews |
| High Grade Suede | high_grade_suede | hgs |
| Iron Ore | iron_ore | io |
| Leather | leather | leather |
| Metal Hardener | reinforcing_agent | mh |
| Metallic Fiber | metallic_fiber | mf |
| Metallic Thread | iron_thread | mt |
| Mithril Ore | mithril_ore | mo |
| Mold Glue | mold_glue | mg |
| Mold Hardener | mold_hardener | moldh |
| Mold Lubricant | mold_lubricant | ml |
| Oriharukon Ore | oriharukon_ore | oo |
| Silver Nugget | silver_nugget | sn |
| Steel | steel | steel |
| Stem | stem | stem |
| Stone of Purity | stone_of_purity | sop |
| Suede | suede | suede |
| Thons | thons | thons |
| Thread | thread | thread |
| Varnish | varnish | varnish |

*You can also see current prices by running: `.\bin\l2loot.bat get-item-prices`*

### Checking Current Item Prices

To see what prices are currently set in the database, **copy and paste these commands**:

```powershell
# See all item prices
.\bin\l2loot.bat get-item-prices
```

```powershell
# Check specific items (use abbreviations)
.\bin\l2loot.bat get-item-prices --item-abbr ab,steel,io
```

**Example output:**
```
animal_bone (ab): 1300
steel (steel): 10000  
iron_ore (io): 460
```

## Part 6: Quick Reference

### Most Common Commands - Copy and Paste
```powershell
# Find profitable spots for your level (change the numbers!)
.\bin\l2loot.bat farm-analysis --min-level YOUR_MIN_LEVEL --max-level YOUR_MAX_LEVEL
```

```powershell
# Update prices after editing the price file
.\bin\l2loot.bat update-prices
```

```powershell
# Check current item prices
.\bin\l2loot.bat get-item-prices --item-abbr ab,steel,io
```

```powershell
# Search for monsters by name
.\bin\l2loot.bat npcs --name "MONSTER_NAME"
```

```powershell
# Get help
.\bin\l2loot.bat --help
```

### File Locations
- **Main folder**: Where you extracted L2Loot
- **Price file**: `seed-data\sellable_items.json`
- **Database**: `database\` folder (don't touch these files)

## Troubleshooting

### "Java is not recognized as an internal or external command"
**Solution**: Java is not installed or not found
1. Install Java following Part 1 of this guide
2. Restart your computer
3. Try again

### "The system cannot find the path specified"
**Solution**: You're not in the right folder
1. Make sure you opened PowerShell in the L2Loot folder
2. You should see files like `install.bat` and `TECH_README.md` in the folder
3. Close PowerShell and right-click in the L2Loot folder, then select "Open PowerShell window here"

### "Access is denied" when running install.bat
**Solution**: Run as administrator
1. Right-click on `install.bat`
2. Select "Run as administrator"
3. Click "Yes" when Windows asks for permission

### The PowerShell window closes immediately
**Solution**: 
1. Don't double-click the .bat files
2. Open PowerShell first, then copy and paste the commands
3. Right-click in the L2Loot folder and select "Open PowerShell window here"

### "Build artifacts not found" error
**Solutions**:
1. Make sure you ran Step 1 first: `.\gradlew.bat build`
2. Wait for the build to complete successfully (you should see "BUILD SUCCESSFUL")
3. Then run Step 2: `.\install.bat`

### "Build failed" during gradlew.bat build
**Solutions**:
1. Make sure you have internet connection (needed to download dependencies)
2. Make sure no antivirus is blocking the download
3. Try running the build command again: `.\gradlew.bat build`
4. If it keeps failing, try: `.\gradlew.bat clean build`

## Getting Help

If you're still having trouble:
1. Make sure you followed each step exactly as written
2. Try restarting your computer and running the setup again

---

**Happy spoiling! May your pouches be heavy with adena!** ⛏️💰

*This guide was written for complete beginners. If you're comfortable with programming, see the main TECH_README.md for shorter instructions.*
