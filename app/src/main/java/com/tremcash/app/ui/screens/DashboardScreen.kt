package com.tremcash.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.*
import com.tremcash.app.model.Transaction
import com.tremcash.app.model.TransactioType
import com.tremcash.app.ui.components.CategoryChartCard
import com.tremcash.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    transactions: List<Transaction>,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onDailyExpense: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
    onDeleteSingle: (Int) -> Unit,
    onDeleteAll: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var transactionToOptions by remember { mutableStateOf<Transaction?>(null) }
    var quickValue by remember { mutableStateOf("") }
    var quickCategory by remember { mutableStateOf("Café") }

    val sdfMonth = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()) }
    val todayMonth = sdfMonth.format(Date())

    // Cálculo do Saldo Atual
    val currentBalance = transactions.filter { it.date <= "$todayMonth-31" }.sumOf {
        if (it.type == TransactioType.RECEITA) it.value else -it.value
    }

    // DIÁLOGO DE OPÇÕES
    if (transactionToOptions != null) {
        val repetition = transactionToOptions?.repetition ?: "Única"
        val isGroup = !transactionToOptions?.groupId.isNullOrEmpty()

        AlertDialog(
            onDismissRequest = { transactionToOptions = null },
            title = { Text("Opções do Lançamento") },
            text = { Text("O que deseja fazer com '${transactionToOptions?.description}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onEdit(transactionToOptions!!)
                    transactionToOptions = null
                }) {
                    Text("Editar", color = TremBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Column(horizontalAlignment = Alignment.End) {
                    if (isGroup && (repetition == "Parcelada" || repetition == "Fixa")) {
                        val buttonText = if (repetition == "Parcelada")
                            "Apagar todas as parcelas"
                        else
                            "Apagar todas as recorrências"

                        TextButton(onClick = {
                            onDeleteAll(transactionToOptions!!.groupId)
                            transactionToOptions = null
                        }) {
                            Text(buttonText, color = TremRed)
                        }
                    }

                    TextButton(onClick = {
                        onDeleteSingle(transactionToOptions!!.id)
                        transactionToOptions = null
                    }) {
                        Text("Apagar apenas esta", color = Color.Gray)
                    }
                }
            }
        )
    }

    Scaffold(
        containerColor = TremBackground,
        topBar = {
            Column(modifier = Modifier.background(TremBlue).padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) { Icon(Icons.Default.Menu, null, tint = Color.White) }
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text("Olá,", color = Color.White.copy(0.7f), fontSize = 14.sp)
                        Text("Paulo Henrique", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = {}) { Icon(Icons.Default.Notifications, null, tint = Color.White) }
                }
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showMenu) {
                    ExtendedFloatingActionButton(
                        onClick = { showMenu = false; onAddIncome() },
                        containerColor = TremBlue,
                        contentColor = Color.White,
                        icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, null) },
                        text = { Text("Receita") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ExtendedFloatingActionButton(
                        onClick = { showMenu = false; onAddExpense() },
                        containerColor = TremRed,
                        contentColor = Color.White,
                        icon = { Icon(Icons.AutoMirrored.Filled.TrendingDown, null) },
                        text = { Text("Despesa") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                FloatingActionButton(
                    onClick = { showMenu = !showMenu },
                    containerColor = TremBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(if (showMenu) Icons.Default.Close else Icons.Default.Add, null)
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

            // 1. ALERTA DE FERIADO
            item {
                val calendar = Calendar.getInstance()
                val isNearMayDay = calendar.get(Calendar.MONTH) == Calendar.APRIL &&
                        calendar.get(Calendar.DAY_OF_MONTH) >= 25

                if (isNearMayDay) {
                    Card(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Fix: Use AutoMirrored version for EventNote
                            Icon(Icons.AutoMirrored.Filled.EventNote, null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Entrega antecipada: Devido ao feriado de 01/05, organize seus lançamentos hoje!",
                                fontSize = 12.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 2. CARD DE SALDO
            item {
                Card(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Saldo Atual", color = Color.Gray, fontSize = 14.sp)
                        Text("R$ ${"%.2f".format(currentBalance)}", color = if (currentBalance < 0) TremRed else TremBlue, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. GRÁFICO DE CATEGORIAS
            item {
                val expensesMap = transactions
                    .filter { it.type == TransactioType.DESPESA && it.date.startsWith(todayMonth) }
                    .groupBy { it.categoryName }
                    .mapValues { entry -> entry.value.sumOf { it.value } }

                CategoryChartCard(expensesByCategory = expensesMap)
            }

            // 4. SEÇÃO DE DESPESA DIÁRIA
            item {
                Card(modifier = Modifier.padding(20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Despesa diária", fontWeight = FontWeight.Bold, color = TremRed, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = quickValue,
                                onValueChange = { quickValue = it },
                                label = { Text("Valor R$") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val v = quickValue.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    if (v > 0) {
                                        onDailyExpense(Transaction(type = TransactioType.DESPESA, description = quickCategory, value = v, date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()), categoryName = quickCategory, repetition = "Única"))
                                        quickValue = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(TremRed), modifier = Modifier.height(56.dp)
                            ) { Icon(Icons.Default.Check, null) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            listOf("Café", "Lanches", "Transporte", "Mercado", "Combustível").forEach { cat ->
                                FilterChip(selected = quickCategory == cat, onClick = { quickCategory = cat }, label = { Text(cat, fontSize = 12.sp) }, modifier = Modifier.padding(end = 8.dp))
                            }
                        }
                    }
                }
            }

            // 5. PROJEÇÃO 24 MESES
            item {
                Text(
                    "Projeção 24 Meses",
                    color = TremBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
                )
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items((0..23).toList()) { i ->
                        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, i) }
                        val monthStr = sdfMonth.format(cal.time)
                        val projBalance = transactions.filter { it.date <= "$monthStr-31" }.sumOf {
                            if (it.type == TransactioType.RECEITA) it.value else -it.value
                        }
                        ForecastCard(SimpleDateFormat("MMM/yy", Locale.getDefault()).format(cal.time), projBalance)
                    }
                }
            }

            // 6. ÚLTIMOS LANÇAMENTOS
            item {
                Text(
                    "Últimos Lançamentos",
                    color = TremBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
                )
            }

            items(transactions.reversed()) { tx ->
                TransactionListItem(tx) {
                    transactionToOptions = tx
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ForecastCard(month: String, balance: Double) {
    val isNeg = balance < 0
    Card(
        modifier = Modifier.width(140.dp).height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isNeg) Color.White else TremBlue),
        border = if (isNeg) BorderStroke(2.dp, TremRed) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(month, color = if (isNeg) TremRed else Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text("R$ ${"%.2f".format(balance)}", color = if (isNeg) TremRed else Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionListItem(tx: Transaction, onClick: () -> Unit) {
    val isInc = tx.type == TransactioType.RECEITA
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background((if (isInc) TremBlue else TremRed).copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(
                if (isInc) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                null,
                tint = if (isInc) TremBlue else TremRed
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(tx.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(tx.date, color = Color.Gray, fontSize = 12.sp)
        }
        Text("${if (isInc) "+" else "-"}R$ ${"%.2f".format(tx.value)}", color = if (isInc) TremBlue else TremRed, fontWeight = FontWeight.Bold)
    }
}