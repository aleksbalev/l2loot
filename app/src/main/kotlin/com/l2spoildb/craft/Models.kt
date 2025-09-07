package com.l2spoildb.craft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EquipmentRecipesData(
    val grade: Grade,
    val recipes: List<EquipmentRecipe>
)

@Serializable
data class EquipmentRecipe(
    val id: Int,
    val name: String,
    val level: Int,
    val material: List<MaterialComponent>,
    val product: List<MaterialComponent>,
    @SerialName("success_rate") val successRate: Int,
    @SerialName("item_id") val itemId: Int,
    @SerialName("crystal_count") val crystalCount: Int? = null
)

@Serializable
data class MaterialRecipeData(
    @SerialName("materials-recipes") val recipes: List<MaterialRecipe>
)

@Serializable
data class MaterialRecipe(
    val id: Int,
    val name: String,
    val level: Int,
    val material: List<MaterialComponent>,
    @SerialName("raw_materials") val rawMaterials: List<MaterialComponent>,
    val product: List<MaterialComponent>,
    @SerialName("success_rate") val successRate: Int,
    @SerialName("item_id") val itemId: Int
)

@Serializable
data class MaterialComponent(
    val item: String,
    val quantity: Int,
)

@Serializable
enum class Grade {
    D, C, B, A, S
}