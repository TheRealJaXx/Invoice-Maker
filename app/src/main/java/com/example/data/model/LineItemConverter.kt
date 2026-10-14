package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object LineItemConverter {
    fun toJson(items: List<LineItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("description", item.description)
                put("quantity", item.quantity)
                put("unitPrice", item.unitPrice)
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
                list.add(
                    LineItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        description = obj.optString("description", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty list on parse error
        }
        return list
    }
}
