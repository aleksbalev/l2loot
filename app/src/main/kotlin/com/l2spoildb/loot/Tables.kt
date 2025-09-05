package com.l2spoildb.loot

import org.jetbrains.exposed.v1.core.Table

object Npcs : Table("npcs") {
    val id = integer("id").autoIncrement()
    val level = integer("level")
    val name = varchar("name", 128)
    val npcBegin = varchar("npc_begin", 64)
    override val primaryKey = PrimaryKey(id)
}

object CorpseLoot : Table("corpse_loot") {
    val id = integer("id").autoIncrement()
    val npcId = integer("npc_id")
    val item = varchar("item", 128)
    val chance = double("chance")
    val minCount = integer("min_count")
    val maxCount = integer("max_count")
    override val primaryKey = PrimaryKey(id)
}

object GroupLootGroups : Table("group_loot_groups") {
    val id = integer("id").autoIncrement()
    val npcId = integer("npc_id")
    val groupChance = double("group_chance")
    override val primaryKey = PrimaryKey(id)
}

object GroupLootItems : Table("group_loot_items") {
    val id = integer("id").autoIncrement()
    val groupId = integer("group_id")
    val item = varchar("item", 128)
    val chance = double("chance")
    val minCount = integer("min_count")
    val maxCount = integer("max_count")
    override val primaryKey = PrimaryKey(id)
}

object SellableItems : Table("sellable_items") {
    val id = integer("id").autoIncrement()
    val item = varchar("item", 128)
    val price = integer("price")
    override val primaryKey = PrimaryKey(id)
}
