package com.l2spoildb.utils

val AbbreviationToItemKeyMap = mapOf(
    "an" to "admantite_nugget",
    "ab" to "animal_bone",
    "as" to "animal_skin",
    "asofe" to "asofe",
    "bh" to "braided_hemp",
    "charcoal" to "charcoal",
    "coal" to "coal",
    "cbp" to "coarse_bone_powder",
    "cokes" to "cokes",
    "cord" to "cord",
    "cl" to "crafted_leather",
    "dmp" to "reinforcing_plate",
    "eaa" to "scrl_of_ench_am_a",
    "eab" to "scrl_of_ench_am_b",
    "eac" to "scrl_of_ench_am_c",
    "ead" to "scrl_of_ench_am_d",
    "eas" to "scrl_of_ench_am_s",
    "enria" to "enria",
    "ewa" to "scrl_of_ench_wp_a",
    "ewb" to "scrl_of_ench_wp_b",
    "ewc" to "scrl_of_ench_wp_c",
    "ewd" to "scrl_of_ench_wp_d",
    "ews" to "scrl_of_ench_wp_s",
    "hgs" to "high_grade_suede",
    "io" to "iron_ore",
    "leather" to "leather",
    "mh" to "reinforcing_agent",
    "mf" to "metallic_fiber",
    "mt" to "iron_thread",
    "mo" to "mithril_ore",
    "mg" to "mold_glue",
    "moldh" to "mold_hardener",
    "ml" to "mold_lubricant",
    "oo" to "oriharukon_ore",
    "steel" to "steel",
    "sop" to "stone_of_purity",
    "thons" to "thons",
    "varnish" to "varnish",
    "thread" to "thread",
    "suede" to "suede",
    "stem" to "stem",
    "sn" to "silver_nugget",
)

fun <K, V> Map<K, V>.getKeyByValue(value: V): K? {
    return this.entries
        .firstOrNull { it.value == value }
        ?.key
}