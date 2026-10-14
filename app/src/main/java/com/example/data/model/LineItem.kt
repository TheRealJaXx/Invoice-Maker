package com.example.data.model

data class LineItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0
) {
    val total: Double
        get() = quantity * unitPrice

    val subtotal: Double
        get() = quantity * unitPrice
}
