package com.tremcash.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.tremcash.app.model.Transaction
import com.tremcash.app.model.TransactioType
import com.tremcash.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    type: TransactioType,
    transactionToEdit: Transaction? = null, // NOVO: Parâmetro para edição
    onSave: (Transaction, Int) -> Unit
) {
    val themeColor = if (type == TransactioType.RECEITA) TremBlue else TremRed

    // 1. Estados do formulário (Iniciam com os dados da edição, se houver)
    var description by remember { mutableStateOf(transactionToEdit?.description ?: "") }
    var value by remember { mutableStateOf(transactionToEdit?.value?.toString() ?: "") }
    var category by remember { mutableStateOf(transactionToEdit?.categoryName ?: "Geral") }
    var repetition by remember { mutableStateOf(transactionToEdit?.repetition ?: "Única") }
    var installments by remember { mutableStateOf("1") }

    // 2. Formatadores de Data
    val displayFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val dbFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    // 3. Lógica para converter data do banco (String) para milissegundos (DatePicker)
    val initialDateMillis = remember {
        if (transactionToEdit != null) {
            try {
                dbFormatter.parse(transactionToEdit.date)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } else {
            System.currentTimeMillis()
        }
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val displayDateText = displayFormatter.format(Date(selectedMillis))

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK", color = themeColor) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val prefix = if (transactionToEdit == null) "Nova" else "Editar"
                    val label = if (type == TransactioType.RECEITA) "Receita" else "Despesa"
                    Text("$prefix $label", color = themeColor, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { /* MainActivity gerencia o popBackStack */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = themeColor)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Painel de Valor Visual
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Valor", color = Color.Gray, fontSize = 14.sp)
                Text(text = "R$ ${value.ifEmpty { "0,00" }}", color = themeColor, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }

            // Inputs principais
            FigmaInput(Icons.Default.Payments, "Valor", value, { value = it }, themeColor, KeyboardType.Decimal)
            FigmaInput(Icons.Default.Description, "Descrição", description, { description = it }, themeColor)

            // Campo de Data
            Column(modifier = Modifier.padding(vertical = 8.dp).clickable { showDatePicker = true }) {
                Text("Data", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                OutlinedTextField(
                    value = displayDateText,
                    onValueChange = {},
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFFF1F1F1),
                        disabledTextColor = Color.Black,
                        disabledContainerColor = Color.White
                    )
                )
            }

            FigmaInput(Icons.Default.Tag, "Categoria", category, { category = it }, themeColor)

            // Sugestões rápidas para despesas
            if (type == TransactioType.DESPESA) {
                Text("Sugestões rápidas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sugestoes = listOf("Lanches", "Café", "Transporte", "Casa", "Saúde", "Dentista", "Combustível", "Mercado", "Assinatura")
                    sugestoes.forEach { sugestao ->
                        FilterChip(
                            selected = category == sugestao,
                            onClick = { category = sugestao },
                            label = { Text(sugestao) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.1f),
                                selectedLabelColor = themeColor,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }
            }

            // Repetição (Escondemos se estivermos editando para evitar bagunçar parcelas antigas, ou deixamos fixo)
            Text("Repetição", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
            val options = if (type == TransactioType.RECEITA) listOf("Única", "Fixa") else listOf("Única", "Fixa", "Parcelada")

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color(0xFFF1F1F1), RoundedCornerShape(12.dp)).padding(4.dp)) {
                options.forEach { opt ->
                    Button(
                        onClick = { repetition = opt },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (repetition == opt) themeColor else Color.Transparent)
                    ) {
                        Text(opt, color = if (repetition == opt) Color.White else Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            if (type == TransactioType.DESPESA && repetition == "Parcelada" && transactionToEdit == null) {
                FigmaInput(Icons.Default.Schedule, "Parcelas (1-72)", installments, { installments = it }, themeColor, KeyboardType.Number)
            }

            // Botão Salvar
            Button(
                onClick = {
                    val numericValue = value.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val formattedDbDate = dbFormatter.format(Date(selectedMillis))

                    val monthsToCreate = when (repetition) {
                        "Fixa" -> if (transactionToEdit == null) 24 else 1 // Se editando, altera só o atual
                        "Parcelada" -> installments.toIntOrNull()?.coerceIn(1, 72) ?: 1
                        else -> 1
                    }

                    onSave(
                        Transaction(
                            id = transactionToEdit?.id ?: 0,
                            type = type,
                            description = description,
                            value = numericValue,
                            date = formattedDbDate,
                            categoryName = category,
                            repetition = repetition
                        ),
                        monthsToCreate
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(themeColor)
            ) {
                Text(if (transactionToEdit == null) "Confirmar Lançamento" else "Salvar Alterações", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun FigmaInput(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color,
    kb: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(icon, null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = kb),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color,
                unfocusedBorderColor = Color(0xFFF1F1F1),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = color
            )
        )
    }
}