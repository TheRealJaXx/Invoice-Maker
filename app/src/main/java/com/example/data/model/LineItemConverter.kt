package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object LineItemConverter {
    fun toJson(items: List<LineItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("project", item.project)
                put("service", item.service)
                put("dateMode", item.dateMode)
                put("startDate", item.startDate)
                put("endDate", item.endDate)
                put("dates", item.dates)
                put("days", item.days)
                put("dailyRate", item.dailyRate)
                put("notes", item.notes)

                // Serialize separate days list if any
                if (item.separateDays.isNotEmpty()) {
                    val daysArr = JSONArray()
                    for (d in item.separateDays) {
                        val dayObj = JSONObject().apply {
                            put("date", d.date)
                            put("rate", d.rate)
                            put("label", d.label)
                        }
                        daysArr.put(dayObj)
                    }
                    put("separateDays", daysArr)
                }

                // Fallbacks for compatibility
                put("description", item.description)
                put("quantity", item.quantity)
                put("unitPrice", item.unitPrice)
                put("total", item.total)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(jsonStr: String): List<LineItem> {
        val list = mutableListOf<LineItem>()
        if (jsonStr.isBlank()) return list
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val project = obj.optString("project", "")
                val service = obj.optString("service", "")
                val legacyDesc = obj.optString("description", "")

                val finalService = when {
                    service.isNotBlank() -> service
                    legacyDesc.contains(" • ") -> legacyDesc.substringBefore(" • ").trim()
                    else -> legacyDesc
                }
                val finalProject = when {
                    project.isNotBlank() -> project
                    legacyDesc.contains(" • ") -> legacyDesc.substringAfter(" • ").trim()
                    else -> ""
                }

                val finalDays = if (obj.has("days")) obj.optDouble("days", 1.0) else obj.optDouble("quantity", 1.0)
                val finalRate = if (obj.has("dailyRate")) obj.optDouble("dailyRate", 0.0) else obj.optDouble("unitPrice", 0.0)

                val separateDaysList = mutableListOf<DayRate>()
                if (obj.has("separateDays")) {
                    val daysArr = obj.getJSONArray("separateDays")
                    for (dIdx in 0 until daysArr.length()) {
                        val dObj = daysArr.getJSONObject(dIdx)
                        separateDaysList.add(
                            DayRate(
                                date = dObj.optString("date", ""),
                                rate = dObj.optDouble("rate", 0.0),
                                label = dObj.optString("label", "")
                            )
                        )
                    }
                }

                list.add(
                    LineItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        project = finalProject,
                        service = finalService,
                        dateMode = obj.optString("dateMode", if (separateDaysList.isNotEmpty()) "SEPARATE" else "RANGE"),
                        startDate = obj.optString("startDate", ""),
                        endDate = obj.optString("endDate", ""),
                        dates = obj.optString("dates", ""),
                        days = if (finalDays <= 0.0) 1.0 else finalDays,
                        dailyRate = finalRate,
                        separateDays = separateDaysList,
                        notes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty list on parse error
        }
        return list
    }
}
