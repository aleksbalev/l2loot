package com.l2spoildb.craft

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.check
import com.l2spoildb.loot.SellableItems
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.math.roundToInt

data class CrystalEfficiencyResult(
    val itemName: String,
    val recipeId: Int,
    val crystalsGained: Int,
    val crystalsUsed: Int,
    val netCrystals: Int,
    val rawMaterialCost: Int,
    val pricePerCrystal: Double
)

class CrystalAnalysisCommand : CliktCommand(
    name = "crystal-analysis", 
    help = "Find the cheapest recipes to get crystals by grade"
) {
    private val grade by option("--grade", help = "Crystal grade (D, C, B, A, S)")
        .check("Grade must be D, C, B, A, or S") { it.uppercase() in listOf("D", "C", "B", "A", "S") }
    
    private val limit by option("--limit", help = "Number of results to show")
        .convert { it.toInt() }.default(5).check("Limit must be > 0") { it > 0 }

    override fun run() {
        if (grade == null) {
            println("Please specify a grade using --grade D/C/B/A/S")
            return
        }
        
        val results = calculateCrystalEfficiency(grade!!.uppercase())
        displayResults(results.take(limit), grade!!.uppercase())
    }
    
    private fun calculateCrystalEfficiency(grade: String): List<CrystalEfficiencyResult> {
        return transaction {
            val recipesWithCrystals = Recipes
                .selectAll()
                .where { 
                    (Recipes.grade eq grade) and 
                    (Recipes.crystalCount.isNotNull()) and
                    (Recipes.crystalCount greater 0)
                }
                .toList()
                
            recipesWithCrystals.mapNotNull { recipe ->
                val recipeId = recipe[Recipes.id]
                val itemName = recipe[Recipes.name].removePrefix("mk_")
                val crystalsGained = recipe[Recipes.crystalCount] ?: 0
                
                if (crystalsGained <= 0) return@mapNotNull null
                
                val crystalsUsed = RecipeMaterials
                    .selectAll()
                    .where { 
                        (RecipeMaterials.recipeId eq recipeId) and
                        (RecipeMaterials.materialType eq "material") and
                        (RecipeMaterials.itemName like "crystal_%")
                    }
                    .sumOf { it[RecipeMaterials.quantity] }
                
                val netCrystals = crystalsGained - crystalsUsed
                if (netCrystals <= 0) return@mapNotNull null
                
                val rawMaterialCost = calculateRawMaterialCost(itemName)
                if (rawMaterialCost <= 0) return@mapNotNull null
                
                val pricePerCrystal = rawMaterialCost.toDouble() / netCrystals
                
                CrystalEfficiencyResult(
                    itemName = itemName,
                    recipeId = recipeId,
                    crystalsGained = crystalsGained,
                    crystalsUsed = crystalsUsed,
                    netCrystals = netCrystals,
                    rawMaterialCost = rawMaterialCost,
                    pricePerCrystal = pricePerCrystal
                )
            }
            .sortedBy { it.pricePerCrystal }
        }
    }
    
    private fun calculateRawMaterialCost(itemName: String): Int {
        return transaction {
            ItemRawMaterials
                .join(SellableItems, JoinType.LEFT, 
                      ItemRawMaterials.rawMaterialName, SellableItems.item)
                .selectAll()
                .where { ItemRawMaterials.itemName eq itemName }
                .sumOf { row ->
                    val quantity = row[ItemRawMaterials.totalQuantity]
                    val price = row.getOrNull(SellableItems.price) ?: 0
                    val materialName = row[ItemRawMaterials.rawMaterialName]
                    
                    quantity * price
                }
        }
    }
    
    private fun displayResults(results: List<CrystalEfficiencyResult>, grade: String) {
        if (results.isEmpty()) {
            println("No crystal recipes found for grade $grade")
            return
        }
        
        println("Top ${results.size} cheapest $grade-grade crystal sources:")
        println("=".repeat(80))
        
        results.forEachIndexed { index, result ->
            println("${index + 1}. ${formatItemName(result.itemName)}")
            println("   Crystal price per unit: ${result.pricePerCrystal.roundToInt()} adena")
            println("   Overall crystals count: ${result.crystalsGained}")
            println("   Net crystals count: ${result.netCrystals}")
            println("   Raw materials price: ${formatAdena(result.rawMaterialCost)}")
            if (result.crystalsUsed > 0) {
                println("   Crystals used in recipe: ${result.crystalsUsed}")
            }
            println()
        }
        
        println("💡 Tip: Lower 'crystal price per unit' = better crystal farming efficiency!")
    }
    
    private fun formatItemName(name: String): String {
        return name.replace("_", " ").split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
    
    private fun formatAdena(amount: Int): String {
        return when {
            amount >= 1_000_000 -> "${(amount / 1_000_000.0).roundToInt()}M adena"
            amount >= 1_000 -> "${(amount / 1_000.0).roundToInt()}K adena" 
            else -> "$amount adena"
        }
    }
}
