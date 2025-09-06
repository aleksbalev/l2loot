package com.l2spoildb.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.help
import com.l2spoildb.loot.CorpseLoot
import com.l2spoildb.loot.GroupLootGroups
import com.l2spoildb.loot.GroupLootItems
import com.l2spoildb.loot.LootLoader
import com.l2spoildb.loot.NpcLootSeeder
import com.l2spoildb.loot.Npcs
import com.l2spoildb.loot.SellableItem
import com.l2spoildb.loot.SellableItems
import com.l2spoildb.utils.AbbreviationToItemKeyMap
import com.l2spoildb.utils.getKeyByValue
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.nio.file.Path
import kotlin.math.roundToInt

fun getDefaultSellableItemsPath(): String {
    val projectPath = "seed-data/sellable_items.json"
    return projectPath
}

class Root : CliktCommand(
    name = "l2loot", 
    help = "Read-only CLI for NPC loot DB",
    invokeWithoutSubcommand = true
) {
    private val dbPath by option("--db", help = "H2 database path without extension").default("./database/mydb")
    private val seedIfEmpty by option("--seed-if-empty", help = "Seed from JSON if tables empty").flag(default = false)
    private val jsonPath by option(
        "--json",
        help = "Path to npc_loot_data_complete.json"
    ).default("seed-data/npc_loot_data_complete.json")

    override fun run() {
        if (seedIfEmpty || currentContext.invokedSubcommand != null) {
            Database.connect("jdbc:h2:$dbPath", driver = "org.h2.Driver")
            
            transaction {
                SchemaUtils.create(Npcs, CorpseLoot, GroupLootGroups, GroupLootItems, SellableItems)
            }
            
            val shouldSeed = seedIfEmpty || transaction { 
                Npcs.selectAll().limit(1).empty()
            }
            
            if (shouldSeed) {
                println("Seeding database...")
                val data = LootLoader.loadFromFile(Path.of(jsonPath))
                NpcLootSeeder.seed(data)
                
                println("Loading item prices...")
                val pricesData = LootLoader.loadSellableItemsFromFile(Path.of(getDefaultSellableItemsPath()))
                
                transaction {
                    SchemaUtils.create(SellableItems)
                    SellableItems.deleteWhere { SellableItems.id greaterEq 1 }
                    
                    pricesData.items.forEach { sellableItem ->
                        SellableItems.insert {
                            it[item] = sellableItem.item
                            it[price] = sellableItem.price
                        }
                    }
                }
                println("Loaded ${pricesData.items.size} item prices")
                

                println("Seeding completed.")
            }
        }
    }
}

class UpdatePricesCommand : CliktCommand(name = "update-prices", help = "Gets given json to update prices in db") {
    private val jsonPath by option(
        "--json",
        help = "Path to json with item prices"
    ).default(getDefaultSellableItemsPath())

    override fun run() {
        transaction {
            SchemaUtils.create(SellableItems)
            
            SellableItems.deleteWhere { SellableItems.id greaterEq 1 }
            
            val data = LootLoader.loadSellableItemsFromFile(Path.of(jsonPath))
            
            data.items.forEach { sellableItem ->
                SellableItems.insert {
                    it[item] = sellableItem.item
                    it[price] = sellableItem.price
                }
            }
            
            println("Successfully updated ${data.items.size} sellable items in the database")
        }
    }
}

data class MobProfitability(
    val npcId: Int,
    val name: String,
    val level: Int,
    val averageIncome: Double
)

class FarmAnalysis : CliktCommand(name = "farm-analysis", help = "Analyze most profitable warrior mobs in level range") {
    private val minLevel by option("--min-level").convert { it.toInt() }.check("min-level must be >= 0") { it >= 0 }
    private val maxLevel by option("--max-level").convert { it.toInt() }.check("max-level must be >= 0") { it >= 0 }
    private val limit by option("--limit").convert { it.toInt() }.default(5).check("limit must be > 0") { it > 0 }
    private val spoilOnly by option("--spoil-only", help = "Calculate income from spoil loot only, excluding group loot").flag(default = false)

    override fun run() {
        if (minLevel == null || maxLevel == null) {
            println("Both --min-level and --max-level are required")
            return
        }
        if (minLevel!! > maxLevel!!) {
            println("min-level must be <= max-level")
            return
        }

        transaction {
            val npcsInRange = Npcs.selectAll()
                .andWhere { Npcs.level greaterEq minLevel!! }
                .andWhere { Npcs.level lessEq maxLevel!! }
                .andWhere { Npcs.npcBegin eq "warrior" }
                .toList()
                .filter { npc -> 
                    val npcName = npc[Npcs.name]
                    !npcName.matches(Regex("^(r\\d+_.*|dusk_\\d+_box|dawn_\\d+_box)$"))
                }

            val mobProfitabilities = mutableListOf<MobProfitability>()

            for (npc in npcsInRange) {
                val npcId = npc[Npcs.id]
                val npcName = npc[Npcs.name]
                val npcLevel = npc[Npcs.level]

                val corpseLootIncome = calculateCorpseLootIncome(npcId)
                val groupLootIncome = if (spoilOnly) 0.0 else calculateGroupLootIncome(npcId)
                
                val totalIncome = corpseLootIncome + groupLootIncome
                
                if (totalIncome > 0) {
                    mobProfitabilities.add(MobProfitability(npcId, npcName, npcLevel, totalIncome))
                }
            }

            val topMobs = mobProfitabilities
                .sortedByDescending { it.averageIncome }
                .take(limit)

            val lootTypeDesc = if (spoilOnly) " (spoil loot only)" else ""
            println("Top $limit most profitable warrior mobs (levels $minLevel-$maxLevel)$lootTypeDesc:")
            println("=".repeat(60))
            topMobs.forEachIndexed { index, mob ->
                println("${index + 1}. https://l2hub.info/c4/npcs/${mob.name} (Level ${mob.level}) - ${mob.averageIncome.roundToInt()} adena average")
            }
            
            if (topMobs.isEmpty()) {
                println("No profitable warrior mobs found in level range $minLevel-$maxLevel")
            }
        }
    }

    private fun calculateCorpseLootIncome(npcId: Int): Double {
        var totalIncome = 0.0
        
        val corpseLoot = CorpseLoot.selectAll()
            .andWhere { CorpseLoot.npcId eq npcId }
            .toList()

        for (loot in corpseLoot) {
            val itemName = loot[CorpseLoot.item]
            val chance = loot[CorpseLoot.chance]
            val minCount = loot[CorpseLoot.minCount]
            val maxCount = loot[CorpseLoot.maxCount]
            
            val averageCount = if (minCount == maxCount) {
                minCount.toDouble()
            } else {
                (minCount + maxCount) / 2.0
            }
            
            val itemPrice = getItemPrice(itemName)
            if (itemPrice > 0) {
                totalIncome += (itemPrice * chance / 100.0) * averageCount
            }
        }
        
        return totalIncome
    }

    private fun calculateGroupLootIncome(npcId: Int): Double {
        var totalIncome = 0.0
        
        val groupLootGroups = GroupLootGroups.selectAll()
            .andWhere { GroupLootGroups.npcId eq npcId }
            .toList()

        for (group in groupLootGroups) {
            val groupId = group[GroupLootGroups.id]
            val groupChance = group[GroupLootGroups.groupChance]
            
            val groupItems = GroupLootItems.selectAll()
                .andWhere { GroupLootItems.groupId eq groupId }
                .toList()
            
            // Calculate category-based drops (only ONE item from each group can drop)
            val groupIncome = calculateCategoryBasedIncome(groupItems)
            
            totalIncome += (groupChance / 100.0) * groupIncome
        }
        
        return totalIncome
    }
    
    private fun calculateCategoryBasedIncome(groupItems: List<ResultRow>): Double {
        if (groupItems.isEmpty()) return 0.0
        
        val allItems = mutableListOf<Triple<Double, Double, String>>() // (balancedChance, itemValue, itemName)
        
        for (item in groupItems) {
            val itemName = item[GroupLootItems.item]
            val itemChance = item[GroupLootItems.chance]
            val minCount = item[GroupLootItems.minCount]
            val maxCount = item[GroupLootItems.maxCount]
            
            val averageCount = if (minCount == maxCount) {
                minCount.toDouble()
            } else {
                (minCount + maxCount) / 2.0
            }
            
            // Apply balanced chance formula: original chance is already in percentage, cap at 100
            val balancedChance = minOf(itemChance, 100.0)
            
            // Calculate item value (adena value or sell price * count)
            val itemValue = if (itemName.lowercase() == "adena") {
                averageCount
            } else {
                val itemPrice = getItemPrice(itemName)
                if (itemPrice > 0) {
                    itemPrice * averageCount
                } else {
                    0.0
                }
            }
            
            allItems.add(Triple(balancedChance, itemValue, itemName))
        }
        
        var totalIncome = 0.0
        if (allItems.isNotEmpty()) {
            val totalBalancedChance = maxOf(allItems.sumOf { it.first }, 100.0)
            
            for ((balancedChance, itemValue, _) in allItems) {
                val realChance = balancedChance / totalBalancedChance
                totalIncome += realChance * itemValue
            }
        }
        
        return totalIncome
    }

    private fun getItemPrice(itemName: String): Double {
        val priceRow = SellableItems.selectAll()
            .andWhere { SellableItems.item eq itemName }
            .singleOrNull()
        
        return priceRow?.get(SellableItems.price)?.toDouble() ?: 0.0
    }
}

class NpcsList : CliktCommand(name = "npcs", help = "List NPCs") {
    private val nameLike by option("--name", help = "Substring match on NPC name")
    private val minLevel by option("--min-level").convert { it.toInt() }.check("min-level must be >= 0") { it >= 0 }
    private val maxLevel by option("--max-level").convert { it.toInt() }.check("max-level must be >= 0") { it >= 0 }
    private val limit by option("--limit").convert { it.toInt() }.default(50).check("limit must be > 0") { it > 0 }

    override fun run() {
        transaction {
            var query = Npcs.selectAll()
            if (!nameLike.isNullOrBlank()) {
                query = query.andWhere { Npcs.name like "%${nameLike!!}%" }
            }
            if (minLevel != null) {
                query = query.andWhere { Npcs.level greaterEq minLevel!! }
            }
            if (maxLevel != null) {
                query = query.andWhere { Npcs.level lessEq maxLevel!! }
            }
            val rows = query.limit(limit).toList()
            rows.forEach { r ->
                println("npc_id=${r[Npcs.id]} level=${r[Npcs.level]} name='${r[Npcs.name]}' type='${r[Npcs.npcBegin]}'")
            }
        }
    }
}

class CorpseLootList : CliktCommand(name = "corpse-loot", help = "List corpse loot entries") {
    private val npcId by option("--npc-id").convert { it.toInt() }
    private val npcName by option("--npc-name")
    private val itemLike by option("--item")
    private val limit by option("--limit").convert { it.toInt() }.default(50)

    override fun run() {
        transaction {
            var q =
                CorpseLoot.join(Npcs, JoinType.INNER, additionalConstraint = { CorpseLoot.npcId eq Npcs.id }).select(
                    CorpseLoot.id,
                    CorpseLoot.npcId,
                    Npcs.name,
                    CorpseLoot.item,
                    CorpseLoot.chance,
                    CorpseLoot.minCount,
                    CorpseLoot.maxCount
                )
            if (npcId != null) q = q.andWhere { CorpseLoot.npcId eq npcId!! }
            if (!npcName.isNullOrBlank()) q = q.andWhere { Npcs.name like "%${npcName!!}%" }
            if (!itemLike.isNullOrBlank()) q = q.andWhere { CorpseLoot.item like "%${itemLike!!}%" }
            q.limit(limit).forEach { r ->
                println("id=${r[CorpseLoot.id]} npc_id=${r[CorpseLoot.npcId]} npc='${r[Npcs.name]}' item='${r[CorpseLoot.item]}' chance=${r[CorpseLoot.chance]} count=${r[CorpseLoot.minCount]}..${r[CorpseLoot.maxCount]}")
            }
        }
    }
}

class GroupLootGroupsList : CliktCommand(name = "group-loot-groups", help = "List group loot groups") {
    private val npcId by option("--npc-id").convert { it.toInt() }
    private val npcName by option("--npc-name")
    private val limit by option("--limit").convert { it.toInt() }.default(50)

    override fun run() {
        transaction {
            var q =
                GroupLootGroups.join(Npcs, JoinType.INNER, additionalConstraint = { GroupLootGroups.npcId eq Npcs.id })
                    .select(
                        GroupLootGroups.id, GroupLootGroups.npcId, Npcs.name, GroupLootGroups.groupChance
                    )
            if (npcId != null) q = q.andWhere { GroupLootGroups.npcId eq npcId!! }
            if (!npcName.isNullOrBlank()) q = q.andWhere { Npcs.name like "%${npcName!!}%" }
            q.limit(limit).forEach { r ->
                println("group_id=${r[GroupLootGroups.id]} npc_id=${r[GroupLootGroups.npcId]} npc='${r[Npcs.name]}' group_chance=${r[GroupLootGroups.groupChance]}")
            }
        }
    }
}

class GroupLootItemsList : CliktCommand(name = "group-loot-items", help = "List items within group loot") {
    private val groupId by option("--group-id").convert { it.toInt() }
    private val itemLike by option("--item")
    private val limit by option("--limit").convert { it.toInt() }.default(50)

    override fun run() {
        transaction {
            var q = GroupLootItems.selectAll()
            if (groupId != null) q = q.andWhere { GroupLootItems.groupId eq groupId!! }
            if (!itemLike.isNullOrBlank()) q = q.andWhere { GroupLootItems.item like "%${itemLike!!}%" }
            q.limit(limit).forEach { r ->
                println("id=${r[GroupLootItems.id]} group_id=${r[GroupLootItems.groupId]} item='${r[GroupLootItems.item]}' chance=${r[GroupLootItems.chance]} count=${r[GroupLootItems.minCount]}..${r[GroupLootItems.maxCount]}")
            }
        }
    }
}

class GetItemPrices : CliktCommand(name = "get-item-prices", help = "Get List of Items prices as well as individual items, could process multiple items item1,item2,item3") {
    private val itemAbbr by option("--item-abbr")
        .help("Item abbreviations separated by comma. If not provided, shows all items.")

    override fun run() {
        try {
            transaction {
                val rows = if (itemAbbr == null) {
                    SellableItems.selectAll().toList()
                } else {
                    val parsedAbbr: List<String> = parseItemKeys(itemAbbr!!)

                    if (parsedAbbr.isEmpty()) {
                        println("Error: No valid item abbreviations found")
                        return@transaction
                    }

                    val itemKeys: List<String> = parsedAbbr
                        .mapNotNull { abbr ->
                            AbbreviationToItemKeyMap[abbr] ?: run {
                                println("Warning: Unknown abbreviation '$abbr' - skipping")
                                null
                            }
                        }

                    if (itemKeys.isEmpty()) {
                        println("Error: No valid item keys found for given abbreviations")
                        return@transaction
                    }

                    SellableItems.selectAll()
                        .andWhere { 
                            itemKeys.map { itemKey -> 
                                SellableItems.item eq itemKey 
                            }.reduce { acc, condition -> acc or condition }
                        }
                        .toList()
                }

                displayResults(rows, itemAbbr)
            }

        } catch (e: Exception) {
            println("Error processing request: ${e.message}")
        }
    }

    private fun parseItemKeys(input: String): List<String> {
        return input.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun displayResults(rows: List<ResultRow>, originalInput: String?) {
        if (rows.isEmpty()) {
            println("No items found")
            return
        }

        rows.forEach { row ->
            val itemName = row[SellableItems.item]
            val itemKey = AbbreviationToItemKeyMap.getKeyByValue(itemName)
            val price = row[SellableItems.price]

            println("$itemName ($itemKey): $price")
        }
    }
}

fun buildCli(): CliktCommand = Root().subcommands(
    NpcsList(),
    CorpseLootList(),
    GroupLootGroupsList(),
    GroupLootItemsList(),
    UpdatePricesCommand(),
    FarmAnalysis(),
    GetItemPrices()
)
