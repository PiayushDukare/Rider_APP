package com.ridervoice.network
import com.ridervoice.models.RoomData
import com.ridervoice.models.Friend
import com.ridervoice.models.RideInvite
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import com.ridervoice.models.RoomTokenRequest
import com.ridervoice.models.FcmTokenRequest

interface ApiService {
    @POST("/api/rooms/room/token")
    suspend fun getRoomToken(@Body body: RoomTokenRequest): Response<RoomData>

    @GET("/api/friends/list/{userId}")
    suspend fun getFriendsList(@Path("userId") userId: String): Response<List<Friend>>

    @GET("/api/rides/invites/{userId}")
    suspend fun getPendingInvites(@Path("userId") userId: String): Response<List<RideInvite>>

    @POST("/api/users/fcm-token")
    suspend fun updateFcmToken(@Body body: FcmTokenRequest): Response<Any>
}
