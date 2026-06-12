package com.elima.installment_management

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.elima.installment_management.ui.screens.AddLoanScreen
import com.elima.installment_management.ui.screens.InstallmentListScreen
import com.elima.installment_management.ui.screens.LoanListScreen
import com.elima.installment_management.ui.screens.SettingsScreen
import com.elima.installment_management.ui.theme.MyApplicationTheme
import com.elima.installment_management.ui.viewmodel.LoanViewModel
import com.elima.installment_management.ui.viewmodel.LoanViewModelFactory

class MainActivity : ComponentActivity() {
    private val loanViewModel: LoanViewModel by viewModels {
        LoanViewModelFactory((application as MyApplication).repository)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkNotificationPermission()
        val locale = java.util.Locale("fa", "IR")
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MyApplicationApp(loanViewModel)
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MyApplicationApp(loanViewModel: LoanViewModel? = null) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.LOANS) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = stringResource(it.labelRes)
                        )
                    },
                    label = { Text(stringResource(it.labelRes)) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.LOANS -> {
                        if (loanViewModel != null) {
                            LoanNavigation(loanViewModel)
                        } else {
                            Text(text = "Loading...")
                        }
                    }
                    AppDestinations.SETTINGS -> SettingsScreen()
                    AppDestinations.PROFILE -> Text(text = stringResource(R.string.menu_profile))
                }
            }
        }
    }
}

@Composable
fun LoanNavigation(viewModel: LoanViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "loan_list") {
        composable("loan_list") {
            LoanListScreen(
                viewModel = viewModel,
                onAddLoanClick = { navController.navigate("add_loan") },
                onLoanClick = { loanId -> navController.navigate("installment_list/$loanId") },
                onEditLoanClick = { loanId -> navController.navigate("edit_loan/$loanId") }
            )
        }
        composable("add_loan") {
            AddLoanScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit_loan/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getInt("loanId")
            AddLoanScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                loanId = loanId
            )
        }
        composable(
            route = "installment_list/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getInt("loanId") ?: 0
            InstallmentListScreen(
                loanId = loanId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

enum class AppDestinations(
    val labelRes: Int,
    val icon: ImageVector,
) {
    LOANS(R.string.menu_loans, Icons.Default.Home),
    SETTINGS(R.string.menu_settings, Icons.Default.Settings),
    PROFILE(R.string.menu_profile, Icons.Default.Person),
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        MyApplicationApp()
    }
}
