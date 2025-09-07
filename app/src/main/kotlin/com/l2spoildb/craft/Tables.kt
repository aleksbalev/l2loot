package com.l2spoildb.craft

import org.jetbrains.exposed.v1.core.Table

object Recipes : Table("recipes") {
    val id = integer("id")
    val name = varchar("name", 128)
    val grade = varchar("grade", 1).nullable() // "D", "C", "B", "A", "S"
    val level = integer("level")
    val successRate = integer("success_rate")
    val itemId = integer("item_id")
    val crystalCount = integer("crystal_count").nullable()
    override val primaryKey = PrimaryKey(id)
}

object RecipeMaterials : Table("recipe_materials") {
    val id = integer("id").autoIncrement()
    val recipeId = integer("recipe_id")
    val materialType = varchar("material_type", 16) // "material", "catalyst", "product"
    val itemName = varchar("item_name", 128)
    val quantity = integer("quantity")
    override val primaryKey = PrimaryKey(id)
}

object ItemRawMaterials : Table("item_raw_materials") {
    val id = integer("id").autoIncrement()
    val itemName = varchar("item_name", 128)
    val rawMaterialName = varchar("raw_material_name", 128)
    val totalQuantity = integer("total_quantity")
    override val primaryKey = PrimaryKey(id)
}

object BasicMaterials : Table("basic_materials") {
    val id = integer("id").autoIncrement()
    val itemName = varchar("item_name", 128)
    val isRawMaterial = bool("is_raw_material").default(true)
    val isSpoilable = bool("is_spoilable").default(false)
    override val primaryKey = PrimaryKey(id)
}