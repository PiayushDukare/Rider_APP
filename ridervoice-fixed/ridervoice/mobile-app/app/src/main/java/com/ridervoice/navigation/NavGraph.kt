package com.ridervoice.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ridervoice.ui.screens.HomeScreen
import com.ridervoice.ui.screens.JoinRoomScreen
import com.ridervoice.ui.screens.RoomScreen

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                onCreateRoom = { userName ->
                    // Auto-generate room name for host
                    val roomName = "room_${System.currentTimeMillis()}"
                    navController.navigate(Routes.roomPath(roomName, userName))
                },
                onJoinRoom = {
                    navController.navigate(Routes.JOIN)
                }
            )
        }

        composable(Routes.JOIN) {
            JoinRoomScreen(
                onJoin = { roomCode, userName ->
                    // FIX: actually pass roomCode and userName into navigation
                    navController.navigate(Routes.roomPath(roomCode, userName))
                }
            )
        }

        // FIX: Room route now takes roomName and userName arguments
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
