package com.example.data.local

import android.content.Context

object CustomOptionsPrefs {
    private const val PREFS_NAME = "custom_neighborhood_options"
    const val KEY_STORE_CATS = "custom_store_categories"
    const val KEY_SERVICE_CATS = "custom_service_categories"
    const val KEY_ORDER_TYPES = "custom_order_types"
    const val KEY_VEHICLE_TYPES = "custom_vehicle_types"
    const val KEY_DELIVERY_ZONES = "custom_delivery_zones"

    fun getCustomOptions(context: Context, key: String): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(key, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|||").filter { it.isNotBlank() }
    }

    fun addCustomOption(context: Context, key: String, option: String) {
        val trimmed = option.trim()
        if (trimmed.isBlank()) return
        val current = getCustomOptions(context, key).toMutableList()
        if (!current.contains(trimmed)) {
            current.add(trimmed)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(key, current.joinToString("|||")).apply()
        }
    }
}
