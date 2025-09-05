package org.example.loot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NpcLootData(
    val npcs: List<NpcEntry>
)

@Serializable
data class NpcEntry(
    val level: Int,
    @SerialName("npc_name") val npcName: List<String>,
    @SerialName("npc_begin") val npcBegin: String,
    @SerialName("corpse_make_list") val corpseMakeList: List<LootItem> = emptyList(),
    @SerialName("additional_make_multi_list") val additionalMakeMultiList: List<LootGroup> = emptyList()
)

@Serializable
data class LootItem(
    val chance: Double,
    val item: String,
    @SerialName("max_count") val maxCount: Int,
    @SerialName("min_count") val minCount: Int
)

@Serializable
data class LootGroup(
    @SerialName("group_chance") val groupChance: Double,
    val items: List<LootItem>
)

@Serializable
data class Items(
    val items: List<SellableItem>
)

@Serializable
data class SellableItem(
    val item: String,
    val price: Int
)
