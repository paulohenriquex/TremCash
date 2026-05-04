package com.tremcash.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tremcash.app.ui.theme.*
@Composable
fun CategoryChartCard(expensesByCategory: Map<String, Double>) {
    // 1. Calcula o total geral para poder definir as porcentagens das barras
    val totalExpense = expensesByCategory.values.sum()

    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Gastos por Categoria",
                fontWeight = FontWeight.Bold,
                color = TremBlue,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (expensesByCategory.isEmpty()) {
                Text("Nenhuma despesa registrada este mês.", color = Color.Gray, fontSize = 13.sp)
            } else {
                // 2. Cria uma linha para cada categoria
                expensesByCategory.forEach { (category, value) ->
                    val percentage = if (totalExpense > 0) (value / totalExpense).toFloat() else 0f

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = category, fontSize = 12.sp, color = Color.DarkGray)
                            Text(
                                text = "R$ ${"%.2f".format(value)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3. Desenha a barra de progresso (Gráfico de Barras)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color(0xFFF1F1F1), CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percentage) // A barra preenche apenas a porcentagem do gasto
                                    .height(10.dp)
                                    .background(
                                        // Se gastou mais de 40% do total em uma só coisa, fica vermelho (alerta)
                                        color = if (percentage > 0.4f) TremRed else TremBlue,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}