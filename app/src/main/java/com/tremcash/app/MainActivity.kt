package com.tremcash.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.tremcash.app.data.DatabaseHelper
import com.tremcash.app.repository.TransactionRepository
import com.tremcash.app.ui.MainViewModel
import com.tremcash.app.ui.screens.*
import com.tremcash.app.ui.theme.TremCashTheme
import com.tremcash.app.util.NotificationHelper
import com.tremcash.app.model.Transaction
import com.tremcash.app.model.TransactioType

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationHelper(this).showReminderNotification()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = TransactionRepository(DatabaseHelper(this))
        val viewModel = MainViewModel(repository)

        checkNotificationPermission()

        enableEdgeToEdge()
        setContent {
            TremCashTheme {
                val navController = rememberNavController()
                val transactions by viewModel.transactions.collectAsState()
                var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

                LaunchedEffect(Unit) {
                    viewModel.loadTransactions()
                }

                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            transactions = transactions,
                            onAddIncome = {
                                editingTransaction = null
                                navController.navigate("transaction_screen/RECEITA")
                            },
                            onAddExpense = {
                                editingTransaction = null
                                navController.navigate("transaction_screen/DESPESA")
                            },
                            onDailyExpense = { transaction ->
                                viewModel.addTransaction(transaction, 1)
                            },
                            onEdit = { transaction ->
                                editingTransaction = transaction
                                navController.navigate("transaction_screen/${transaction.type.name}")
                            },
                            // AQUI ESTÁ A CORREÇÃO: Passando os dois parâmetros de exclusão
                            onDeleteSingle = { id ->
                                viewModel.deleteTransaction(id)
                            },
                            onDeleteAll = { groupId ->
                                viewModel.deleteTransactionGroup(groupId)
                            }
                        )
                    }

                    composable("transaction_screen/{type}") { backStackEntry ->
                        val typeStr = backStackEntry.arguments?.getString("type") ?: "RECEITA"
                        val type = TransactioType.valueOf(typeStr)

                        TransactionScreen(
                            type = type,
                            transactionToEdit = editingTransaction
                        ) { transaction, installments ->
                            if (editingTransaction == null) {
                                viewModel.addTransaction(transaction, installments)
                            } else {
                                viewModel.updateTransaction(transaction.copy(id = editingTransaction!!.id))
                            }
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationHelper(this).showReminderNotification()
            }
        } else {
            NotificationHelper(this).showReminderNotification()
        }
    }
}