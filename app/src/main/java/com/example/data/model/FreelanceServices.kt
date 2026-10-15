package com.example.data.model

import android.content.Context
import android.content.SharedPreferences

object FreelanceServices {
    val DEFAULT_SERVICES = listOf(
        "vMix Operator",
        "Vision Mixer",
        "Streaming",
        "Technical Director",
        "TD/Vision Mixer",
        "PTZ Camera Operator",
        "Network Specialist"
    )

    private const val PREFS_NAME = "freelance_services_prefs"
    private const val KEY_CUSTOM_SERVICES = "custom_services"

    fun getAllServices(context: Context?): List<String> {
        if (context == null) return DEFAULT_SERVICES
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val custom = prefs.getStringSet(KEY_CUSTOM_SERVICES, emptySet()) ?: emptySet()
        val combined = LinkedHashSet<String>()
        combined.addAll(DEFAULT_SERVICES)
        combined.addAll(custom.filter { it.isNotBlank() })
        return combined.toList()
    }

    fun addCustomService(context: Context, newService: String): List<String> {
        val trimmed = newService.trim()
        if (trimmed.isBlank()) return getAllServices(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_CUSTOM_SERVICES, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(trimmed)
        prefs.edit().putStringSet(KEY_CUSTOM_SERVICES, current).apply()
        return getAllServices(context)
    }
}
