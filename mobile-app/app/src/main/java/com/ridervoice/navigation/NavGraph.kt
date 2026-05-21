package com.ridervoice.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ridervoice.ui.screens.HomeScreen
import com.ridervoice.ui.screens.LoginScreen
import com.ridervoice.ui.screens.RoomScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    // Simple check: if logged in, go directly to HOME
    val startDest = if (FirebaseAuth.getInstance().currentUser != null) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        
        composable(Routes.LOGIN) {
            LoginScreen(
                onGoogleSignInClick = { 
                    // To be wired to Google SignIn Client
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onPhoneOtpClick = { /* Wire to OTP */ }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onStartRideClick = {
                    val roomName = "PUNE NIGHT RIDERS"
                    val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Rider"
                    navController.navigate(Routes.roomPath(roomName, userName))
                },
                onSquadClick = {
                    navController.navigate(Routes.SQUAD)
                }
            )
        }

        composable(Routes.SQUAD) {
            SquadScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ROOM,
            arguments = listOf(
                navArgument("roomName") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            RoomScreen(
                roomName = roomName,
                userName = userName,
                onLeave = { navController.popBackStack(Routes.HOME, false) }
            )
        }
    }
}
