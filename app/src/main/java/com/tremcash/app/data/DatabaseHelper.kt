package com.tremcash.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// MUDANÇA IMPORTANTE: Versão alterada para 4 para ativar o group_id
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "tremcash.db", null, 4) {
    override fun onCreate(db: SQLiteDatabase) {
        // 1. Tabela de Usuários
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, password TEXT)")

        // 2. Tabela de Categorias
        db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, type TEXT)")

        // 3. Tabela de Transações atualizada com group_id
        db.execSQL("""
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                category_id INTEGER,
                group_id TEXT, 
                type TEXT,
                description TEXT,
                value REAL,
                date TEXT,
                repetition TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id),
                FOREIGN KEY(category_id) REFERENCES categories(id)
            )
        """)

        // 4. Inserir Usuário Padrão
        db.execSQL("INSERT INTO users (id, name, email, password) VALUES (1, 'Paulo Henrique', 'paulo@tremcash.com', '123')")

        // 5. Inserir todas as categorias que você pediu
        val categories = listOf(
            "('Salário', 'RECEITA')",
            "('Investimentos', 'RECEITA')",
            "('Lanches', 'DESPESA')",
            "('Café', 'DESPESA')",
            "('Transporte', 'DESPESA')",
            "('Casa', 'DESPESA')",
            "('Saúde', 'DESPESA')",
            "('Dentista', 'DESPESA')",
            "('Combustível', 'DESPESA')",
            "('Mercado', 'DESPESA')",
            "('Assinatura', 'DESPESA')",
            "('Lazer', 'DESPESA')"
        )
        categories.forEach { db.execSQL("INSERT INTO categories (name, type) VALUES $it") }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        // Se a versão mudar, ele apaga tudo e cria de novo do zero (com a nova estrutura)
        db.execSQL("DROP TABLE IF EXISTS transactions")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }
}