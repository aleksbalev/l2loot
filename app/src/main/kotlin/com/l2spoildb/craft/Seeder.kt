package com.l2spoildb.craft

import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

object Seeder {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    fun loadGradeRecipesFromFile(path: Path): EquipmentRecipesData {
        Files.newBufferedReader(path).use { reader ->
            return json.decodeFromString(EquipmentRecipesData.serializer(), reader.readText())
        }
    }

    fun loadMaterialRecipesFromFile(path: Path): MaterialRecipeData {
        Files.newBufferedReader(path).use { reader ->
            return json.decodeFromString(MaterialRecipeData.serializer(), reader.readText())
        }
    }
}