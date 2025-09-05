package com.l2spoildb.loot

import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

object LootLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    fun loadFromFile(path: Path): NpcLootData {
        Files.newBufferedReader(path).use { reader ->
            return json.decodeFromString(NpcLootData.serializer(), reader.readText())
        }
    }

    fun loadSellableItemsFromFile(path: Path): Items {
        Files.newBufferedReader(path).use { reader ->
            return json.decodeFromString(Items.serializer(), reader.readText())
        }
    }
}
