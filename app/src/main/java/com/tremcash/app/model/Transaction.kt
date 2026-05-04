package com.tremcash.app.model

data class Transaction(
    val id: Int = 0,
    val groupId: String = "", // Identificador do grupo de parcelas
    val type: TransactioType,
    val description: String,
    val value: Double,
    val date: String,
    val categoryName: String,
    val repetition: String
)