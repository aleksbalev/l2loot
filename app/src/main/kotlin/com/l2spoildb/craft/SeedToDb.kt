package com.l2spoildb.craft

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.jdbc.selectAll

object RecipesSeeder {
    
    fun seedEquipmentRecipes(data: EquipmentRecipesData) {
        transaction {
            SchemaUtils.create(Recipes, RecipeMaterials, ItemRawMaterials)
            
            println("Seeding ${data.grade} grade equipment recipes...")
            
            data.recipes.forEach { recipe ->
                // Insert recipe
                Recipes.insert {
                    it[id] = recipe.id
                    it[name] = recipe.name
                    it[grade] = data.grade.name
                    it[level] = recipe.level
                    it[successRate] = recipe.successRate
                    it[itemId] = recipe.itemId
                    it[crystalCount] = recipe.crystalCount
                }
                
                // Insert materials
                recipe.material.forEach { material ->
                    RecipeMaterials.insert {
                        it[recipeId] = recipe.id
                        it[materialType] = "material"
                        it[itemName] = material.item
                        it[quantity] = material.quantity
                    }
                }
                
                // Insert products
                recipe.product.forEach { product ->
                    RecipeMaterials.insert {
                        it[recipeId] = recipe.id
                        it[materialType] = "product"
                        it[itemName] = product.item
                        it[quantity] = product.quantity
                    }
                }
            }
            
            println("Seeded ${data.recipes.size} ${data.grade} grade recipes")
            
            // Process equipment recipes to calculate raw materials
            processEquipmentRecipesToRawMaterials(data)
        }
    }
    
    fun seedMaterialRecipes(data: MaterialRecipeData) {
        transaction {
            SchemaUtils.create(Recipes, RecipeMaterials, ItemRawMaterials)
            
            println("Seeding material recipes...")
            
            data.recipes.forEach { recipe ->
                // Insert recipe (no grade for materials)
                Recipes.insert {
                    it[id] = recipe.id
                    it[name] = recipe.name
                    it[grade] = null
                    it[level] = recipe.level
                    it[successRate] = recipe.successRate
                    it[itemId] = recipe.itemId
                    it[crystalCount] = null
                }
                
                // Insert materials
                recipe.material.forEach { material ->
                    RecipeMaterials.insert {
                        it[recipeId] = recipe.id
                        it[materialType] = "material"
                        it[itemName] = material.item
                        it[quantity] = material.quantity
                    }
                }
                
                // Insert products
                recipe.product.forEach { product ->
                    RecipeMaterials.insert {
                        it[recipeId] = recipe.id
                        it[materialType] = "product"
                        it[itemName] = product.item
                        it[quantity] = product.quantity
                    }
                }
                
                // Insert pre-calculated raw materials
                val productName = recipe.product.first().item
                recipe.rawMaterials.forEach { rawMaterial ->
                    ItemRawMaterials.insert {
                        it[itemName] = productName
                        it[rawMaterialName] = rawMaterial.item
                        it[totalQuantity] = rawMaterial.quantity
                    }
                }
            }
            
            println("Seeded ${data.recipes.size} material recipes")
        }
    }
    
    fun seedBasicMaterials() {
        transaction {
            SchemaUtils.create(BasicMaterials)
            
            println("Seeding basic materials...")
            
            // Clear existing basic materials
            BasicMaterials.deleteWhere { BasicMaterials.id greaterEq 1 }
            
            // Spoilable basic materials
            val spoilableMaterials = listOf(
                "animal_bone", "animal_skin", "coal", "iron_ore", "stem", "suede",
                "thread", "varnish", "charcoal", "silver_nugget", "mithril_ore",
                "oriharukon_ore", "stone_of_purity", "asofe", "enria", "mold_glue",
                "mold_hardener", "mold_lubricant", "thons", "admantite_nugget"
            )
            
            // Non-spoilable basic materials
            val nonSpoilableMaterials = listOf(
                "spirit_ore", "soul_ore", "gemstone_d", "gemstone_c", "gemstone_b", 
                "gemstone_a", "gemstone_s"
            )
            
            // Insert spoilable materials
            spoilableMaterials.forEach { materialName ->
                BasicMaterials.insert {
                    it[itemName] = materialName
                    it[isRawMaterial] = true
                    it[isSpoilable] = true
                }
            }
            
            // Insert non-spoilable materials
            nonSpoilableMaterials.forEach { materialName ->
                BasicMaterials.insert {
                    it[itemName] = materialName
                    it[isRawMaterial] = true
                    it[isSpoilable] = false
                }
            }
            
            println("Seeded ${spoilableMaterials.size} spoilable and ${nonSpoilableMaterials.size} non-spoilable basic materials")
        }
    }
    
    private fun processEquipmentRecipesToRawMaterials(data: EquipmentRecipesData) {
        transaction {
            println("Processing ${data.grade} grade equipment recipes to raw materials...")
            
            data.recipes.forEach { recipe ->
                val productName = recipe.product.first().item
                val rawMaterialsMap = mutableMapOf<String, Int>()
                
                recipe.material.forEach { material ->
                    processRecipeMaterialToRaw(material.item, material.quantity, rawMaterialsMap)
                }
                
                rawMaterialsMap.forEach { (materialName, totalQuantity) ->
                    val existingEntry = ItemRawMaterials
                        .selectAll()
                        .where { 
                            (ItemRawMaterials.itemName eq productName) and
                            (ItemRawMaterials.rawMaterialName eq materialName) 
                        }
                        .firstOrNull()
                        
                    if (existingEntry == null) {
                        ItemRawMaterials.insert {
                            it[itemName] = productName
                            it[rawMaterialName] = materialName
                            it[ItemRawMaterials.totalQuantity] = totalQuantity
                        }
                    }
                }
            }
            
            println("Processed ${data.recipes.size} ${data.grade} grade equipment recipes")
        }
    }
    
    private fun processRecipeMaterialToRaw(materialName: String, quantity: Int, rawMaterialsMap: MutableMap<String, Int>) {
        val isBasicMaterial = BasicMaterials
            .selectAll()
            .where { BasicMaterials.itemName eq materialName }
            .firstOrNull() != null
            
        if (isBasicMaterial) {
            rawMaterialsMap[materialName] = rawMaterialsMap.getOrDefault(materialName, 0) + quantity
        } else {
            val rawMaterials = ItemRawMaterials
                .selectAll()
                .where { ItemRawMaterials.itemName eq materialName }
                .toList()
                
            if (rawMaterials.isNotEmpty()) {
                rawMaterials.forEach { row ->
                    val rawMaterialName = row[ItemRawMaterials.rawMaterialName]
                    val rawQuantity = row[ItemRawMaterials.totalQuantity]
                    val totalNeeded = rawQuantity * quantity
                    
                    rawMaterialsMap[rawMaterialName] = rawMaterialsMap.getOrDefault(rawMaterialName, 0) + totalNeeded
                }
            } else {
                rawMaterialsMap[materialName] = rawMaterialsMap.getOrDefault(materialName, 0) + quantity
            }
        }
    }
    
    fun clearRecipeData() {
        transaction {
            BasicMaterials.deleteWhere { BasicMaterials.id greaterEq 1 }
            RecipeMaterials.deleteWhere { RecipeMaterials.id greaterEq 1 }
            ItemRawMaterials.deleteWhere { ItemRawMaterials.id greaterEq 1 }
            Recipes.deleteWhere { Recipes.id greaterEq 1 }
            println("Cleared existing recipe data")
        }
    }
}