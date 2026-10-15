package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val company: String = "",
    val email: String,
    val phone: String = "",
    val address: String = "",
    val cityStateZip: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val initials: String
        get() {
            val parts = name.trim().split("\\s+".toRegex())
            return when {
                parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
                name.isNotBlank() -> name.take(2).uppercase()
                company.isNotBlank() -> company.take(2).uppercase()
                else -> "CL"
            }
        }
}
