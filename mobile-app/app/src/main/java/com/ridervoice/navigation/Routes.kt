package com.ridervoice.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val SQUAD = "squad"
    const val JOIN = "join"
    // Template with named args
    const val ROOM = "room/{roomName}/{userName}"

    fun roomPath(roomName: String, userName: String) = "room/$roomName/$userName"
}
