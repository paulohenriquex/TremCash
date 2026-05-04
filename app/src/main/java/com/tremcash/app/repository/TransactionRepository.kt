package com.tremcash.app.repository

import android.content.ContentValues
import com.tremcash.app.data.DatabaseHelper
import com.tremcash.app.model.Transaction
import com.tremcash.app.model.TransactioType
import java.text.SimpleDateFormat
import java.util.*

class TransactionRepository(private val dbHelper: DatabaseHelper) {

    fun insertTransaction(transaction: Transaction, installments: Int) {
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        sdf.parse(transaction.date)?.let { calendar.time = it }

        // --- NOVA LÓGICA: Descobrir o ID da categoria pelo nome ---
        var categoryId = 1 // Padrão caso não encontre
        val cursor = db.rawQuery("SELECT id FROM categories WHERE name = ?", arrayOf(transaction.categoryName))
        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(0)
        }
        cursor.close()
        // ---------------------------------------------------------

        val uniqueGroupId = UUID.randomUUID().toString()

        for (i in 0 until installments) {
            val values = ContentValues().apply {
                put("user_id", 1)
                put("group_id", uniqueGroupId)
                put("type", transaction.type.name)
                val desc = if (installments > 1) "${transaction.description} (${i + 1}/$installments)" else transaction.description
                put("description", desc)
                put("value", transaction.value)
                put("date", sdf.format(calendar.time))
                put("category_id", categoryId) // AGORA USA O ID CORRETO
                put("repetition", transaction.repetition)
            }
            db.insert("transactions", null, values)
            calendar.add(Calendar.MONTH, 1)
        }
    }

    fun getAllTransactions(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT t.*, c.name FROM transactions t LEFT JOIN categories c ON t.category_id = c.id", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Transaction(
                    id = cursor.getInt(0),
                    groupId = cursor.getString(3) ?: "",
                    type = TransactioType.valueOf(cursor.getString(4)),
                    description = cursor.getString(5),
                    value = cursor.getDouble(6),
                    date = cursor.getString(7),
                    categoryName = cursor.getString(9) ?: "Geral",
                    repetition = cursor.getString(8)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // Deleta apenas uma parcela
    fun deleteTransaction(id: Int) {
        dbHelper.writableDatabase.delete("transactions", "id = ?", arrayOf(id.toString()))
    }

    // Deleta o grupo inteiro
    fun deleteTransactionGroup(groupId: String) {
        dbHelper.writableDatabase.delete("transactions", "group_id = ?", arrayOf(groupId))
    }

    fun updateTransaction(transaction: Transaction) {
        val values = ContentValues().apply {
            put("description", transaction.description)
            put("value", transaction.value)
            put("date", transaction.date)
        }
        dbHelper.writableDatabase.update("transactions", values, "id = ?", arrayOf(transaction.id.toString()))
    }
}