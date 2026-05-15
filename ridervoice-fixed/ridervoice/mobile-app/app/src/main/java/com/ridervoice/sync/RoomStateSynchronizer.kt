package com.ridervoice.sync
import com.ridervoice.models.Participant
class RoomStateSynchronizer {
    private val roomParticipants = mutableMapOf<String, Participant>()
    fun syncParticipant(p: Participant) { roomParticipants[p.identity] = p }
    fun remove(identity: String) { roomParticipants.remove(identity) }
    fun getAll(): List<Participant> = roomParticipants.values.toList()
}
