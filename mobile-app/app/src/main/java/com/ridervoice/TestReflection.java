package com.ridervoice;

public class TestReflection {
    public static void main(String[] args) {
        for (java.lang.reflect.Method m : io.livekit.android.room.participant.LocalParticipant.class.getMethods()) {
            if (m.getName().equals("publishData")) {
                System.out.println(m.toString());
            }
        }
    }
}
