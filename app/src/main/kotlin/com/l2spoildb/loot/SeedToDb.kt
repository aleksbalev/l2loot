package com.l2spoildb.loot

import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object NpcLootSeeder {
    fun seed(data: NpcLootData) {
        transaction {
            SchemaUtils.create(Npcs, CorpseLoot, GroupLootGroups, GroupLootItems)

            // Note: Schema is recreated in Root command, so always seed

            var npcCount = 0
            var corpseLootCount = 0
            var groupLootCount = 0
            
            data.npcs.forEach { npc ->
                npc.npcName.forEach { nameVal ->
                    npcCount++
                    val npcId = Npcs.insert {
                        it[level] = npc.level
                        it[name] = nameVal
                        it[npcBegin] = npc.npcBegin
                    } get Npcs.id

                    npc.corpseMakeList.forEach { loot ->
                        corpseLootCount++
                        CorpseLoot.insert {
                            it[this.npcId] = npcId
                            it[item] = loot.item
                            it[chance] = loot.chance
                            it[minCount] = loot.minCount
                            it[maxCount] = loot.maxCount
                        }
                    }

                    npc.additionalMakeMultiList.forEach { group ->
                        val groupId = GroupLootGroups.insert {
                            it[this.npcId] = npcId
                            it[groupChance] = group.groupChance
                        } get GroupLootGroups.id

                        group.items.forEach { li ->
                            groupLootCount++
                            GroupLootItems.insert {
                                it[this.groupId] = groupId
                                it[item] = li.item
                                it[chance] = li.chance
                                it[minCount] = li.minCount
                                it[maxCount] = li.maxCount
                            }
                        }
                    }
                }
            }
            
            println("Seeded: $npcCount NPCs, $corpseLootCount corpse loot items, $groupLootCount group loot items")
        }
    }
}
